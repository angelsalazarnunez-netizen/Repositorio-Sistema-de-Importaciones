package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoDocumento;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 22/09/2026
 * Descripción: Reglas de negocio de la documentación de un expediente que son comunes a todos los
 *   trámites. Permite retirar del expediente un documento cargado por error: el archivo no se
 *   borra, se marca como inactivo y la eliminación queda anotada en la bitácora con el usuario,
 *   la fecha y los datos del documento retirado, de modo que el expediente conserva su historia.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: se agregan el guardado versionado de un documento y la limpieza de los archivos
 *     escritos cuando la transacción termina en rollback, para que los trámites nuevos los
 *     reutilicen en lugar de copiarlos (HU-025). Los métodos existentes no cambian.
 */
@Service
public class BllDocumento {

    private final DalDocumento vgo_DalDocumento;
    private final DalTipoDocumento vgo_DalTipoDocumento;
    private final DalUsuario vgo_DalUsuario;
    private final BllBitacora vgo_BllBitacora;
    private final AlmacenamientoDocumentos vgo_AlmacenamientoDocumentos;

    public BllDocumento(
            DalDocumento pvo_DalDocumento,
            DalTipoDocumento pvo_DalTipoDocumento,
            DalUsuario pvo_DalUsuario,
            BllBitacora pvo_BllBitacora,
            AlmacenamientoDocumentos pvo_AlmacenamientoDocumentos) {
        this.vgo_DalDocumento = pvo_DalDocumento;
        this.vgo_DalTipoDocumento = pvo_DalTipoDocumento;
        this.vgo_DalUsuario = pvo_DalUsuario;
        this.vgo_BllBitacora = pvo_BllBitacora;
        this.vgo_AlmacenamientoDocumentos = pvo_AlmacenamientoDocumentos;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 22/09/2026
     * Descripción: Retira del expediente un documento vigente. Comprueba que el documento exista,
     *   que pertenezca al expediente indicado y que no esté ya retirado.
     *
     * @param pvn_IdTramite expediente al que pertenece el documento
     * @param pvn_IdDocumento documento que se retira
     * @param pvn_IdUsuario usuario que realiza la acción
     */
    @Transactional
    public void EliminarDocumento(Integer pvn_IdTramite, Integer pvn_IdDocumento, Integer pvn_IdUsuario) {
        EntDocumento vlo_EnDocumento = this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(pvn_IdDocumento);
        if (vlo_EnDocumento == null || !vlo_EnDocumento.getIdTramite().equals(pvn_IdTramite)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DOCUMENTO_NO_PERTENECE_AL_EXPEDIENTE);
        }

        if (!Constantes.ESTADO_DOCUMENTO_ACTIVO.equals(vlo_EnDocumento.getEstadoDocumento())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DOCUMENTO_YA_ELIMINADO);
        }

        EntUsuario vlo_EnUsuario = this.ObtenerUsuario(pvn_IdUsuario);

        vlo_EnDocumento.setEstadoDocumento(Constantes.ESTADO_DOCUMENTO_INACTIVO);
        this.vgo_DalDocumento.SIMF_DOCUMENTO_ModificarRegistro(vlo_EnDocumento);

