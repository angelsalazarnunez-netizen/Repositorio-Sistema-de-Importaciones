package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDesalmacenajeEspecial;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalMoneda;
import cr.ac.ucr.sim.accesodatos.tramites.DalPais;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCambioEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCorreccionDesalmacenaje;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDesalmacenajeEspecial;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoDocumental;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExpedienteDetalle;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExpedienteResumen;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMoneda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntPais;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroDesalmacenaje;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaDesalmacenaje;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Reglas de negocio del expediente de desalmacenaje especial (HU-031). Integra el
 *   formulario de registro, el expediente y su documentación: el trámite, sus datos específicos,
 *   sus archivos y su historial de estados se crean y se consultan como una sola unidad.
 *   Esta categoría se utiliza cuando el trámite no corresponde a una donación, una compra de la
 *   Fundación ni un pedido a proveedor, y aplica a mercancía que ya es propiedad de la institución.
 *
 * Historial de cambios:
 *   Responsable: Katherine Susana Valverde Fallas
 *   Fecha: 15/09/2026
 *   Descripción: se agrega el seguimiento del expediente (HU-032): consulta de los estados
 *     disponibles y cambio de estado con justificación obligatoria, que queda anotado en el
 *     historial junto con el estado anterior, el nuevo, la fecha y el usuario responsable.
 *   Responsable: Katherine Susana Valverde Fallas
 *   Fecha: 16/09/2026
 *   Descripción: el cambio de estado y la documentación agregada durante el seguimiento se
 *     registran también en la bitácora de auditoría del expediente (HU-032).
 *   Responsable: Katherine Susana Valverde Fallas
 *   Fecha: 22/09/2026
 *   Descripción: la documentación deja de ser obligatoria para abrir el expediente y se permite
 *     retirar un documento cargado por error, que se conserva como inactivo.
 */
@Service
public class BllDesalmacenajeEspecial {

    private final DalTramite vgo_DalTramite;
    private final DalDesalmacenajeEspecial vgo_DalDesalmacenajeEspecial;
    private final DalDocumento vgo_DalDocumento;
    private final DalHistorialEstado vgo_DalHistorialEstado;
    private final DalTipoTramite vgo_DalTipoTramite;
    private final DalEstadoTramite vgo_DalEstadoTramite;
    private final DalMoneda vgo_DalMoneda;
    private final DalPais vgo_DalPais;
    private final DalTipoDocumento vgo_DalTipoDocumento;
    private final DalUsuario vgo_DalUsuario;
    private final AlmacenamientoDocumentos vgo_AlmacenamientoDocumentos;
    private final BllBitacora vgo_BllBitacora;
    private final BllDocumento vgo_BllDocumento;

