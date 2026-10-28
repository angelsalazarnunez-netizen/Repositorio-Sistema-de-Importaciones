package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalDonacion;
import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalMoneda;
import cr.ac.ucr.sim.accesodatos.tramites.DalPais;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCambioEstadoDonacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDonacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExpedienteDonacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMoneda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntPais;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroDonacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaDonacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenDonacion;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 10/09/2026
 * Descripción: Reglas de negocio del registro de donaciones (HU-024). La donación puede ser
 *   nacional, internacional o por medio de la Fundación UCR, no exige contrato SICOP ni orden de
 *   compra, y solo se registra cuando se adjunta la resolución o acta que la acepta.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 15/09/2026
 *   Descripción: se agrega la gestión de donaciones (HU-044): consulta del expediente, lista de
 *     expedientes, catálogo de estados, cambio de estado con historial y documentación adicional.
 *     El registro pasa a abrir el historial de estados, a versionar la documentación y a borrar
 *     los archivos escritos cuando la transacción termina en rollback.
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 15/09/2026
 *   Descripción: el registro, el cambio de estado y la documentación agregada quedan también en
 *     la bitácora del expediente, con el mismo mecanismo que utiliza la HU-018.
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: se permite retirar un documento del expediente mientras siga abierto, con la
 *     regla común de BllDocumento que usan el desalmacenaje y la exoneración.
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: FUNDEVI es el nombre anterior de la Fundación UCR, por lo que el tipo de donación
 *     pasa a llamarse Fundación UCR. La referencia usa las siglas de la Unidad: D- para la
 *     donación y DF- para la donación por medio de la Fundación UCR.
 */
@Service
public class BllDonacion {

    private final DalTramite vgo_DalTramite;
    private final DalDonacion vgo_DalDonacion;
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