        this.vgo_BllBitacora.RegistrarAccion(
                pvn_IdTramite,
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_DOCUMENTO_ELIMINADO,
                Constantes.ENTIDAD_BITACORA_DOCUMENTO,
                vlo_EnDocumento.getIdDocumento(),
                "Documento: " + vlo_EnDocumento.getNombreArchivo()
                        + ". Versión: " + vlo_EnDocumento.getVersion() + ".",
                "El documento se retiró del expediente y se conserva como inactivo.");
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 22/09/2026
     * Descripción: Obtiene el usuario que realiza la acción y comprueba que esté registrado.
     *
     * @param pvn_IdUsuario identificador del usuario
     * @return usuario encontrado
     */
    private EntUsuario ObtenerUsuario(Integer pvn_IdUsuario) {
        EntUsuario vlo_EnUsuario = this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(pvn_IdUsuario);
        if (vlo_EnUsuario == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_USUARIO_RESPONSABLE_INVALIDO);
        }
        return vlo_EnUsuario;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 25/09/2026
     * Descripción: Cambia la clasificación de un documento vigente del expediente (HU-045). El
     *   archivo y su versión no se tocan: solo cambia el tipo con el que queda clasificado, y el
     *   cambio se anota en la bitácora con la clasificación anterior y la nueva.
     *
     * @param pvn_IdTramite expediente al que pertenece el documento
     * @param pvn_IdDocumento documento que se clasifica
     * @param pvc_CodigoTipoDocumento código del tipo de documento del catálogo
     * @param pvn_IdUsuario usuario que realiza la acción
     */
    @Transactional
    public void ClasificarDocumento(
            Integer pvn_IdTramite,
            Integer pvn_IdDocumento,
            String pvc_CodigoTipoDocumento,
            Integer pvn_IdUsuario) {

        EntDocumento vlo_EnDocumento = this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(pvn_IdDocumento);
        if (vlo_EnDocumento == null || !vlo_EnDocumento.getIdTramite().equals(pvn_IdTramite)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DOCUMENTO_NO_PERTENECE_AL_EXPEDIENTE);
        }
        if (!Constantes.ESTADO_DOCUMENTO_ACTIVO.equals(vlo_EnDocumento.getEstadoDocumento())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DOCUMENTO_YA_ELIMINADO);
        }

        EntTipoDocumento vlo_EnTipoNuevo = pvc_CodigoTipoDocumento == null
                ? null
                : this.vgo_DalTipoDocumento.SIMM_TIPO_DOCUMENTO_ObtenerPorCodigo(
                        pvc_CodigoTipoDocumento.trim().toUpperCase());
        if (vlo_EnTipoNuevo == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_CLASIFICACION_INVALIDA);
        }
        if (vlo_EnTipoNuevo.getIdTipoDocumento().equals(vlo_EnDocumento.getIdTipoDocumento())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_CLASIFICACION_SIN_CAMBIO);
        }

        EntUsuario vlo_EnUsuario = this.ObtenerUsuario(pvn_IdUsuario);
        EntTipoDocumento vlo_EnTipoAnterior = this.vgo_DalTipoDocumento
                .SIMM_TIPO_DOCUMENTO_ObtenerRegistro(vlo_EnDocumento.getIdTipoDocumento());

        vlo_EnDocumento.setIdTipoDocumento(vlo_EnTipoNuevo.getIdTipoDocumento());
        this.vgo_DalDocumento.SIMF_DOCUMENTO_ModificarRegistro(vlo_EnDocumento);

        this.vgo_BllBitacora.RegistrarAccion(
                pvn_IdTramite,
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_DOCUMENTO_CLASIFICADO,
                Constantes.ENTIDAD_BITACORA_DOCUMENTO,
                vlo_EnDocumento.getIdDocumento(),
                "Documento: " + vlo_EnDocumento.getNombreArchivo() + ". Clasificación: "
                        + (vlo_EnTipoAnterior == null ? "sin clasificar" : vlo_EnTipoAnterior.getNombre())
                        + ".",
                "Documento: " + vlo_EnDocumento.getNombreArchivo() + ". Clasificación: "
                        + vlo_EnTipoNuevo.getNombre() + ".");
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Guarda el archivo en disco y registra el documento en el expediente. Si el
     *   expediente ya tiene un documento vigente del mismo tipo y nombre, el nuevo se registra como
     *   la versión siguiente y el anterior queda inactivo, sin perderse. La ruta escrita se acumula
     *   para poder borrarla si la transacción no se confirma.
     *
     * @param pvo_EnTramite expediente ya persistido
     * @param pvn_IdUsuarioCarga usuario que adjunta el documento
     * @param pvo_Archivo archivo recibido
     * @param pvc_CodigoTipoDocumento código del tipo de documento del catálogo
     * @param pvo_LsRutasEscritas rutas escritas durante la transacción
     * @return documento registrado
     */
    public EntDocumento GuardarDocumento(
            EntTramite pvo_EnTramite,
            Integer pvn_IdUsuarioCarga,
            MultipartFile pvo_Archivo,
            String pvc_CodigoTipoDocumento,
            List<String> pvo_LsRutasEscritas) {

        EntTipoDocumento vlo_EnTipoDocumento = this.vgo_DalTipoDocumento
                .SIMM_TIPO_DOCUMENTO_ObtenerPorCodigo(pvc_CodigoTipoDocumento);
        if (vlo_EnTipoDocumento == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_DOCUMENTO_NO_CONFIGURADO);
        }

        String vlc_NombreArchivo = this.vgo_AlmacenamientoDocumentos
                .DepurarNombreArchivo(pvo_Archivo.getOriginalFilename());
        EntDocumento vlo_EnVersionAnterior = this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerVersionVigente(
                pvo_EnTramite.getIdTramite(),
                vlo_EnTipoDocumento.getIdTipoDocumento(),
                vlc_NombreArchivo,
                Constantes.ESTADO_DOCUMENTO_ACTIVO);

        String vlc_RutaArchivo = this.vgo_AlmacenamientoDocumentos
                .GuardarArchivo(pvo_Archivo, pvo_EnTramite.getReferenciaInterna());
        pvo_LsRutasEscritas.add(vlc_RutaArchivo);

        EntDocumento vlo_EnDocumento = new EntDocumento();
        vlo_EnDocumento.setIdTramite(pvo_EnTramite.getIdTramite());
        vlo_EnDocumento.setIdTipoDocumento(vlo_EnTipoDocumento.getIdTipoDocumento());
        vlo_EnDocumento.setIdUsuarioCarga(pvn_IdUsuarioCarga);
        vlo_EnDocumento.setNombreArchivo(vlc_NombreArchivo);
        vlo_EnDocumento.setRutaArchivo(vlc_RutaArchivo);
        vlo_EnDocumento.setEstadoDocumento(Constantes.ESTADO_DOCUMENTO_ACTIVO);

        if (vlo_EnVersionAnterior == null) {
            vlo_EnDocumento.setVersion((short) 1); // 1 = primera versión del documento
        } else {
            vlo_EnDocumento.setVersion((short) (vlo_EnVersionAnterior.getVersion() + 1));
            vlo_EnDocumento.setIdDocumentoAnterior(vlo_EnVersionAnterior.getIdDocumento());
            vlo_EnVersionAnterior.setEstadoDocumento(Constantes.ESTADO_DOCUMENTO_INACTIVO);
            this.vgo_DalDocumento.SIMF_DOCUMENTO_ModificarRegistro(vlo_EnVersionAnterior);
        }
        this.vgo_DalDocumento.SIMF_DOCUMENTO_InsertarRegistro(vlo_EnDocumento);
        return vlo_EnDocumento;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Programa el borrado de los archivos escritos si la transacción termina en
     *   rollback, de manera que no queden en disco archivos sin registro en la base de datos.
     *
     * @param pvo_LsRutasEscritas rutas relativas que se escriban durante la transacción
     */
    public void RegistrarLimpiezaSiFallaLaTransaccion(List<String> pvo_LsRutasEscritas) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int pvn_EstadoTransaccion) {
                if (pvn_EstadoTransaccion != TransactionSynchronization.STATUS_ROLLED_BACK) {
                    return;
                }
                for (String vlc_Ruta : pvo_LsRutasEscritas) {
                    vgo_AlmacenamientoDocumentos.EliminarArchivo(vlc_Ruta);
                }
            }
        });
    }
}