    public BllDesalmacenajeEspecial(
            DalTramite pvo_DalTramite,
            DalDesalmacenajeEspecial pvo_DalDesalmacenajeEspecial,
            DalDocumento pvo_DalDocumento,
            DalHistorialEstado pvo_DalHistorialEstado,
            DalTipoTramite pvo_DalTipoTramite,
            DalEstadoTramite pvo_DalEstadoTramite,
            DalMoneda pvo_DalMoneda,
            DalPais pvo_DalPais,
            DalTipoDocumento pvo_DalTipoDocumento,
            DalUsuario pvo_DalUsuario,
            AlmacenamientoDocumentos pvo_AlmacenamientoDocumentos,
            BllBitacora pvo_BllBitacora,
            BllDocumento pvo_BllDocumento) {
        this.vgo_DalTramite = pvo_DalTramite;
        this.vgo_DalDesalmacenajeEspecial = pvo_DalDesalmacenajeEspecial;
        this.vgo_DalDocumento = pvo_DalDocumento;
        this.vgo_DalHistorialEstado = pvo_DalHistorialEstado;
        this.vgo_DalTipoTramite = pvo_DalTipoTramite;
        this.vgo_DalEstadoTramite = pvo_DalEstadoTramite;
        this.vgo_DalMoneda = pvo_DalMoneda;
        this.vgo_DalPais = pvo_DalPais;
        this.vgo_DalTipoDocumento = pvo_DalTipoDocumento;
        this.vgo_DalUsuario = pvo_DalUsuario;
        this.vgo_AlmacenamientoDocumentos = pvo_AlmacenamientoDocumentos;
        this.vgo_BllBitacora = pvo_BllBitacora;
        this.vgo_BllDocumento = pvo_BllDocumento;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Crea el expediente de desalmacenaje especial, le asocia la referencia interna
     *   específica del tipo de trámite, le asigna el estado inicial Pendiente, adjunta la
     *   documentación de respaldo y abre su historial de estados. No exige contrato SICOP ni
     *   orden de compra.
     *
     * @param pvo_Registro datos del formulario de registro
     * @param pvo_LsRespaldo documentos de respaldo, actas históricas o documentos atípicos
     * @param pvo_PackingList lista de empaque, opcional
     * @param pvo_Factura factura o documento equivalente, opcional
     * @param pvo_DocumentoEmbarque documento de embarque, opcional
     * @return confirmación del registro con la referencia interna y el estado asignados
     */
    @Transactional
    public EntRespuestaDesalmacenaje InsertarRegistro(
            EntRegistroDesalmacenaje pvo_Registro,
            List<MultipartFile> pvo_LsRespaldo,
            MultipartFile pvo_PackingList,
            MultipartFile pvo_Factura,
            MultipartFile pvo_DocumentoEmbarque) {

        if (pvo_Registro == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }

        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteDesalmacenaje();
        String vlc_ReferenciaInterna = this.NormalizarReferencia(pvo_Registro.getReferenciaInterna());
        String vlc_OrigenDependencia = this.NormalizarTexto(pvo_Registro.getOrigenDependencia());
        String vlc_Justificacion = this.NormalizarTexto(pvo_Registro.getJustificacion());
        List<MultipartFile> vlo_LsRespaldo = this.DepurarArchivos(pvo_LsRespaldo);

        this.ValidarReferenciaInterna(vlc_ReferenciaInterna, vlo_EnTipoTramite);
        this.ValidarOrigenDependencia(vlc_OrigenDependencia);
        this.ValidarValorEstimado(pvo_Registro.getValorEstimado());
        this.ValidarJustificacion(vlc_Justificacion);
        this.ValidarDocumentosRespaldo(vlo_LsRespaldo);

        EntUsuario vlo_EnUsuario = this.ObtenerUsuarioResponsable(pvo_Registro.getIdUsuarioResponsable());
        EntMoneda vlo_EnMoneda = this.ObtenerMoneda(pvo_Registro.getMoneda());
        EntPais vlo_EnPaisOrigen = this.ObtenerPaisOrigen(pvo_Registro.getPaisOrigen());
        EntEstadoTramite vlo_EnEstadoInicial = this.ObtenerEstadoInicial(vlo_EnTipoTramite.getIdTipoTramite());

        EntTramite vlo_EnTramite = new EntTramite();
        vlo_EnTramite.setReferenciaInterna(vlc_ReferenciaInterna);
        vlo_EnTramite.setIdTipoTramite(vlo_EnTipoTramite.getIdTipoTramite());
        vlo_EnTramite.setIdEstadoTramite(vlo_EnEstadoInicial.getIdEstadoTramite());
        vlo_EnTramite.setIdUsuarioResponsable(vlo_EnUsuario.getIdUsuario());
        vlo_EnTramite.setIdMoneda(vlo_EnMoneda.getIdMoneda());
        vlo_EnTramite.setIdPaisOrigen(vlo_EnPaisOrigen == null ? null : vlo_EnPaisOrigen.getIdPais());
        vlo_EnTramite.setValorEstimado(pvo_Registro.getValorEstimado());
        this.vgo_DalTramite.SIMT_TRAMITE_InsertarRegistro(vlo_EnTramite);

        EntDesalmacenajeEspecial vlo_EnDesalmacenaje = new EntDesalmacenajeEspecial();
        vlo_EnDesalmacenaje.setIdTramite(vlo_EnTramite.getIdTramite());
        vlo_EnDesalmacenaje.setOrigenDependencia(vlc_OrigenDependencia);
        vlo_EnDesalmacenaje.setJustificacion(vlc_Justificacion);
        this.vgo_DalDesalmacenajeEspecial.SIMT_DESALMACENAJE_ESPEC_InsertarRegistro(vlo_EnDesalmacenaje);

        this.AbrirHistorialEstado(vlo_EnTramite, vlo_EnEstadoInicial, vlo_EnUsuario.getIdUsuario());

        int vln_CantidadDocumentos = this.RegistrarDocumentos(
                vlo_EnTramite,
                vlo_EnUsuario.getIdUsuario(),
                vlo_LsRespaldo,
                pvo_PackingList,
                pvo_Factura,
                pvo_DocumentoEmbarque);

        EntRespuestaDesalmacenaje vlo_Respuesta = new EntRespuestaDesalmacenaje();
        vlo_Respuesta.setMensaje(Constantes.MENSAJE_REGISTRO_DESALMACENAJE_EXITOSO);
        vlo_Respuesta.setIdTramite(vlo_EnTramite.getIdTramite());
        vlo_Respuesta.setReferenciaInterna(vlo_EnTramite.getReferenciaInterna());
        vlo_Respuesta.setEstado(vlo_EnEstadoInicial.getNombre());
        vlo_Respuesta.setUsuarioResponsable(vlo_EnUsuario.getNombreCompleto());
        vlo_Respuesta.setCantidadDocumentos(vln_CantidadDocumentos);
        vlo_Respuesta.setFechaCreacion(vlo_EnTramite.getFechaCreacion());
        return vlo_Respuesta;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Agrega documentación a un expediente ya registrado. La documentación de un
     *   desalmacenaje especial suele llegar después del registro, por lo que el expediente
     *   permanece abierto para recibir actas, facturas o documentos de embarque posteriores.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvn_IdUsuarioCarga usuario que adjunta la documentación
     * @param pvo_LsRespaldo documentos de respaldo adicionales
     * @param pvo_PackingList lista de empaque, opcional
     * @param pvo_Factura factura o documento equivalente, opcional
     * @param pvo_DocumentoEmbarque documento de embarque, opcional
     * @return expediente actualizado con toda su documentación
     */
    @Transactional
    public EntExpedienteDetalle InsertarDocumentos(
            String pvc_ReferenciaInterna,
            Integer pvn_IdUsuarioCarga,
            List<MultipartFile> pvo_LsRespaldo,
            MultipartFile pvo_PackingList,
            MultipartFile pvo_Factura,
            MultipartFile pvo_DocumentoEmbarque) {

        EntTramite vlo_EnTramite = this.ObtenerTramite(pvc_ReferenciaInterna);
        EntUsuario vlo_EnUsuario = this.ObtenerUsuarioResponsable(pvn_IdUsuarioCarga);
        List<MultipartFile> vlo_LsRespaldo = this.DepurarArchivos(pvo_LsRespaldo);

        int vln_Cantidad = this.RegistrarDocumentos(
                vlo_EnTramite,
                vlo_EnUsuario.getIdUsuario(),
                vlo_LsRespaldo,
                pvo_PackingList,
                pvo_Factura,
                pvo_DocumentoEmbarque);

        if (vln_Cantidad == 0) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_SIN_DOCUMENTOS_ADJUNTOS);
        }

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_DOCUMENTO_AGREGADO,
                Constantes.ENTIDAD_BITACORA_DOCUMENTO,
                null,
                null,
                "Documentos agregados al expediente: " + vln_Cantidad + ".");