    public BllDonacion(
            DalTramite pvo_DalTramite,
            DalDonacion pvo_DalDonacion,
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
        this.vgo_DalDonacion = pvo_DalDonacion;
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
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Crea el expediente de la donación, le asigna el estado inicial Pendiente y lo
     *   asocia al usuario responsable. La resolución o acta de aceptación se registra antes que la
     *   donación porque SIMT_DONACION la referencia de forma obligatoria.
     *
     * @param pvo_Registro datos del formulario de registro
     * @param pvo_ResolucionAceptacion resolución o acta de aceptación de la donación, obligatoria
     * @param pvo_LsRespaldo documentos de respaldo adicionales, opcionales
     * @param pvo_PackingList lista de empaque, opcional
     * @param pvo_Factura factura o documento equivalente, opcional
     * @param pvo_DocumentoEmbarque documento de embarque, opcional
     * @return confirmación del registro con la referencia interna y el estado asignados
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 15/09/2026
     *   Descripción: se abre el historial de estados del expediente (HU-044).
     */
    @Transactional
    public EntRespuestaDonacion InsertarRegistro(
            EntRegistroDonacion pvo_Registro,
            MultipartFile pvo_ResolucionAceptacion,
            List<MultipartFile> pvo_LsRespaldo,
            MultipartFile pvo_PackingList,
            MultipartFile pvo_Factura,
            MultipartFile pvo_DocumentoEmbarque) {

        if (pvo_Registro == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }

        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteDonacion();
        String vlc_ReferenciaInterna = this.NormalizarReferencia(pvo_Registro.getReferenciaInterna());
        String vlc_TipoDonacion = this.NormalizarTipoDonacion(pvo_Registro.getTipoDonacion());
        String vlc_NombreDonante = this.NormalizarTexto(pvo_Registro.getNombreDonante());
        String vlc_ReferenciaFundacionUcr = this.NormalizarTexto(pvo_Registro.getReferenciaFundacionUcr());
        String vlc_Observaciones = this.NormalizarTexto(pvo_Registro.getObservaciones());
        List<MultipartFile> vlo_LsRespaldo = this.DepurarArchivos(pvo_LsRespaldo);

        this.ValidarTipoDonacion(vlc_TipoDonacion);
        this.ValidarReferenciaInterna(vlc_ReferenciaInterna, vlo_EnTipoTramite, vlc_TipoDonacion);
        this.ValidarNombreDonante(vlc_NombreDonante);
        this.ValidarValorEstimado(pvo_Registro.getValorEstimado());
        this.ValidarResolucionAceptacion(pvo_ResolucionAceptacion);
        this.ValidarReferenciaFundacionUcr(vlc_ReferenciaFundacionUcr, vlc_TipoDonacion);
        this.ValidarObservaciones(vlc_Observaciones);

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
        vlo_EnTramite.setObservaciones(vlc_Observaciones);
        this.vgo_DalTramite.SIMT_TRAMITE_InsertarRegistro(vlo_EnTramite);

        List<String> vlo_LsRutasEscritas = new ArrayList<>();
        this.RegistrarLimpiezaSiFallaLaTransaccion(vlo_LsRutasEscritas);

        EntDocumento vlo_EnResolucion = this.GuardarDocumento(
                vlo_EnTramite,
                vlo_EnUsuario.getIdUsuario(),
                pvo_ResolucionAceptacion,
                Constantes.CODIGO_TIPO_DOCUMENTO_RESOLUCION_ACTA,
                vlo_LsRutasEscritas);

        EntDonacion vlo_EnDonacion = new EntDonacion();
        vlo_EnDonacion.setIdTramite(vlo_EnTramite.getIdTramite());
        vlo_EnDonacion.setIdDocumentoAceptacion(vlo_EnResolucion.getIdDocumento());
        vlo_EnDonacion.setTipoDonacion(vlc_TipoDonacion);
        vlo_EnDonacion.setNombreDonante(vlc_NombreDonante);
        vlo_EnDonacion.setReferenciaFundacionUcr(vlc_ReferenciaFundacionUcr);
        this.vgo_DalDonacion.SIMT_DONACION_InsertarRegistro(vlo_EnDonacion);

        this.RegistrarMovimientoEstado(
                vlo_EnTramite,
                null,
                vlo_EnEstadoInicial,
                vlo_EnUsuario.getIdUsuario(),
                Constantes.JUSTIFICACION_REGISTRO_INICIAL);

        List<EntDocumento> vlo_LsDocumentos = this.RegistrarDocumentos(
                vlo_EnTramite,
                vlo_EnUsuario.getIdUsuario(),
                null,
                vlo_LsRespaldo,
                pvo_PackingList,
                pvo_Factura,
                pvo_DocumentoEmbarque,
                vlo_LsRutasEscritas);
        int vln_CantidadDocumentos = 1 + vlo_LsDocumentos.size();

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_REGISTRO,
                Constantes.ENTIDAD_BITACORA_DONACION,
                vlo_EnTramite.getIdTramite(),
                null,
                this.DescribirRegistro(vlo_EnDonacion, vlo_EnEstadoInicial, vln_CantidadDocumentos));

        EntRespuestaDonacion vlo_Respuesta = new EntRespuestaDonacion();
        vlo_Respuesta.setMensaje(Constantes.MENSAJE_REGISTRO_DONACION_EXITOSO);
        vlo_Respuesta.setIdTramite(vlo_EnTramite.getIdTramite());
        vlo_Respuesta.setReferenciaInterna(vlo_EnTramite.getReferenciaInterna());
        vlo_Respuesta.setTipoDonacion(vlo_EnDonacion.getTipoDonacion());
        vlo_Respuesta.setEstado(vlo_EnEstadoInicial.getNombre());
        vlo_Respuesta.setUsuarioResponsable(vlo_EnUsuario.getNombreCompleto());
        vlo_Respuesta.setCantidadDocumentos(vln_CantidadDocumentos);
        vlo_Respuesta.setFechaCreacion(vlo_EnTramite.getFechaCreacion());
        return vlo_Respuesta;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Obtiene los expedientes de donación registrados, para la pantalla de gestión
     *   desde la que se consulta y se da seguimiento a cada trámite (HU-044).
     *
     * @return expedientes del más reciente al más antiguo
     */
    @Transactional(readOnly = true)
    public List<EntResumenDonacion> ObtenerListaExpedientes() {
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteDonacion();
        return this.vgo_DalTramite.SIMT_TRAMITE_ObtenerListaDonaciones(
                vlo_EnTipoTramite.getIdTipoTramite(),
                Constantes.ESTADO_DOCUMENTO_ACTIVO);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Obtiene un expediente de donación completo, con su estado actual, su
     *   documentación y su historial de estados (HU-044).
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @return expediente completo
     */
    @Transactional(readOnly = true)
    public EntExpedienteDonacion ObtenerExpediente(String pvc_ReferenciaInterna) {
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteDonacion();
        EntTramite vlo_EnTramite = this.ObtenerTramiteDonacion(pvc_ReferenciaInterna, vlo_EnTipoTramite);
        return this.ArmarExpedienteDonacion(vlo_EnTramite, vlo_EnTipoTramite);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Obtiene los estados que el catálogo define para los trámites de donación, para
     *   que la pantalla de gestión ofrezca las opciones del catálogo y no una lista fija (HU-044).
     *
     * @return estados activos del tipo de trámite de donación
     */
    @Transactional(readOnly = true)
    public List<EntEstadoTramite> ObtenerEstadosDisponibles() {
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteDonacion();
        return this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerPorTipoTramite(
                        vlo_EnTipoTramite.getIdTipoTramite(), Constantes.ESTADO_CATALOGO_ACTIVO);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Actualiza el estado del expediente de donación (HU-044). El movimiento se
     *   agrega al historial antes de mover el estado del expediente, de manera que el estado
     *   anterior queda registrado con su fecha, su hora y el usuario responsable. El historial
     *   nunca se modifica ni se elimina, solo se le agregan movimientos.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvo_Cambio nuevo estado, justificación, observaciones y usuario responsable
     * @return expediente actualizado con su documentación y su historial
     */
    @Transactional
    public EntExpedienteDonacion ActualizarEstado(
            String pvc_ReferenciaInterna,
            EntCambioEstadoDonacion pvo_Cambio) {

        if (pvo_Cambio == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }

        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteDonacion();
        EntTramite vlo_EnTramite = this.ObtenerTramiteDonacion(pvc_ReferenciaInterna, vlo_EnTipoTramite);

        String vlc_Justificacion = this.NormalizarTexto(pvo_Cambio.getJustificacion());
        String vlc_Observaciones = this.NormalizarTexto(pvo_Cambio.getObservaciones());
        this.ValidarJustificacionEstado(vlc_Justificacion);
        this.ValidarObservaciones(vlc_Observaciones);

        EntUsuario vlo_EnUsuario = this.ObtenerUsuarioResponsable(pvo_Cambio.getIdUsuarioResponsable());
        EntEstadoTramite vlo_EnEstadoActual = this.ObtenerEstadoActual(vlo_EnTramite);
        this.ValidarExpedienteAbierto(vlo_EnEstadoActual);

        EntEstadoTramite vlo_EnEstadoNuevo = this.ObtenerEstadoDestino(
                vlo_EnTipoTramite.getIdTipoTramite(),
                this.NormalizarTexto(pvo_Cambio.getNuevoEstado()));
        if (vlo_EnEstadoNuevo.getIdEstadoTramite().equals(vlo_EnEstadoActual.getIdEstadoTramite())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_SIN_CAMBIO);
        }

        this.RegistrarMovimientoEstado(
                vlo_EnTramite,
                vlo_EnEstadoActual.getIdEstadoTramite(),
                vlo_EnEstadoNuevo,
                vlo_EnUsuario.getIdUsuario(),
                vlc_Justificacion);

        vlo_EnTramite.setIdEstadoTramite(vlo_EnEstadoNuevo.getIdEstadoTramite());
        if (vlc_Observaciones != null && !vlc_Observaciones.isBlank()) {
            vlo_EnTramite.setObservaciones(vlc_Observaciones);
        }
        if (Boolean.TRUE.equals(vlo_EnEstadoNuevo.getEsFinal())) {
            vlo_EnTramite.setFechaCierre(LocalDate.now());
        }
        this.vgo_DalTramite.SIMT_TRAMITE_ModificarRegistro(vlo_EnTramite);

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_CAMBIO_ESTADO,
                Constantes.ENTIDAD_BITACORA_TRAMITE,
                vlo_EnTramite.getIdTramite(),
                this.DescribirEstado(vlo_EnEstadoActual),
                this.DescribirCambioEstado(vlo_EnEstadoNuevo, vlc_Justificacion));

        return this.ArmarExpedienteDonacion(vlo_EnTramite, vlo_EnTipoTramite);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Agrega documentación de respaldo a un expediente de donación ya registrado
     *   (HU-044). La documentación anterior no se elimina: si el archivo repite tipo y nombre, el
     *   nuevo se guarda como la versión siguiente y el anterior queda inactivo en el expediente.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvn_IdUsuarioCarga usuario que adjunta la documentación
     * @param pvo_ResolucionAceptacion resolución o acta de aceptación posterior, si aplica
     * @param pvo_LsRespaldo documentos de respaldo adicionales
     * @param pvo_PackingList lista de empaque, opcional
     * @param pvo_Factura factura o documento equivalente, opcional
     * @param pvo_DocumentoEmbarque documento de embarque, opcional
     * @return expediente actualizado con toda su documentación
     */
    @Transactional
    public EntExpedienteDonacion InsertarDocumentos(
            String pvc_ReferenciaInterna,
            Integer pvn_IdUsuarioCarga,
            MultipartFile pvo_ResolucionAceptacion,
            List<MultipartFile> pvo_LsRespaldo,
            MultipartFile pvo_PackingList,
            MultipartFile pvo_Factura,
            MultipartFile pvo_DocumentoEmbarque) {

        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteDonacion();
        EntTramite vlo_EnTramite = this.ObtenerTramiteDonacion(pvc_ReferenciaInterna, vlo_EnTipoTramite);
        this.ValidarExpedienteAbierto(this.ObtenerEstadoActual(vlo_EnTramite));

        EntUsuario vlo_EnUsuario = this.ObtenerUsuarioResponsable(pvn_IdUsuarioCarga);
        List<MultipartFile> vlo_LsRespaldo = this.DepurarArchivos(pvo_LsRespaldo);

        List<String> vlo_LsRutasEscritas = new ArrayList<>();
        this.RegistrarLimpiezaSiFallaLaTransaccion(vlo_LsRutasEscritas);

        List<EntDocumento> vlo_LsDocumentos = this.RegistrarDocumentos(
                vlo_EnTramite,
                vlo_EnUsuario.getIdUsuario(),
                pvo_ResolucionAceptacion,
                vlo_LsRespaldo,
                pvo_PackingList,
                pvo_Factura,
                pvo_DocumentoEmbarque,
                vlo_LsRutasEscritas);

        if (vlo_LsDocumentos.isEmpty()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_SIN_DOCUMENTOS_ADJUNTOS);
        }

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_DOCUMENTO_AGREGADO,
                Constantes.ENTIDAD_BITACORA_DOCUMENTO,
                vlo_LsDocumentos.size() == 1 ? vlo_LsDocumentos.get(0).getIdDocumento() : null,
                null,
                this.DescribirDocumentosAgregados(vlo_LsDocumentos));

        return this.ArmarExpedienteDonacion(vlo_EnTramite, vlo_EnTipoTramite);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 03/10/2026
     * Descripción: Retira del expediente de donación un documento cargado por error. Solo se
     *   permite mientras el expediente siga abierto: uno finalizado o cancelado ya no admite
     *   cambios. El archivo se conserva como inactivo y la eliminación queda en la bitácora.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvn_IdDocumento documento que se retira
     * @param pvn_IdUsuario usuario que realiza la acción
     * @return expediente actualizado con su documentación
     */
    @Transactional
    public EntExpedienteDonacion EliminarDocumento(
            String pvc_ReferenciaInterna,
            Integer pvn_IdDocumento,
            Integer pvn_IdUsuario) {

        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteDonacion();
        EntTramite vlo_EnTramite = this.ObtenerTramiteDonacion(pvc_ReferenciaInterna, vlo_EnTipoTramite);
        this.ValidarExpedienteAbierto(this.ObtenerEstadoActual(vlo_EnTramite));

        this.vgo_BllDocumento.EliminarDocumento(vlo_EnTramite.getIdTramite(), pvn_IdDocumento, pvn_IdUsuario);
        return this.ArmarExpedienteDonacion(vlo_EnTramite, vlo_EnTipoTramite);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Obtiene el registro documental de un archivo de un expediente de donación para
     *   su descarga.
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
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteDonacion();
        EntTramite vlo_EnTramite = this.vgo_DalTramite
                .SIMT_TRAMITE_ObtenerRegistro(vlo_EnDocumento.getIdTramite());
        if (vlo_EnTramite == null
                || !vlo_EnTipoTramite.getIdTipoTramite().equals(vlo_EnTramite.getIdTipoTramite())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DOCUMENTO_NO_DISPONIBLE);
        }
        return vlo_EnDocumento;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Lee el contenido de un archivo del expediente.
     *
     * @param pvo_EnDocumento registro documental del archivo
     * @return contenido del archivo
     */
    public byte[] ObtenerContenidoDocumento(EntDocumento pvo_EnDocumento) {
        return this.vgo_AlmacenamientoDocumentos.LeerArchivo(pvo_EnDocumento.getRutaArchivo());
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Reúne el trámite, los datos propios de la donación, su documentación y su
     *   historial de estados en un solo expediente.
     *
     * @param pvo_EnTramite trámite ya recuperado
     * @param pvo_EnTipoTramite tipo de trámite de donación
     * @return expediente completo
     */
    private EntExpedienteDonacion ArmarExpedienteDonacion(
            EntTramite pvo_EnTramite,
            EntTipoTramite pvo_EnTipoTramite) {

        EntDonacion vlo_EnDonacion = this.vgo_DalDonacion
                .SIMT_DONACION_ObtenerRegistro(pvo_EnTramite.getIdTramite());
        EntEstadoTramite vlo_EnEstado = this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerRegistro(pvo_EnTramite.getIdEstadoTramite());
        EntMoneda vlo_EnMoneda = this.vgo_DalMoneda
                .SIMM_MONEDA_ObtenerRegistro(pvo_EnTramite.getIdMoneda());
        EntPais vlo_EnPais = this.vgo_DalPais
                .SIMM_PAIS_ObtenerRegistro(pvo_EnTramite.getIdPaisOrigen());
        EntUsuario vlo_EnUsuario = this.vgo_DalUsuario
                .SIMF_USUARIO_ObtenerRegistro(pvo_EnTramite.getIdUsuarioResponsable());

        EntExpedienteDonacion vlo_Detalle = new EntExpedienteDonacion();
        vlo_Detalle.setReferenciaInterna(pvo_EnTramite.getReferenciaInterna());
        vlo_Detalle.setTipoTramite(pvo_EnTipoTramite.getNombre());
        vlo_Detalle.setEstado(vlo_EnEstado == null ? null : vlo_EnEstado.getNombre());
        vlo_Detalle.setEstadoFinal(vlo_EnEstado == null ? null : vlo_EnEstado.getEsFinal());
        vlo_Detalle.setMoneda(vlo_EnMoneda == null ? null : vlo_EnMoneda.getCodigoIso());
        vlo_Detalle.setPaisOrigen(vlo_EnPais == null ? null : vlo_EnPais.getNombre());
        vlo_Detalle.setUsuarioResponsable(vlo_EnUsuario == null ? null : vlo_EnUsuario.getNombreCompleto());
        vlo_Detalle.setValorEstimado(pvo_EnTramite.getValorEstimado());
        vlo_Detalle.setObservaciones(pvo_EnTramite.getObservaciones());
        vlo_Detalle.setFechaCreacion(pvo_EnTramite.getFechaCreacion());
        vlo_Detalle.setFechaCierre(pvo_EnTramite.getFechaCierre());
        if (vlo_EnDonacion != null) {
            vlo_Detalle.setTipoDonacion(vlo_EnDonacion.getTipoDonacion());
            vlo_Detalle.setNombreDonante(vlo_EnDonacion.getNombreDonante());
            vlo_Detalle.setReferenciaFundacionUcr(vlo_EnDonacion.getReferenciaFundacionUcr());
        }
        vlo_Detalle.setDocumentos(
                this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerListaExpediente(pvo_EnTramite.getIdTramite()));
        vlo_Detalle.setHistorial(
                this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_ObtenerPorTramite(pvo_EnTramite.getIdTramite()));
        return vlo_Detalle;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Recupera un expediente de donación por su referencia interna. La referencia es
     *   única en todo el sistema, por lo que se comprueba además que el expediente encontrado sea
     *   del tipo de trámite de donación.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvo_EnTipoTramite tipo de trámite de donación
     * @return trámite encontrado
     */
    private EntTramite ObtenerTramiteDonacion(
            String pvc_ReferenciaInterna,
            EntTipoTramite pvo_EnTipoTramite) {
        String vlc_Referencia = this.NormalizarReferencia(pvc_ReferenciaInterna);
        if (vlc_Referencia == null || vlc_Referencia.isBlank()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXPEDIENTE_DONACION_NO_ENCONTRADO);
        }
        EntTramite vlo_EnTramite = this.vgo_DalTramite
                .SIMT_TRAMITE_ObtenerPorReferenciaInterna(vlc_Referencia);
        if (vlo_EnTramite == null
                || !pvo_EnTipoTramite.getIdTipoTramite().equals(vlo_EnTramite.getIdTipoTramite())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXPEDIENTE_DONACION_NO_ENCONTRADO);
        }
        return vlo_EnTramite;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Agrega un movimiento al historial de estados del expediente. El movimiento
     *   inicial se registra con estado anterior nulo, para dejar constancia de cuándo y quién dejó
     *   el expediente en su estado de partida.
     *
     * @param pvo_EnTramite expediente ya persistido
     * @param pvn_IdEstadoAnterior estado que deja el expediente, nulo en el registro inicial
     * @param pvo_EnEstadoNuevo estado que toma el expediente
     * @param pvn_IdUsuario usuario responsable del movimiento
     * @param pvc_Justificacion justificación del movimiento
     */
    private void RegistrarMovimientoEstado(
            EntTramite pvo_EnTramite,
            Integer pvn_IdEstadoAnterior,
            EntEstadoTramite pvo_EnEstadoNuevo,
            Integer pvn_IdUsuario,
            String pvc_Justificacion) {
        EntHistorialEstado vlo_EnHistorial = new EntHistorialEstado();
        vlo_EnHistorial.setIdTramite(pvo_EnTramite.getIdTramite());
        vlo_EnHistorial.setIdEstadoAnterior(pvn_IdEstadoAnterior);
        vlo_EnHistorial.setIdEstadoNuevo(pvo_EnEstadoNuevo.getIdEstadoTramite());
        vlo_EnHistorial.setIdUsuario(pvn_IdUsuario);
        vlo_EnHistorial.setJustificacion(pvc_Justificacion);
        this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_EnHistorial);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Describe el registro de la donación para la bitácora del expediente.
     *
     * @param pvo_EnDonacion datos propios de la donación ya persistidos
     * @param pvo_EnEstadoInicial estado con el que nace el expediente
     * @param pvn_CantidadDocumentos documentos asociados al registro
     * @return descripción de la acción
     */
    private String DescribirRegistro(
            EntDonacion pvo_EnDonacion,
            EntEstadoTramite pvo_EnEstadoInicial,
            int pvn_CantidadDocumentos) {
        return "Donación " + pvo_EnDonacion.getTipoDonacion()
                + " de " + pvo_EnDonacion.getNombreDonante()
                + ". Estado inicial: " + pvo_EnEstadoInicial.getNombre()
                + ". Documentos asociados: " + pvn_CantidadDocumentos + ".";
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Describe el estado que deja el expediente, para el dato anterior de la bitácora.
     *
     * @param pvo_EnEstado estado del expediente
     * @return descripción del estado
     */
    private String DescribirEstado(EntEstadoTramite pvo_EnEstado) {
        return "Estado: " + pvo_EnEstado.getNombre() + ".";
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Describe el estado que toma el expediente junto con la justificación del
     *   movimiento, para el dato nuevo de la bitácora.
     *
     * @param pvo_EnEstado estado que toma el expediente
     * @param pvc_Justificacion justificación del movimiento
     * @return descripción del cambio
     */
    private String DescribirCambioEstado(EntEstadoTramite pvo_EnEstado, String pvc_Justificacion) {
        return "Estado: " + pvo_EnEstado.getNombre()
                + ". Justificación: " + pvc_Justificacion;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Describe la documentación agregada al expediente, con la versión de cada
     *   archivo, para la bitácora.
     *
     * @param pvo_LsDocumentos documentos agregados en la operación
     * @return descripción de la acción
     */
    private String DescribirDocumentosAgregados(List<EntDocumento> pvo_LsDocumentos) {
        StringBuilder vlo_Descripcion = new StringBuilder("Documentos agregados: ");
        for (int vln_Indice = 0; vln_Indice < pvo_LsDocumentos.size(); vln_Indice++) {
            EntDocumento vlo_EnDocumento = pvo_LsDocumentos.get(vln_Indice);
            if (vln_Indice > 0) {
                vlo_Descripcion.append(", ");
            }
            vlo_Descripcion.append(vlo_EnDocumento.getNombreArchivo())
                    .append(" (versión ")
                    .append(vlo_EnDocumento.getVersion())
                    .append(")");
        }
        return vlo_Descripcion.append(".").toString();
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Obtiene el estado en el que se encuentra el expediente.
     *
     * @param pvo_EnTramite expediente ya recuperado
     * @return estado actual del expediente
     */
    private EntEstadoTramite ObtenerEstadoActual(EntTramite pvo_EnTramite) {
        EntEstadoTramite vlo_EnEstado = this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerRegistro(pvo_EnTramite.getIdEstadoTramite());
        if (vlo_EnEstado == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_ACTUAL_NO_DISPONIBLE);
        }
        return vlo_EnEstado;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Obtiene el estado al que se quiere mover el expediente. El estado debe estar
     *   definido en el catálogo para el tipo de trámite de donación.
     *
     * @param pvn_IdTipoTramite identificador del tipo de trámite de donación
     * @param pvc_Nombre nombre del estado solicitado
     * @return estado solicitado
     */
    private EntEstadoTramite ObtenerEstadoDestino(Integer pvn_IdTipoTramite, String pvc_Nombre) {
        if (pvc_Nombre == null || pvc_Nombre.isBlank()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_DONACION_INVALIDO);
        }
        EntEstadoTramite vlo_EnEstado = this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                        pvn_IdTipoTramite, pvc_Nombre, Constantes.ESTADO_CATALOGO_ACTIVO);
        if (vlo_EnEstado == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_DONACION_INVALIDO);
        }
        return vlo_EnEstado;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Comprueba que el expediente no esté cerrado. Un expediente que llegó a un
     *   estado final conserva su información y su historial, pero ya no admite cambios.
     *
     * @param pvo_EnEstadoActual estado en el que se encuentra el expediente
     */
    private void ValidarExpedienteAbierto(EntEstadoTramite pvo_EnEstadoActual) {
        if (Boolean.TRUE.equals(pvo_EnEstadoActual.getEsFinal())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXPEDIENTE_DONACION_CERRADO);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Comprueba que el cambio de estado venga justificado, porque la justificación es
     *   lo que explica el movimiento dentro del historial del expediente.
     *
     * @param pvc_Justificacion justificación normalizada
     */
    private void ValidarJustificacionEstado(String pvc_Justificacion) {
        if (pvc_Justificacion == null
                || pvc_Justificacion.isBlank()
                || pvc_Justificacion.length() > Constantes.LONGITUD_MAXIMA_JUSTIFICACION_CAMBIO) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_JUSTIFICACION_ESTADO_INVALIDA);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Obtiene el tipo de trámite de donación y comprueba que el catálogo lo mantenga
     *   exento de contrato SICOP y de orden de compra, según la regla de la HU-024.
     *
     * @return tipo de trámite de donación
     */
    private EntTipoTramite ObtenerTipoTramiteDonacion() {
        EntTipoTramite vlo_EnTipoTramite = this.vgo_DalTipoTramite
                .SIMM_TIPO_TRAMITE_ObtenerPorCodigo(Constantes.CODIGO_TIPO_TRAMITE_DONACION);
        if (vlo_EnTipoTramite == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_TRAMITE_DONACION_NO_CONFIGURADO);
        }
        boolean vlb_ExigeDocumentoContractual =
                Boolean.TRUE.equals(vlo_EnTipoTramite.getRequiereSicop())
                        || Boolean.TRUE.equals(vlo_EnTipoTramite.getRequiereOrdenCompra());
        if (vlb_ExigeDocumentoContractual) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_TRAMITE_DONACION_NO_CONFIGURADO);
        }
        return vlo_EnTipoTramite;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Comprueba que la referencia interna lleve la sigla que le corresponde según el
     *   tipo de donación y que no haya sido utilizada por otro trámite. El formato del consecutivo
     *   no se restringe porque la HU-024 no lo define.
     *
     * @param pvc_ReferenciaInterna referencia interna normalizada
     * @param pvo_EnTipoTramite tipo de trámite que define el prefijo de la donación
     * @param pvc_TipoDonacion tipo de donación ya validado
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 03/10/2026
     *   Descripción: la donación por medio de la Fundación UCR usa la sigla DF- y las demás la del
     *     catálogo (D-), según las siglas que utiliza la Unidad.
     */
    private void ValidarReferenciaInterna(
            String pvc_ReferenciaInterna,
            EntTipoTramite pvo_EnTipoTramite,
            String pvc_TipoDonacion) {
        if (pvc_ReferenciaInterna == null || pvc_ReferenciaInterna.isBlank()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_DONACION_INVALIDA);
        }
        String vlc_Prefijo = Constantes.TIPO_DONACION_FUNDACION_UCR.equals(pvc_TipoDonacion)
                ? Constantes.PREFIJO_REFERENCIA_DONACION_FUNDACION_UCR
                : pvo_EnTipoTramite.getPrefijoReferencia();
        if (!pvc_ReferenciaInterna.startsWith(vlc_Prefijo)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_DONACION_INVALIDA);
        }
        if (pvc_ReferenciaInterna.length() == vlc_Prefijo.length()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_DONACION_INVALIDA);
        }
        if (this.vgo_DalTramite.SIMT_TRAMITE_ExistePorReferenciaInterna(pvc_ReferenciaInterna)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_DUPLICADA);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Comprueba que el tipo de donación sea uno de los admitidos por la HU-024.
     *
     * @param pvc_TipoDonacion tipo de donación normalizado
     */
    private void ValidarTipoDonacion(String pvc_TipoDonacion) {
        boolean vlb_TipoAdmitido = Constantes.TIPO_DONACION_NACIONAL.equals(pvc_TipoDonacion)
                || Constantes.TIPO_DONACION_INTERNACIONAL.equals(pvc_TipoDonacion)
                || Constantes.TIPO_DONACION_FUNDACION_UCR.equals(pvc_TipoDonacion);
        if (!vlb_TipoAdmitido) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_DONACION_INVALIDO);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Comprueba que se indique la persona o entidad donante.
     *
     * @param pvc_NombreDonante nombre del donante normalizado
     */
    private void ValidarNombreDonante(String pvc_NombreDonante) {
        if (pvc_NombreDonante == null
                || pvc_NombreDonante.isBlank()
                || pvc_NombreDonante.length() > Constantes.LONGITUD_MAXIMA_NOMBRE_DONANTE) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DONANTE_REQUERIDO);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Comprueba que el valor estimado de la donación sea mayor que cero.
     *
     * @param pvn_ValorEstimado valor declarado en el formulario
     */
    private void ValidarValorEstimado(BigDecimal pvn_ValorEstimado) {
        if (pvn_ValorEstimado == null || pvn_ValorEstimado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_VALOR_ESTIMADO_INVALIDO);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Comprueba que se adjunte la resolución o acta de aceptación, obligatoria para
     *   toda donación según la HU-024.
     *
     * @param pvo_ResolucionAceptacion archivo recibido en la petición
     */
    private void ValidarResolucionAceptacion(MultipartFile pvo_ResolucionAceptacion) {
        if (pvo_ResolucionAceptacion == null || pvo_ResolucionAceptacion.isEmpty()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_RESOLUCION_ACEPTACION_REQUERIDA);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Comprueba la referencia de la Fundación UCR. Es opcional, pero solo tiene
     *   sentido en las donaciones por medio de la Fundación UCR.
     *
     * @param pvc_ReferenciaFundacionUcr referencia de la Fundación UCR normalizada
     * @param pvc_TipoDonacion tipo de donación normalizado
     */
    private void ValidarReferenciaFundacionUcr(String pvc_ReferenciaFundacionUcr, String pvc_TipoDonacion) {
        if (pvc_ReferenciaFundacionUcr == null || pvc_ReferenciaFundacionUcr.isBlank()) {
            return;
        }
        if (!Constantes.TIPO_DONACION_FUNDACION_UCR.equals(pvc_TipoDonacion)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_FUNDACION_UCR_NO_APLICA);
        }
        if (pvc_ReferenciaFundacionUcr.length() > Constantes.LONGITUD_MAXIMA_REFERENCIA_FUNDACION_UCR) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_FUNDACION_UCR_INVALIDA);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Comprueba la longitud de las observaciones del expediente.
     *
     * @param pvc_Observaciones observaciones normalizadas
     */
    private void ValidarObservaciones(String pvc_Observaciones) {
        if (pvc_Observaciones != null
                && pvc_Observaciones.length() > Constantes.LONGITUD_MAXIMA_OBSERVACIONES) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_OBSERVACIONES_INVALIDAS);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
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
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Obtiene la moneda en la que se declara el valor estimado. La HU-024 no define
     *   una moneda predeterminada, por lo que debe venir indicada en el formulario.
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
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Obtiene el país de origen cuando la persona usuaria lo indica. El campo es
     *   opcional porque la donación puede ser nacional.
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
     * Autor: Josue Guevara Lostalo
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
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_INICIAL_DONACION_NO_CONFIGURADO);
        }
        return vlo_EnEstado;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Registra la documentación del expediente: resolución o acta posterior,
     *   respaldos, lista de empaque, factura y documento de embarque. Las rutas escritas se
     *   acumulan para poder borrarlas si la transacción no se confirma.
     *
     * @param pvo_EnTramite expediente ya persistido
     * @param pvn_IdUsuarioCarga usuario que adjunta la documentación
     * @param pvo_ResolucionAceptacion resolución o acta de aceptación, opcional en esta lista
     * @param pvo_LsRespaldo documentos de respaldo
     * @param pvo_PackingList lista de empaque, opcional
     * @param pvo_Factura factura o documento equivalente, opcional
     * @param pvo_DocumentoEmbarque documento de embarque, opcional
     * @param pvo_LsRutasEscritas rutas escritas durante la transacción
     * @return documentos agregados al expediente
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 15/09/2026
     *   Descripción: recibe la resolución o acta para la documentación posterior al registro, y
     *     devuelve los documentos agregados para describirlos en la bitácora (HU-044).
     */
    private List<EntDocumento> RegistrarDocumentos(
            EntTramite pvo_EnTramite,
            Integer pvn_IdUsuarioCarga,
            MultipartFile pvo_ResolucionAceptacion,
            List<MultipartFile> pvo_LsRespaldo,
            MultipartFile pvo_PackingList,
            MultipartFile pvo_Factura,
            MultipartFile pvo_DocumentoEmbarque,
            List<String> pvo_LsRutasEscritas) {

        List<EntDocumento> vlo_LsDocumentos = new ArrayList<>();
        this.AgregarDocumentoOpcional(vlo_LsDocumentos, pvo_EnTramite, pvn_IdUsuarioCarga,
                pvo_ResolucionAceptacion, Constantes.CODIGO_TIPO_DOCUMENTO_RESOLUCION_ACTA,
                pvo_LsRutasEscritas);
        for (MultipartFile vlo_Archivo : pvo_LsRespaldo) {
            vlo_LsDocumentos.add(this.GuardarDocumento(pvo_EnTramite, pvn_IdUsuarioCarga, vlo_Archivo,
                    Constantes.CODIGO_TIPO_DOCUMENTO_RESPALDO, pvo_LsRutasEscritas));
        }
        this.AgregarDocumentoOpcional(vlo_LsDocumentos, pvo_EnTramite, pvn_IdUsuarioCarga, pvo_PackingList,
                Constantes.CODIGO_TIPO_DOCUMENTO_PACKING_LIST, pvo_LsRutasEscritas);
        this.AgregarDocumentoOpcional(vlo_LsDocumentos, pvo_EnTramite, pvn_IdUsuarioCarga, pvo_Factura,
                Constantes.CODIGO_TIPO_DOCUMENTO_FACTURA, pvo_LsRutasEscritas);
        this.AgregarDocumentoOpcional(vlo_LsDocumentos, pvo_EnTramite, pvn_IdUsuarioCarga, pvo_DocumentoEmbarque,
                Constantes.CODIGO_TIPO_DOCUMENTO_EMBARQUE, pvo_LsRutasEscritas);
        return vlo_LsDocumentos;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
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
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Registra un documento opcional cuando la persona usuaria lo adjunta y lo agrega
     *   a la lista de documentos de la operación.
     *
     * @param pvo_LsDocumentos documentos agregados hasta el momento
     * @param pvo_EnTramite expediente ya persistido
     * @param pvn_IdUsuarioCarga usuario que adjunta la documentación
     * @param pvo_Archivo archivo recibido, puede venir vacío
     * @param pvc_CodigoTipoDocumento código del tipo de documento
     * @param pvo_LsRutasEscritas rutas escritas durante la transacción
     */
    private void AgregarDocumentoOpcional(
            List<EntDocumento> pvo_LsDocumentos,
            EntTramite pvo_EnTramite,
            Integer pvn_IdUsuarioCarga,
            MultipartFile pvo_Archivo,
            String pvc_CodigoTipoDocumento,
            List<String> pvo_LsRutasEscritas) {
        if (pvo_Archivo == null || pvo_Archivo.isEmpty()) {
            return;
        }
        pvo_LsDocumentos.add(this.GuardarDocumento(pvo_EnTramite, pvn_IdUsuarioCarga, pvo_Archivo,
                pvc_CodigoTipoDocumento, pvo_LsRutasEscritas));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Guarda el archivo en disco y persiste su registro documental. Si el expediente
     *   ya tiene un documento vigente del mismo tipo y nombre, el nuevo se registra como la
     *   versión siguiente y el anterior queda inactivo, sin perderse del expediente. Devuelve el
     *   documento persistido porque la donación referencia la resolución de aceptación.
     *
     * @param pvo_EnTramite expediente ya persistido
     * @param pvn_IdUsuarioCarga usuario que adjunta la documentación
     * @param pvo_Archivo archivo recibido
     * @param pvc_CodigoTipoDocumento código del tipo de documento
     * @param pvo_LsRutasEscritas rutas escritas durante la transacción
     * @return documento registrado en SIMF_DOCUMENTO
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 15/09/2026
     *   Descripción: se versiona el documento y se acumula la ruta escrita, para no perder la
     *     documentación anterior del expediente ni dejar archivos huérfanos (HU-044).
     */
    private EntDocumento GuardarDocumento(
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
     * Autor: Josue Guevara Lostalo
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
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Normaliza el tipo de donación a mayúsculas para compararlo con los valores
     *   admitidos por el catálogo y por la restricción de la tabla.
     *
     * @param pvc_TipoDonacion tipo recibido
     * @return tipo normalizado o null
     */
    private String NormalizarTipoDonacion(String pvc_TipoDonacion) {
        String vlc_Tipo = this.NormalizarTexto(pvc_TipoDonacion);
        return vlc_Tipo == null ? null : vlc_Tipo.toUpperCase(Locale.ROOT);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Elimina los espacios sobrantes de un texto recibido del formulario.
     *
     * @param pvc_Texto texto recibido
     * @return texto sin espacios en los extremos o null
     */
    private String NormalizarTexto(String pvc_Texto) {
        return pvc_Texto == null ? null : pvc_Texto.trim();
    }
}