        return this.ArmarExpedienteDetalle(vlo_EnTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 22/09/2026
     * Descripción: Retira del expediente un documento cargado por error. El archivo se conserva
     *   como inactivo y la eliminación queda anotada en la bitácora del expediente.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvn_IdDocumento documento que se retira
     * @param pvn_IdUsuario usuario que realiza la acción
     * @return expediente actualizado con su documentación vigente
     */
    @Transactional
    public EntExpedienteDetalle EliminarDocumento(
            String pvc_ReferenciaInterna,
            Integer pvn_IdDocumento,
            Integer pvn_IdUsuario) {

        EntTramite vlo_EnTramite = this.ObtenerTramite(pvc_ReferenciaInterna);
        this.vgo_BllDocumento.EliminarDocumento(
                vlo_EnTramite.getIdTramite(), pvn_IdDocumento, pvn_IdUsuario);
        return this.ArmarExpedienteDetalle(vlo_EnTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene los expedientes de desalmacenaje especial registrados, para la
     *   pantalla de seguimiento y para la generación de reportes.
     *
     * @return expedientes del más reciente al más antiguo
     */
    @Transactional(readOnly = true)
    public List<EntExpedienteResumen> ObtenerListaExpedientes() {
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteDesalmacenaje();
        return this.vgo_DalTramite.SIMT_TRAMITE_ObtenerListaResumen(
                vlo_EnTipoTramite.getIdTipoTramite(),
                Constantes.ESTADO_DOCUMENTO_ACTIVO);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene un expediente completo con su documentación y su historial de estados.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @return expediente completo
     */
    @Transactional(readOnly = true)
    public EntExpedienteDetalle ObtenerExpediente(String pvc_ReferenciaInterna) {
        return this.ArmarExpedienteDetalle(this.ObtenerTramite(pvc_ReferenciaInterna));
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene los estados que el catálogo define para el desalmacenaje especial, en su
     *   orden, para ofrecerlos como destino de un cambio de estado durante el seguimiento (HU-032).
     *
     * @return estados disponibles del tipo de trámite
     */
    @Transactional(readOnly = true)
    public List<EntEstadoTramite> ObtenerEstadosDisponibles() {
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteDesalmacenaje();
        return this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorTipoTramite(
                vlo_EnTipoTramite.getIdTipoTramite(), Constantes.ESTADO_CATALOGO_ACTIVO);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Actualiza el estado de un expediente durante su seguimiento (HU-032). Exige una
     *   justificación, comprueba que el estado pertenezca al tipo de trámite y anota el movimiento
     *   en el historial con el estado anterior, el nuevo, la fecha, la hora y el usuario. El
     *   historial previo y la documentación del expediente no se modifican.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvo_Cambio estado nuevo, justificación y usuario responsable del cambio
     * @return expediente actualizado con su historial
     */
    @Transactional
    public EntExpedienteDetalle ActualizarEstado(String pvc_ReferenciaInterna, EntCambioEstado pvo_Cambio) {
        if (pvo_Cambio == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }

        EntTramite vlo_EnTramite = this.ObtenerTramite(pvc_ReferenciaInterna);
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteDesalmacenaje();
        String vlc_Justificacion = this.NormalizarTexto(pvo_Cambio.getJustificacion());

        if (vlc_Justificacion == null
                || vlc_Justificacion.length() < Constantes.LONGITUD_MINIMA_JUSTIFICACION_CAMBIO
                || vlc_Justificacion.length() > Constantes.LONGITUD_MAXIMA_JUSTIFICACION_CAMBIO) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_JUSTIFICACION_CAMBIO_REQUERIDA);
        }

        EntUsuario vlo_EnUsuario = this.ObtenerUsuarioResponsable(pvo_Cambio.getIdUsuarioResponsable());
        EntEstadoTramite vlo_EnEstadoNuevo = this.ObtenerEstadoDestino(pvo_Cambio, vlo_EnTipoTramite.getIdTipoTramite());

        Integer vln_IdEstadoAnterior = vlo_EnTramite.getIdEstadoTramite();
        if (vlo_EnEstadoNuevo.getIdEstadoTramite().equals(vln_IdEstadoAnterior)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_SIN_CAMBIO);
        }

        vlo_EnTramite.setIdEstadoTramite(vlo_EnEstadoNuevo.getIdEstadoTramite());
        this.vgo_DalTramite.SIMT_TRAMITE_ModificarRegistro(vlo_EnTramite);

        EntHistorialEstado vlo_EnHistorial = new EntHistorialEstado();
        vlo_EnHistorial.setIdTramite(vlo_EnTramite.getIdTramite());
        vlo_EnHistorial.setIdEstadoAnterior(vln_IdEstadoAnterior);
        vlo_EnHistorial.setIdEstadoNuevo(vlo_EnEstadoNuevo.getIdEstadoTramite());
        vlo_EnHistorial.setIdUsuario(vlo_EnUsuario.getIdUsuario());
        vlo_EnHistorial.setJustificacion(vlc_Justificacion);
        this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_EnHistorial);

        EntEstadoTramite vlo_EnEstadoAnterior =
                this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(vln_IdEstadoAnterior);
        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_CAMBIO_ESTADO,
                Constantes.ENTIDAD_BITACORA_TRAMITE,
                vlo_EnTramite.getIdTramite(),
                "Estado: " + (vlo_EnEstadoAnterior == null ? "sin estado" : vlo_EnEstadoAnterior.getNombre()) + ".",
                "Estado: " + vlo_EnEstadoNuevo.getNombre() + ". Justificación: " + vlc_Justificacion);

        return this.ArmarExpedienteDetalle(vlo_EnTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene el estado destino de un cambio y comprueba que esté catalogado, vigente
     *   y que pertenezca al tipo de trámite del expediente. Admite que la pantalla lo indique por
     *   su nombre, como aparece en la lista desplegable, o por su identificador.
     *
     * @param pvo_Cambio datos del cambio de estado
     * @param pvn_IdTipoTramite tipo de trámite del expediente
     * @return estado destino
     */
    private EntEstadoTramite ObtenerEstadoDestino(EntCambioEstado pvo_Cambio, Integer pvn_IdTipoTramite) {
        String vlc_NombreEstado = this.NormalizarTexto(pvo_Cambio.getNuevoEstado());
        EntEstadoTramite vlo_EnEstado;
        if (vlc_NombreEstado != null && !vlc_NombreEstado.isEmpty()) {
            vlo_EnEstado = this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                    pvn_IdTipoTramite, vlc_NombreEstado, Constantes.ESTADO_CATALOGO_ACTIVO);
        } else {
            vlo_EnEstado = pvo_Cambio.getIdEstadoNuevo() == null
                    ? null
                    : this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(pvo_Cambio.getIdEstadoNuevo());
        }
        boolean vlb_EstadoValido = vlo_EnEstado != null
                && pvn_IdTipoTramite.equals(vlo_EnEstado.getIdTipoTramite())
                && Constantes.ESTADO_CATALOGO_ACTIVO.equals(vlo_EnEstado.getEstado());
        if (!vlb_EstadoValido) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_NUEVO_INVALIDO);
        }
        return vlo_EnEstado;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene el registro documental de un archivo del expediente para su descarga.
     *
     * @param pvn_IdDocumento identificador del documento
     * @return documento encontrado
     */
    @Transactional(readOnly = true)
    public EntDocumento ObtenerDocumento(Integer pvn_IdDocumento) {
        EntDocumento vlo_EnDocumento = this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(pvn_IdDocumento);
        if (vlo_EnDocumento == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DOCUMENTO_NO_DISPONIBLE);
        }
        return vlo_EnDocumento;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Lee el contenido de un archivo del expediente.
     *
     * @param pvo_EnDocumento registro documental del archivo
     * @return contenido del archivo
     */
    public byte[] ObtenerContenidoDocumento(EntDocumento pvo_EnDocumento) {
        return this.vgo_AlmacenamientoDocumentos.LeerArchivo(pvo_EnDocumento.getRutaArchivo());
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Reúne el trámite, sus datos específicos, su documentación y su historial en
     *   un solo expediente.
     *
     * @param pvo_EnTramite trámite ya recuperado
     * @return expediente completo
     */
    private EntExpedienteDetalle ArmarExpedienteDetalle(EntTramite pvo_EnTramite) {
        EntDesalmacenajeEspecial vlo_EnDesalmacenaje = this.vgo_DalDesalmacenajeEspecial
                .SIMT_DESALMACENAJE_ESPEC_ObtenerRegistro(pvo_EnTramite.getIdTramite());
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteDesalmacenaje();
        EntEstadoTramite vlo_EnEstado = this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerRegistro(pvo_EnTramite.getIdEstadoTramite());
        EntMoneda vlo_EnMoneda = this.vgo_DalMoneda
                .SIMM_MONEDA_ObtenerRegistro(pvo_EnTramite.getIdMoneda());
        EntPais vlo_EnPais = this.vgo_DalPais
                .SIMM_PAIS_ObtenerRegistro(pvo_EnTramite.getIdPaisOrigen());
        EntUsuario vlo_EnUsuario = this.vgo_DalUsuario
                .SIMF_USUARIO_ObtenerRegistro(pvo_EnTramite.getIdUsuarioResponsable());

        EntExpedienteDetalle vlo_Detalle = new EntExpedienteDetalle();
        vlo_Detalle.setReferenciaInterna(pvo_EnTramite.getReferenciaInterna());
        vlo_Detalle.setTipoTramite(vlo_EnTipoTramite.getNombre());
        vlo_Detalle.setRequiereSicop(vlo_EnTipoTramite.getRequiereSicop());
        vlo_Detalle.setRequiereOrdenCompra(vlo_EnTipoTramite.getRequiereOrdenCompra());
        vlo_Detalle.setEstado(vlo_EnEstado == null ? null : vlo_EnEstado.getNombre());
        vlo_Detalle.setMoneda(vlo_EnMoneda == null ? null : vlo_EnMoneda.getCodigoIso());
        vlo_Detalle.setPaisOrigen(vlo_EnPais == null ? null : vlo_EnPais.getNombre());
        vlo_Detalle.setUsuarioResponsable(vlo_EnUsuario == null ? null : vlo_EnUsuario.getNombreCompleto());
        vlo_Detalle.setValorEstimado(pvo_EnTramite.getValorEstimado());
        vlo_Detalle.setDescripcion(pvo_EnTramite.getDescripcion());
        vlo_Detalle.setRegimenAduanero(pvo_EnTramite.getRegimenAduanero());
        vlo_Detalle.setAduanaControl(pvo_EnTramite.getAduanaControl());
        vlo_Detalle.setObservaciones(pvo_EnTramite.getObservaciones());
        vlo_Detalle.setTipoPartidoResponsable(pvo_EnTramite.getTipoPartidoResponsable());
        vlo_Detalle.setPartidoResponsable(pvo_EnTramite.getPartidoResponsable());
        vlo_Detalle.setImpuestosPagados(pvo_EnTramite.getImpuestosPagados());
        vlo_Detalle.setImpuestosExonerados(pvo_EnTramite.getImpuestosExonerados());
        vlo_Detalle.setMontoBodegaje(pvo_EnTramite.getMontoBodegaje());
        vlo_Detalle.setFechaCreacion(pvo_EnTramite.getFechaCreacion());
        if (vlo_EnDesalmacenaje != null) {
            vlo_Detalle.setOrigenDependencia(vlo_EnDesalmacenaje.getOrigenDependencia());
            vlo_Detalle.setJustificacion(vlo_EnDesalmacenaje.getJustificacion());
            vlo_Detalle.setEstadoDocumental(vlo_EnDesalmacenaje.getEstadoDocumental());
            vlo_Detalle.setObservacionesDocumentales(
                    vlo_EnDesalmacenaje.getObservacionesDocumentales());
        }
        vlo_Detalle.setDocumentos(
                this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerListaExpediente(pvo_EnTramite.getIdTramite()));
        vlo_Detalle.setHistorial(
                this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_ObtenerPorTramite(pvo_EnTramite.getIdTramite()));
        vlo_Detalle.setBitacora(this.vgo_BllBitacora.ObtenerPorTramite(pvo_EnTramite.getIdTramite()));
        return vlo_Detalle;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Recupera un expediente por su referencia interna.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @return trámite encontrado
     */
    private EntTramite ObtenerTramite(String pvc_ReferenciaInterna) {
        String vlc_Referencia = this.NormalizarReferencia(pvc_ReferenciaInterna);
        if (vlc_Referencia == null || vlc_Referencia.isBlank()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXPEDIENTE_NO_ENCONTRADO);
        }
        EntTramite vlo_EnTramite = this.vgo_DalTramite
                .SIMT_TRAMITE_ObtenerPorReferenciaInterna(vlc_Referencia);
        if (vlo_EnTramite == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXPEDIENTE_NO_ENCONTRADO);
        }
        return vlo_EnTramite;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Registra el primer movimiento del historial, con estado anterior nulo, para
     *   dejar constancia de cuándo y quién dejó el expediente en su estado inicial.
     *
     * @param pvo_EnTramite expediente ya persistido
     * @param pvo_EnEstadoInicial estado con el que nace el expediente
     * @param pvn_IdUsuario usuario responsable del registro
     */
    private void AbrirHistorialEstado(
            EntTramite pvo_EnTramite,
            EntEstadoTramite pvo_EnEstadoInicial,
            Integer pvn_IdUsuario) {
        EntHistorialEstado vlo_EnHistorial = new EntHistorialEstado();
        vlo_EnHistorial.setIdTramite(pvo_EnTramite.getIdTramite());
        vlo_EnHistorial.setIdEstadoAnterior(null);
        vlo_EnHistorial.setIdEstadoNuevo(pvo_EnEstadoInicial.getIdEstadoTramite());
        vlo_EnHistorial.setIdUsuario(pvn_IdUsuario);
        vlo_EnHistorial.setJustificacion(Constantes.JUSTIFICACION_REGISTRO_INICIAL);
        this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_EnHistorial);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene el tipo de trámite de desalmacenaje especial y comprueba que el catálogo
     *   lo mantenga exento de contrato SICOP y de orden de compra, según la regla de la HU-031.
     *
     * @return tipo de trámite de desalmacenaje especial
     */
    private EntTipoTramite ObtenerTipoTramiteDesalmacenaje() {
        EntTipoTramite vlo_EnTipoTramite = this.vgo_DalTipoTramite
                .SIMM_TIPO_TRAMITE_ObtenerPorCodigo(Constantes.CODIGO_TIPO_TRAMITE_DESALMACENAJE);
        if (vlo_EnTipoTramite == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_TRAMITE_NO_CONFIGURADO);
        }
        boolean vlb_ExigeDocumentoContractual =
                Boolean.TRUE.equals(vlo_EnTipoTramite.getRequiereSicop())
                        || Boolean.TRUE.equals(vlo_EnTipoTramite.getRequiereOrdenCompra());
        if (vlb_ExigeDocumentoContractual) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_TRAMITE_NO_CONFIGURADO);
        }
        return vlo_EnTipoTramite;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Comprueba el formato de la referencia interna, que lleve el prefijo propio del
     *   desalmacenaje especial y que no haya sido utilizada por otro trámite.
     *
     * @param pvc_ReferenciaInterna referencia interna normalizada
     * @param pvo_EnTipoTramite tipo de trámite que define el prefijo
     */
    private void ValidarReferenciaInterna(String pvc_ReferenciaInterna, EntTipoTramite pvo_EnTipoTramite) {
        if (pvc_ReferenciaInterna == null || pvc_ReferenciaInterna.isBlank()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_INVALIDA);
        }
        if (!pvc_ReferenciaInterna.startsWith(pvo_EnTipoTramite.getPrefijoReferencia())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_INVALIDA);
        }
        if (!pvc_ReferenciaInterna.matches(Constantes.EXPRESION_REFERENCIA_DESALMACENAJE)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_INVALIDA);
        }
        if (this.vgo_DalTramite.SIMT_TRAMITE_ExistePorReferenciaInterna(pvc_ReferenciaInterna)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_DUPLICADA);
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Comprueba que se indique el origen o la dependencia responsable del trámite.
     *
     * @param pvc_OrigenDependencia origen o dependencia normalizada
     */
    private void ValidarOrigenDependencia(String pvc_OrigenDependencia) {
        if (pvc_OrigenDependencia == null
                || pvc_OrigenDependencia.isBlank()
                || pvc_OrigenDependencia.length() > Constantes.LONGITUD_MAXIMA_ORIGEN_DEPENDENCIA) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ORIGEN_DEPENDENCIA_REQUERIDO);
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Comprueba que el valor estimado de la mercancía sea mayor que cero.
     *
     * @param pvn_ValorEstimado valor declarado en el formulario
     */
    private void ValidarValorEstimado(BigDecimal pvn_ValorEstimado) {
        if (pvn_ValorEstimado == null || pvn_ValorEstimado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_VALOR_ESTIMADO_INVALIDO);
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Comprueba la longitud de la justificación del trámite.
     *
     * @param pvc_Justificacion justificación normalizada
     */
    private void ValidarJustificacion(String pvc_Justificacion) {
        if (pvc_Justificacion == null
                || pvc_Justificacion.length() < Constantes.LONGITUD_MINIMA_JUSTIFICACION
                || pvc_Justificacion.length() > Constantes.LONGITUD_MAXIMA_JUSTIFICACION) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_JUSTIFICACION_INVALIDA);
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Comprueba que el expediente reciba al menos un documento de respaldo. La HU-031
     *   admite documentación atípica, por lo que no se restringe el formato del archivo.
     *
     * @param pvo_LsRespaldo documentos de respaldo depurados
     */
    private void ValidarDocumentosRespaldo(List<MultipartFile> pvo_LsRespaldo) {
        // La documentación puede quedar pendiente: al abrir el expediente muchas veces solo se
        // cuenta con el correo de la guía. El expediente informa después lo que falta adjuntar.
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene el usuario responsable al que queda asociado el expediente.
     *
     * @param pvn_IdUsuarioResponsable identificador del usuario responsable
     * @return usuario responsable registrado
     */
    private EntUsuario ObtenerUsuarioResponsable(Integer pvn_IdUsuarioResponsable) {
        if (pvn_IdUsuarioResponsable == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_USUARIO_RESPONSABLE_INVALIDO);
        }
        EntUsuario vlo_EnUsuario = this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(pvn_IdUsuarioResponsable);
        if (vlo_EnUsuario == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_USUARIO_RESPONSABLE_INVALIDO);
        }
        return vlo_EnUsuario;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene la moneda en la que se declara el valor estimado.
     *
     * @param pvc_Moneda código ISO de la moneda
     * @return moneda registrada
     */
    private EntMoneda ObtenerMoneda(String pvc_Moneda) {
        String vlc_CodigoIso = this.NormalizarTexto(pvc_Moneda);
        if (vlc_CodigoIso == null || vlc_CodigoIso.isBlank()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_MONEDA_INVALIDA);
        }
        EntMoneda vlo_EnMoneda = this.vgo_DalMoneda
                .SIMM_MONEDA_ObtenerPorCodigoIso(vlc_CodigoIso.toUpperCase(Locale.ROOT));
        if (vlo_EnMoneda == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_MONEDA_INVALIDA);
        }
        return vlo_EnMoneda;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene el país de origen cuando la persona usuaria lo indica. El campo es
     *   opcional porque la mercancía puede encontrarse ya en el país.
     *
     * @param pvc_PaisOrigen nombre del país de origen
     * @return país registrado o null cuando no aplica
     */
    private EntPais ObtenerPaisOrigen(String pvc_PaisOrigen) {
        String vlc_Nombre = this.NormalizarTexto(pvc_PaisOrigen);
        if (vlc_Nombre == null || vlc_Nombre.isBlank() || "Otro".equalsIgnoreCase(vlc_Nombre)) {
            return null;
        }
        EntPais vlo_EnPais = this.vgo_DalPais.SIMM_PAIS_ObtenerPorNombre(vlc_Nombre);
        if (vlo_EnPais == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PAIS_INVALIDO);
        }
        return vlo_EnPais;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene el estado con el que nace el expediente, definido en el catálogo.
     *
     * @param pvn_IdTipoTramite identificador del tipo de trámite
     * @return estado inicial del expediente
     */
    private EntEstadoTramite ObtenerEstadoInicial(Integer pvn_IdTipoTramite) {
        EntEstadoTramite vlo_EnEstado = this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerEstadoInicial(pvn_IdTipoTramite);
        if (vlo_EnEstado == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_INICIAL_NO_CONFIGURADO);
        }
        return vlo_EnEstado;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Almacena los archivos del expediente y registra cada uno en SIMF_DOCUMENTO.
     *   Las rutas escritas se acumulan para poder borrarlas si la transacción no se confirma.
     *
     * @param pvo_EnTramite expediente ya persistido
     * @param pvn_IdUsuarioCarga usuario que adjunta la documentación
     * @param pvo_LsRespaldo documentos de respaldo
     * @param pvo_PackingList lista de empaque, opcional
     * @param pvo_Factura factura o documento equivalente, opcional
     * @param pvo_DocumentoEmbarque documento de embarque, opcional
     * @return cantidad de documentos agregados al expediente
     */
    private int RegistrarDocumentos(
            EntTramite pvo_EnTramite,
            Integer pvn_IdUsuarioCarga,
            List<MultipartFile> pvo_LsRespaldo,
            MultipartFile pvo_PackingList,
            MultipartFile pvo_Factura,
            MultipartFile pvo_DocumentoEmbarque) {

        List<String> vlo_LsRutasEscritas = new ArrayList<>();
        this.RegistrarLimpiezaSiFallaLaTransaccion(vlo_LsRutasEscritas);

        int vln_Cantidad = 0;
        for (MultipartFile vlo_Archivo : pvo_LsRespaldo) {
            this.GuardarDocumento(pvo_EnTramite, pvn_IdUsuarioCarga, vlo_Archivo,
                    Constantes.CODIGO_TIPO_DOCUMENTO_RESPALDO, vlo_LsRutasEscritas);
            vln_Cantidad++;
        }
        vln_Cantidad += this.GuardarDocumentoOpcional(pvo_EnTramite, pvn_IdUsuarioCarga, pvo_PackingList,
                Constantes.CODIGO_TIPO_DOCUMENTO_PACKING_LIST, vlo_LsRutasEscritas);
        vln_Cantidad += this.GuardarDocumentoOpcional(pvo_EnTramite, pvn_IdUsuarioCarga, pvo_Factura,
                Constantes.CODIGO_TIPO_DOCUMENTO_FACTURA, vlo_LsRutasEscritas);
        vln_Cantidad += this.GuardarDocumentoOpcional(pvo_EnTramite, pvn_IdUsuarioCarga, pvo_DocumentoEmbarque,
                Constantes.CODIGO_TIPO_DOCUMENTO_EMBARQUE, vlo_LsRutasEscritas);
        return vln_Cantidad;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Programa el borrado de los archivos escritos si la transacción termina en
     *   rollback, de manera que no queden archivos sin registro en la base de datos.
     *
     * @param pvo_LsRutasEscritas rutas relativas escritas durante la transacción
     */
    private void RegistrarLimpiezaSiFallaLaTransaccion(List<String> pvo_LsRutasEscritas) {
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

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Registra un documento opcional cuando la persona usuaria lo adjunta.
     *
     * @param pvo_EnTramite expediente ya persistido
     * @param pvn_IdUsuarioCarga usuario que adjunta la documentación
     * @param pvo_Archivo archivo recibido, puede venir vacío
     * @param pvc_CodigoTipoDocumento código del tipo de documento
     * @param pvo_LsRutasEscritas rutas escritas durante la transacción
     * @return 1 si el documento se registró, 0 si no venía adjunto
     */
    private int GuardarDocumentoOpcional(
            EntTramite pvo_EnTramite,
            Integer pvn_IdUsuarioCarga,
            MultipartFile pvo_Archivo,
            String pvc_CodigoTipoDocumento,
            List<String> pvo_LsRutasEscritas) {
        if (pvo_Archivo == null || pvo_Archivo.isEmpty()) {
            return 0;
        }
        this.GuardarDocumento(pvo_EnTramite, pvn_IdUsuarioCarga, pvo_Archivo,
                pvc_CodigoTipoDocumento, pvo_LsRutasEscritas);
        return 1;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Guarda el archivo en disco y persiste su registro documental. Si el expediente
     *   ya tiene un documento vigente del mismo tipo y nombre, el nuevo se registra como la
     *   versión siguiente y el anterior queda inactivo, sin perderse del expediente.
     *
     * @param pvo_EnTramite expediente ya persistido
     * @param pvn_IdUsuarioCarga usuario que adjunta la documentación
     * @param pvo_Archivo archivo recibido
     * @param pvc_CodigoTipoDocumento código del tipo de documento
     * @param pvo_LsRutasEscritas rutas escritas durante la transacción
     */
    private void GuardarDocumento(
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
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Descarta los archivos vacíos que envían los campos de carga sin selección.
     *
     * @param pvo_LsArchivos archivos recibidos en la petición
     * @return lista sin archivos vacíos
     */
    private List<MultipartFile> DepurarArchivos(List<MultipartFile> pvo_LsArchivos) {
        List<MultipartFile> vlo_LsDepurados = new ArrayList<>();
        if (pvo_LsArchivos == null) {
            return vlo_LsDepurados;
        }
        for (MultipartFile vlo_Archivo : pvo_LsArchivos) {
            if (vlo_Archivo != null && !vlo_Archivo.isEmpty()) {
                vlo_LsDepurados.add(vlo_Archivo);
            }
        }
        return vlo_LsDepurados;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Normaliza la referencia interna a mayúsculas y sin espacios sobrantes.
     *
     * @param pvc_ReferenciaInterna referencia recibida
     * @return referencia normalizada o null
     */
    private String NormalizarReferencia(String pvc_ReferenciaInterna) {
        String vlc_Referencia = this.NormalizarTexto(pvc_ReferenciaInterna);
        return vlc_Referencia == null ? null : vlc_Referencia.toUpperCase(Locale.ROOT);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Elimina los espacios sobrantes de un texto recibido del formulario.
     *
     * @param pvc_Texto texto recibido
     * @return texto sin espacios en los extremos o null
     */
    private String NormalizarTexto(String pvc_Texto) {
        return pvc_Texto == null ? null : pvc_Texto.trim();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 25/09/2026
     * Descripción: Clasifica un documento del expediente según su tipo (HU-045). El archivo se
     *   conserva igual y el cambio queda anotado en la bitácora con su responsable y su fecha.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvn_IdDocumento documento que se clasifica
     * @param pvc_CodigoTipoDocumento código del tipo de documento del catálogo
     * @param pvn_IdUsuario usuario que realiza la acción
     * @return expediente con su documentación actualizada
     */
    @Transactional
    public EntExpedienteDetalle ClasificarDocumento(
            String pvc_ReferenciaInterna,
            Integer pvn_IdDocumento,
            String pvc_CodigoTipoDocumento,
            Integer pvn_IdUsuario) {

        EntTramite vlo_EnTramite = this.ObtenerTramite(pvc_ReferenciaInterna);
        this.vgo_BllDocumento.ClasificarDocumento(
                vlo_EnTramite.getIdTramite(), pvn_IdDocumento, pvc_CodigoTipoDocumento, pvn_IdUsuario);
        return this.ArmarExpedienteDetalle(vlo_EnTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 25/09/2026
     * Descripción: Guarda si el expediente está documentalmente completo o incompleto, con las
     *   observaciones de la persona analista (HU-045). El estado anterior queda en la bitácora,
     *   de modo que se sabe quién lo determinó y cuándo.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvo_Estado estado documental y observaciones
     * @return expediente con su estado documental actualizado
     */
    @Transactional
    public EntExpedienteDetalle GuardarEstadoDocumental(
            String pvc_ReferenciaInterna,
            EntEstadoDocumental pvo_Estado) {

        if (pvo_Estado == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }

        String vlc_Estado = pvo_Estado.getEstadoDocumental() == null
                ? null
                : pvo_Estado.getEstadoDocumental().trim().toUpperCase();
        if (!Constantes.ESTADO_DOCUMENTAL_COMPLETO.equals(vlc_Estado)
                && !Constantes.ESTADO_DOCUMENTAL_INCOMPLETO.equals(vlc_Estado)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_DOCUMENTAL_INVALIDO);
        }

        String vlc_Observaciones = pvo_Estado.getObservaciones() == null
                ? null
                : pvo_Estado.getObservaciones().trim();
        if (vlc_Observaciones != null
                && vlc_Observaciones.length() > Constantes.LONGITUD_MAXIMA_OBSERVACIONES_DOCUMENTALES) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_OBSERVACIONES_DOCUMENTALES_INVALIDAS);
        }

        EntTramite vlo_EnTramite = this.ObtenerTramite(pvc_ReferenciaInterna);
        EntUsuario vlo_EnUsuario = this.ObtenerUsuarioResponsable(pvo_Estado.getIdUsuarioResponsable());
        EntDesalmacenajeEspecial vlo_EnDesalmacenaje = this.vgo_DalDesalmacenajeEspecial
                .SIMT_DESALMACENAJE_ESPEC_ObtenerRegistro(vlo_EnTramite.getIdTramite());
        if (vlo_EnDesalmacenaje == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXPEDIENTE_NO_ENCONTRADO);
        }

        String vlc_EstadoAnterior = vlo_EnDesalmacenaje.getEstadoDocumental();
        vlo_EnDesalmacenaje.setEstadoDocumental(vlc_Estado);
        vlo_EnDesalmacenaje.setObservacionesDocumentales(
                vlc_Observaciones == null || vlc_Observaciones.isEmpty() ? null : vlc_Observaciones);
        this.vgo_DalDesalmacenajeEspecial.SIMT_DESALMACENAJE_ESPEC_ModificarRegistro(vlo_EnDesalmacenaje);

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_ESTADO_DOCUMENTAL,
                Constantes.ENTIDAD_BITACORA_TRAMITE,
                vlo_EnTramite.getIdTramite(),
                "Estado documental: "
                        + (vlc_EstadoAnterior == null ? "sin definir" : vlc_EstadoAnterior) + ".",
                "Estado documental: " + vlc_Estado + "."
                        + (vlo_EnDesalmacenaje.getObservacionesDocumentales() == null
                                ? ""
                                : " Observaciones: " + vlo_EnDesalmacenaje.getObservacionesDocumentales()));

        return this.ArmarExpedienteDetalle(vlo_EnTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Corrige los datos ya guardados del expediente de desalmacenaje especial
     *   (HU-032): la dependencia, la justificación, el país, la moneda, el valor, la
     *   descripción, el régimen aduanero, la aduana de control y las observaciones. Solo se
     *   tocan los datos que vienen indicados, y cada cambio queda anotado en la bitácora con su
     *   valor anterior, su responsable y su fecha.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvo_Correccion datos corregidos y usuario responsable
     * @return expediente con sus datos actualizados
     */
    @Transactional
    public EntExpedienteDetalle ModificarDatos(
            String pvc_ReferenciaInterna,
            EntCorreccionDesalmacenaje pvo_Correccion) {

        if (pvo_Correccion == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }

        EntTramite vlo_EnTramite = this.ObtenerTramite(pvc_ReferenciaInterna);
        EntUsuario vlo_EnUsuario = this.ObtenerUsuarioResponsable(pvo_Correccion.getIdUsuarioResponsable());
        EntDesalmacenajeEspecial vlo_EnDesalmacenaje = this.vgo_DalDesalmacenajeEspecial
                .SIMT_DESALMACENAJE_ESPEC_ObtenerRegistro(vlo_EnTramite.getIdTramite());
        if (vlo_EnDesalmacenaje == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXPEDIENTE_NO_ENCONTRADO);
        }

        List<String> vlo_LsAnteriores = new ArrayList<>();
        List<String> vlo_LsCambios = new ArrayList<>();
        boolean vlb_CambiaDesalmacenaje = false;
        boolean vlb_CambiaTramite = false;

        String vlc_Dependencia = this.NormalizarTextoCorreccion(pvo_Correccion.getOrigenDependencia());
        if (vlc_Dependencia != null && !vlc_Dependencia.equals(vlo_EnDesalmacenaje.getOrigenDependencia())) {
            if (vlc_Dependencia.isEmpty()
                    || vlc_Dependencia.length() > Constantes.LONGITUD_MAXIMA_ORIGEN_DEPENDENCIA) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_ORIGEN_DEPENDENCIA_REQUERIDO);
            }
            vlo_LsAnteriores.add("Dependencia: " + vlo_EnDesalmacenaje.getOrigenDependencia());
            vlo_LsCambios.add("Dependencia: " + vlc_Dependencia);
            vlo_EnDesalmacenaje.setOrigenDependencia(vlc_Dependencia);
            vlb_CambiaDesalmacenaje = true;
        }

        String vlc_Justificacion = this.NormalizarTextoCorreccion(pvo_Correccion.getJustificacion());
        if (vlc_Justificacion != null && !vlc_Justificacion.equals(vlo_EnDesalmacenaje.getJustificacion())) {
            if (vlc_Justificacion.length() < Constantes.LONGITUD_MINIMA_JUSTIFICACION
                    || vlc_Justificacion.length() > Constantes.LONGITUD_MAXIMA_JUSTIFICACION) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_JUSTIFICACION_INVALIDA);
            }
            vlo_LsAnteriores.add("Justificación: " + vlo_EnDesalmacenaje.getJustificacion());
            vlo_LsCambios.add("Justificación: " + vlc_Justificacion);
            vlo_EnDesalmacenaje.setJustificacion(vlc_Justificacion);
            vlb_CambiaDesalmacenaje = true;
        }

        String vlc_Pais = this.NormalizarTextoCorreccion(pvo_Correccion.getPaisOrigen());
        if (vlc_Pais != null) {
            EntPais vlo_EnPaisActual = this.vgo_DalPais
                    .SIMM_PAIS_ObtenerRegistro(vlo_EnTramite.getIdPaisOrigen());
            String vlc_Actual = vlo_EnPaisActual == null ? "" : vlo_EnPaisActual.getNombre();
            if (!vlc_Pais.equals(vlc_Actual)) {
                Integer vln_IdPais = null;
                if (!vlc_Pais.isEmpty()) {
                    EntPais vlo_EnPais = this.vgo_DalPais.SIMM_PAIS_ObtenerPorNombre(vlc_Pais);
                    if (vlo_EnPais == null) {
                        throw new ExcepcionNegocio(Constantes.MENSAJE_PAIS_INVALIDO);
                    }
                    vln_IdPais = vlo_EnPais.getIdPais();
                }
                vlo_LsAnteriores.add("País de origen: " + this.DescribirDato(vlc_Actual));
                vlo_LsCambios.add("País de origen: " + this.DescribirDato(vlc_Pais));
                vlo_EnTramite.setIdPaisOrigen(vln_IdPais);
                vlb_CambiaTramite = true;
            }
        }

        String vlc_Moneda = this.NormalizarTextoCorreccion(pvo_Correccion.getMoneda());
        if (vlc_Moneda != null && !vlc_Moneda.isEmpty()) {
            EntMoneda vlo_EnMonedaActual = this.vgo_DalMoneda
                    .SIMM_MONEDA_ObtenerRegistro(vlo_EnTramite.getIdMoneda());
            String vlc_Actual = vlo_EnMonedaActual == null ? "" : vlo_EnMonedaActual.getCodigoIso();
            if (!vlc_Moneda.equalsIgnoreCase(vlc_Actual)) {
                EntMoneda vlo_EnMoneda = this.vgo_DalMoneda
                        .SIMM_MONEDA_ObtenerPorCodigoIso(vlc_Moneda.toUpperCase());
                if (vlo_EnMoneda == null) {
                    throw new ExcepcionNegocio(Constantes.MENSAJE_MONEDA_INVALIDA);
                }
                vlo_LsAnteriores.add("Moneda: " + this.DescribirDato(vlc_Actual));
                vlo_LsCambios.add("Moneda: " + vlo_EnMoneda.getCodigoIso());
                vlo_EnTramite.setIdMoneda(vlo_EnMoneda.getIdMoneda());
                vlb_CambiaTramite = true;
            }
        }

        if (pvo_Correccion.getValorEstimado() != null
                && (vlo_EnTramite.getValorEstimado() == null
                        || pvo_Correccion.getValorEstimado().compareTo(vlo_EnTramite.getValorEstimado()) != 0)) {
            if (pvo_Correccion.getValorEstimado().signum() <= 0) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_VALOR_ESTIMADO_INVALIDO);
            }
            vlo_LsAnteriores.add("Valor estimado: " + this.DescribirDato(
                    vlo_EnTramite.getValorEstimado() == null
                            ? "" : vlo_EnTramite.getValorEstimado().toPlainString()));
            vlo_LsCambios.add("Valor estimado: " + pvo_Correccion.getValorEstimado().toPlainString());
            vlo_EnTramite.setValorEstimado(pvo_Correccion.getValorEstimado());
            vlb_CambiaTramite = true;
        }

        vlb_CambiaTramite = this.CorregirTextoDelTramite("Descripción",
                pvo_Correccion.getDescripcion(), vlo_EnTramite.getDescripcion(),
                vlo_EnTramite::setDescripcion, vlo_LsAnteriores, vlo_LsCambios) || vlb_CambiaTramite;
        vlb_CambiaTramite = this.CorregirTextoDelTramite("Régimen aduanero",
                pvo_Correccion.getRegimenAduanero(), vlo_EnTramite.getRegimenAduanero(),
                vlo_EnTramite::setRegimenAduanero, vlo_LsAnteriores, vlo_LsCambios) || vlb_CambiaTramite;
        vlb_CambiaTramite = this.CorregirTextoDelTramite("Aduana de control",
                pvo_Correccion.getAduanaControl(), vlo_EnTramite.getAduanaControl(),
                vlo_EnTramite::setAduanaControl, vlo_LsAnteriores, vlo_LsCambios) || vlb_CambiaTramite;
        vlb_CambiaTramite = this.CorregirTextoDelTramite("Observaciones",
                pvo_Correccion.getObservaciones(), vlo_EnTramite.getObservaciones(),
                vlo_EnTramite::setObservaciones, vlo_LsAnteriores, vlo_LsCambios) || vlb_CambiaTramite;

        if (vlo_LsCambios.isEmpty()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_DESALMACENAJE_SIN_CAMBIO);
        }
        if (vlb_CambiaDesalmacenaje) {
            this.vgo_DalDesalmacenajeEspecial.SIMT_DESALMACENAJE_ESPEC_ModificarRegistro(vlo_EnDesalmacenaje);
        }
        if (vlb_CambiaTramite) {
            this.vgo_DalTramite.SIMT_TRAMITE_ModificarRegistro(vlo_EnTramite);
        }

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_DATOS_MODIFICADOS,
                Constantes.ENTIDAD_BITACORA_TRAMITE,
                vlo_EnTramite.getIdTramite(),
                String.join(". ", vlo_LsAnteriores) + ".",
                String.join(". ", vlo_LsCambios) + ".");

        return this.ArmarExpedienteDetalle(vlo_EnTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Corrige un dato de texto del trámite y lo anota para la bitácora cuando
     *   cambia. Un texto vacío borra el dato, porque son datos opcionales del expediente.
     *
     * @param pvc_Etiqueta nombre del dato tal como se lee en la bitácora
     * @param pvc_Nuevo valor digitado, puede venir nulo si no se tocó
     * @param pvc_Actual valor que tiene el expediente
     * @param pvo_Asignador operación que guarda el valor nuevo
     * @param pvo_LsAnteriores lista donde se anota el valor anterior
     * @param pvo_LsCambios lista donde se anota el valor nuevo
     * @return true si el dato cambió
     */
    private boolean CorregirTextoDelTramite(
            String pvc_Etiqueta,
            String pvc_Nuevo,
            String pvc_Actual,
            java.util.function.Consumer<String> pvo_Asignador,
            List<String> pvo_LsAnteriores,
            List<String> pvo_LsCambios) {

        String vlc_Nuevo = this.NormalizarTextoCorreccion(pvc_Nuevo);
        if (vlc_Nuevo == null || vlc_Nuevo.equals(pvc_Actual == null ? "" : pvc_Actual)) {
            return false;
        }
        pvo_LsAnteriores.add(pvc_Etiqueta + ": " + this.DescribirDato(pvc_Actual));
        pvo_LsCambios.add(pvc_Etiqueta + ": " + this.DescribirDato(vlc_Nuevo));
        pvo_Asignador.accept(vlc_Nuevo.isEmpty() ? null : vlc_Nuevo);
        return true;
    }

    private String NormalizarTextoCorreccion(String pvc_Texto) {
        return pvc_Texto == null ? null : pvc_Texto.trim();
    }

    private String DescribirDato(String pvc_Dato) {
        return pvc_Dato == null || pvc_Dato.isEmpty() ? "sin dato" : pvc_Dato;
    }
}
