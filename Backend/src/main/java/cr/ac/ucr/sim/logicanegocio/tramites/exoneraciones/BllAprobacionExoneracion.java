package cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalAutorizacion;
import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalExoneracion;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.accesodatos.tramites.exoneraciones.DalDecisionExoneracion;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntDecisionExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntNotificacionExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntObservacionExpediente;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionAutenticacion;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.logicanegocio.tramites.BllBitacora;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.Permisos;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloAprobacionPendienteRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloAprobacionRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloControlDocumentalRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloDecisionRespuesta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Aprobación de solicitudes de exoneración (HU-022).
 *   Reglas de negocio:
 *   - Solo decide quien tenga el permiso EXONERACION_APROBAR; a cualquier otro usuario se le
 *     rechaza la acción con 403. El permiso se consulta en el servidor, no se confía en la pantalla.
 *   - Solo se decide sobre un expediente en "Pendiente de aprobación", al que llega con la
 *     documentación obligatoria completa (HU-021). Al aprobar se vuelve a evaluar la documentación:
 *     un expediente incompleto no se aprueba.
 *   - Aprobar lo pasa a "Exoneración aprobada"; rechazar, a "Exoneración rechazada", que cierra el
 *     expediente; solicitar modificación, a "Pendiente de modificación", desde donde la técnica lo
 *     corrige y lo reenvía. La justificación es obligatoria al rechazar o pedir modificación.
 *   - Cada decisión se conserva con su fecha, su hora y el usuario responsable; queda en el
 *     historial de estados y en la bitácora, y se notifica a la técnica que envió el expediente a
 *     aprobación. Si el correo no sale, la decisión igual queda registrada y la notificación queda
 *     marcada con error.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: el código del permiso se toma de Permisos.EXONERACION_APROBAR, única fuente de
 *     códigos de permiso (integración de restricciones por rol, HU-003).
 */
@Service
public class BllAprobacionExoneracion {

    private static final Logger LOGGER = LoggerFactory.getLogger(BllAprobacionExoneracion.class);
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DalTramite vgo_DalTramite;
    private final DalExoneracion vgo_DalExoneracion;
    private final DalEstadoTramite vgo_DalEstadoTramite;
    private final DalHistorialEstado vgo_DalHistorialEstado;
    private final DalTipoTramite vgo_DalTipoTramite;
    private final DalDecisionExoneracion vgo_DalDecisionExoneracion;
    private final DalUsuario vgo_DalUsuario;
    private final DalAutorizacion vgo_DalAutorizacion;
    private final BllControlDocumental vgo_BllControlDocumental;
    private final BllObservacionExpediente vgo_BllObservacionExpediente;
    private final BllBitacora vgo_BllBitacora;
    private final JavaMailSender vgo_Correo;
    private final String vgc_CorreoRemitente;

    public BllAprobacionExoneracion(
            DalTramite pvo_DalTramite,
            DalExoneracion pvo_DalExoneracion,
            DalEstadoTramite pvo_DalEstadoTramite,
            DalHistorialEstado pvo_DalHistorialEstado,
            DalTipoTramite pvo_DalTipoTramite,
            DalDecisionExoneracion pvo_DalDecisionExoneracion,
            DalUsuario pvo_DalUsuario,
            DalAutorizacion pvo_DalAutorizacion,
            BllControlDocumental pvo_BllControlDocumental,
            BllObservacionExpediente pvo_BllObservacionExpediente,
            BllBitacora pvo_BllBitacora,
            JavaMailSender pvo_Correo,
            @Value("${app.notificaciones.correo-remitente}") String pvc_CorreoRemitente) {
        this.vgo_DalTramite = pvo_DalTramite;
        this.vgo_DalExoneracion = pvo_DalExoneracion;
        this.vgo_DalEstadoTramite = pvo_DalEstadoTramite;
        this.vgo_DalHistorialEstado = pvo_DalHistorialEstado;
        this.vgo_DalTipoTramite = pvo_DalTipoTramite;
        this.vgo_DalDecisionExoneracion = pvo_DalDecisionExoneracion;
        this.vgo_DalUsuario = pvo_DalUsuario;
        this.vgo_DalAutorizacion = pvo_DalAutorizacion;
        this.vgo_BllControlDocumental = pvo_BllControlDocumental;
        this.vgo_BllObservacionExpediente = pvo_BllObservacionExpediente;
        this.vgo_BllBitacora = pvo_BllBitacora;
        this.vgo_Correo = pvo_Correo;
        this.vgc_CorreoRemitente = pvc_CorreoRemitente;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene la bandeja de solicitudes de exoneración pendientes de aprobación. Solo
     *   la consulta quien está autorizado a decidir.
     *
     * @param pvn_IdUsuario usuario autenticado
     * @return solicitudes pendientes, de la más antigua a la más reciente
     */
    @Transactional(readOnly = true)
    public List<ModeloAprobacionPendienteRespuesta> ObtenerPendientes(Integer pvn_IdUsuario) {
        this.ValidarAutorizacion(pvn_IdUsuario);

        EntEstadoTramite vlo_EnEstadoAprobacion = this.ObtenerEstadoPorNombre(
                this.ObtenerIdTipoTramiteExoneracion(),
                Constantes.NOMBRE_ESTADO_PENDIENTE_APROBACION_EXONERACION);
        Map<Integer, String> vlo_MpNombres = new HashMap<>();
        List<ModeloAprobacionPendienteRespuesta> vlo_LsRespuesta = new ArrayList<>();

        for (EntTramite vlo_EnTramite
                : this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorEstado(vlo_EnEstadoAprobacion.getIdEstadoTramite())) {
            EntExoneracion vlo_EnExoneracion =
                    this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(vlo_EnTramite.getIdTramite());
            if (vlo_EnExoneracion == null) {
                continue;
            }
            EntHistorialEstado vlo_EnEnvio = this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_ObtenerUltimoIngreso(
                    vlo_EnTramite.getIdTramite(), vlo_EnEstadoAprobacion.getIdEstadoTramite());

            ModeloAprobacionPendienteRespuesta vlo_Fila = new ModeloAprobacionPendienteRespuesta();
            vlo_Fila.setIdTramite(vlo_EnTramite.getIdTramite());
            vlo_Fila.setReferenciaInterna(vlo_EnTramite.getReferenciaInterna());
            vlo_Fila.setTipoExoneracion(vlo_EnExoneracion.getTipoExoneracion());
            vlo_Fila.setNumeroContratacion(vlo_EnExoneracion.getNumeroContratacion());
            if (vlo_EnEnvio != null) {
                vlo_Fila.setEnviadoPor(this.ObtenerNombreUsuario(vlo_EnEnvio.getIdUsuario(), vlo_MpNombres));
                vlo_Fila.setFechaEnvio(vlo_EnEnvio.getFechaHora());
            }
            vlo_LsRespuesta.add(vlo_Fila);
        }
        return vlo_LsRespuesta;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene el resumen del expediente para registrar la decisión: datos de la
     *   solicitud, resultado de la revisión documental, observaciones registradas, quién lo envió a
     *   aprobación, si el usuario puede decidir y el historial de decisiones.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvn_IdUsuario usuario autenticado
     * @return resumen para la aprobación
     */
    @Transactional(readOnly = true)
    public ModeloAprobacionRespuesta ObtenerAprobacion(Integer pvn_IdTramite, Integer pvn_IdUsuario) {
        EntTramite vlo_EnTramite = this.ObtenerTramite(pvn_IdTramite, false);
        return this.ArmarAprobacion(vlo_EnTramite, this.EsUsuarioAutorizado(pvn_IdUsuario));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Registra la decisión de la jefatura: aprobar, rechazar o solicitar modificación.
     *   Actualiza el estado del expediente, conserva la decisión, el movimiento de estado y la
     *   bitácora con fecha, hora y usuario, y notifica a la técnica responsable.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvn_IdUsuario usuario autenticado que decide
     * @param pvc_Decision APROBAR, RECHAZAR o MODIFICAR
     * @param pvc_Justificacion justificación de la decisión
     * @return resumen del expediente ya con la decisión registrada
     */
    @Transactional
    public ModeloAprobacionRespuesta RegistrarDecision(
            Integer pvn_IdTramite,
            Integer pvn_IdUsuario,
            String pvc_Decision,
            String pvc_Justificacion) {
        EntUsuario vlo_EnDecisor = this.ValidarAutorizacion(pvn_IdUsuario);
        String vlc_Decision = this.NormalizarDecision(pvc_Decision);
        String vlc_Justificacion = this.ValidarJustificacion(vlc_Decision, pvc_Justificacion);

        // El expediente queda bloqueado hasta el final de la transacción: dos decisiones
        // simultáneas no pueden registrarse las dos.
        EntTramite vlo_EnTramite = this.ObtenerTramite(pvn_IdTramite, true);
        EntEstadoTramite vlo_EnEstadoActual =
                this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(vlo_EnTramite.getIdEstadoTramite());
        if (!this.EstaPendienteAprobacion(vlo_EnEstadoActual)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXONERACION_NO_PENDIENTE_APROBACION);
        }

        if (Constantes.DECISION_EXONERACION_APROBAR.equals(vlc_Decision)) {
            ModeloControlDocumentalRespuesta vlo_Control =
                    this.vgo_BllControlDocumental.ObtenerControlDocumental(vlo_EnTramite.getIdTramite());
            if (!Boolean.TRUE.equals(vlo_Control.getCumpleDocumentacion())) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_EXONERACION_INCOMPLETA_NO_APROBABLE
                        + String.join("; ", vlo_Control.getDocumentosPendientes()) + ".");
            }
        }

        EntEstadoTramite vlo_EnEstadoNuevo =
                this.ObtenerEstadoPorNombre(vlo_EnTramite.getIdTipoTramite(), this.NombreEstadoDestino(vlc_Decision));

        EntDecisionExoneracion vlo_EnDecision = new EntDecisionExoneracion();
        vlo_EnDecision.setIdTramite(vlo_EnTramite.getIdTramite());
        vlo_EnDecision.setIdUsuarioDecisor(vlo_EnDecisor.getIdUsuario());
        vlo_EnDecision.setDecision(vlc_Decision);
        vlo_EnDecision.setJustificacion(vlc_Justificacion);
        vlo_EnDecision.setIdEstadoAnterior(vlo_EnEstadoActual.getIdEstadoTramite());
        vlo_EnDecision.setIdEstadoNuevo(vlo_EnEstadoNuevo.getIdEstadoTramite());
        vlo_EnDecision.setFechaHora(LocalDateTime.now());
        vlo_EnDecision.setEstado(Constantes.ESTADO_REGISTRO_DECISION_ACTIVO);
        this.vgo_DalDecisionExoneracion.SIMT_DECISION_EXONERACION_InsertarRegistro(vlo_EnDecision);

        // El técnico que envió a aprobación se busca antes de mover el estado.
        EntHistorialEstado vlo_EnEnvio = this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_ObtenerUltimoIngreso(
                vlo_EnTramite.getIdTramite(), vlo_EnEstadoActual.getIdEstadoTramite());

        EntHistorialEstado vlo_EnHistorial = new EntHistorialEstado();
        vlo_EnHistorial.setIdTramite(vlo_EnTramite.getIdTramite());
        vlo_EnHistorial.setIdEstadoAnterior(vlo_EnEstadoActual.getIdEstadoTramite());
        vlo_EnHistorial.setIdEstadoNuevo(vlo_EnEstadoNuevo.getIdEstadoTramite());
        vlo_EnHistorial.setIdUsuario(vlo_EnDecisor.getIdUsuario());
        vlo_EnHistorial.setJustificacion(this.Recortar(
                this.NombreDecision(vlc_Decision) + (vlc_Justificacion == null ? "" : ": " + vlc_Justificacion),
                Constantes.LONGITUD_MAXIMA_JUSTIFICACION_CAMBIO));
        this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_EnHistorial);

        vlo_EnTramite.setIdEstadoTramite(vlo_EnEstadoNuevo.getIdEstadoTramite());
        if (Boolean.TRUE.equals(vlo_EnEstadoNuevo.getEsFinal())) {
            vlo_EnTramite.setFechaCierre(vlo_EnDecision.getFechaHora().toLocalDate());
        }
        this.vgo_DalTramite.SIMT_TRAMITE_ModificarRegistro(vlo_EnTramite);

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnDecisor.getIdUsuario(),
                Constantes.ACCION_BITACORA_DECISION_EXONERACION,
                Constantes.ENTIDAD_BITACORA_DECISION_EXONERACION,
                vlo_EnDecision.getIdDecision(),
                null,
                "Decisión: " + this.NombreDecision(vlc_Decision) + "."
                        + (vlc_Justificacion == null ? "" : " Justificación: " + vlc_Justificacion));
        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnDecisor.getIdUsuario(),
                Constantes.ACCION_BITACORA_CAMBIO_ESTADO,
                Constantes.ENTIDAD_BITACORA_TRAMITE,
                vlo_EnTramite.getIdTramite(),
                "Estado: " + vlo_EnEstadoActual.getNombre() + ".",
                "Estado: " + vlo_EnEstadoNuevo.getNombre() + ".");

        EntNotificacionExoneracion vlo_EnNotificacion = this.Notificar(
                vlo_EnTramite, vlo_EnDecision, vlo_EnDecisor, vlo_EnEstadoNuevo, vlo_EnEnvio);

        ModeloAprobacionRespuesta vlo_Respuesta = this.ArmarAprobacion(vlo_EnTramite, true);
        vlo_Respuesta.setMensaje(this.DescribirResultado(vlc_Decision, vlo_EnEstadoNuevo, vlo_EnNotificacion));
        return vlo_Respuesta;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Registra la notificación de la decisión y envía el correo a la técnica que envió
     *   el expediente a aprobación; si no se conoce, al usuario responsable del expediente. Un fallo
     *   del correo no deshace la decisión: la notificación queda con estado de error.
     *
     * @param pvo_EnTramite expediente
     * @param pvo_EnDecision decisión registrada
     * @param pvo_EnDecisor usuario que decidió
     * @param pvo_EnEstadoNuevo estado resultante
     * @param pvo_EnEnvio movimiento con el que el expediente llegó a aprobación, puede ser nulo
     * @return notificación registrada
     */
    private EntNotificacionExoneracion Notificar(
            EntTramite pvo_EnTramite,
            EntDecisionExoneracion pvo_EnDecision,
            EntUsuario pvo_EnDecisor,
            EntEstadoTramite pvo_EnEstadoNuevo,
            EntHistorialEstado pvo_EnEnvio) {
        Integer vln_IdDestino = pvo_EnEnvio != null ? pvo_EnEnvio.getIdUsuario() : pvo_EnTramite.getIdUsuarioResponsable();
        EntUsuario vlo_EnDestino = this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(vln_IdDestino);

        String vlc_Nombre = this.NombreDecision(pvo_EnDecision.getDecision());
        EntNotificacionExoneracion vlo_EnNotificacion = new EntNotificacionExoneracion();
        vlo_EnNotificacion.setIdDecision(pvo_EnDecision.getIdDecision());
        vlo_EnNotificacion.setIdUsuarioDestino(vln_IdDestino);
        vlo_EnNotificacion.setCorreoDestino(vlo_EnDestino == null ? null : vlo_EnDestino.getCorreoInstitucional());
        vlo_EnNotificacion.setAsunto(this.Recortar(
                "Solicitud de exoneración " + pvo_EnTramite.getReferenciaInterna() + ": " + vlc_Nombre.toLowerCase(),
                150));
        vlo_EnNotificacion.setMensaje(this.Recortar(
                "La jefatura registró una decisión sobre la solicitud de exoneración "
                        + pvo_EnTramite.getReferenciaInterna() + ".\n"
                        + "Decisión: " + vlc_Nombre + ".\n"
                        + "Estado del expediente: " + pvo_EnEstadoNuevo.getNombre() + ".\n"
                        + (pvo_EnDecision.getJustificacion() == null
                                ? ""
                                : "Justificación: " + pvo_EnDecision.getJustificacion() + "\n")
                        + "Registrada por: " + pvo_EnDecisor.getNombreCompleto() + " el "
                        + pvo_EnDecision.getFechaHora().format(FORMATO_FECHA_HORA) + ".",
                2000));
        vlo_EnNotificacion.setEstadoEnvio(Constantes.ESTADO_ENVIO_NOTIFICACION_PENDIENTE);
        this.vgo_DalDecisionExoneracion.SIMT_NOTIFICACION_EXONERACION_InsertarRegistro(vlo_EnNotificacion);

        if (vlo_EnNotificacion.getCorreoDestino() == null || vlo_EnNotificacion.getCorreoDestino().isBlank()) {
            LOGGER.error("No se envió la notificación de la decisión {}: el destinatario no tiene correo.",
                    pvo_EnDecision.getIdDecision());
            vlo_EnNotificacion.setEstadoEnvio(Constantes.ESTADO_ENVIO_NOTIFICACION_ERROR);
        } else {
            try {
                SimpleMailMessage vlo_Mensaje = new SimpleMailMessage();
                vlo_Mensaje.setFrom(this.vgc_CorreoRemitente);
                vlo_Mensaje.setTo(vlo_EnNotificacion.getCorreoDestino());
                vlo_Mensaje.setSubject(vlo_EnNotificacion.getAsunto());
                vlo_Mensaje.setText(vlo_EnNotificacion.getMensaje());
                this.vgo_Correo.send(vlo_Mensaje);
                vlo_EnNotificacion.setEstadoEnvio(Constantes.ESTADO_ENVIO_NOTIFICACION_ENVIADA);
                vlo_EnNotificacion.setFechaEnvio(LocalDateTime.now());
            } catch (MailException vlo_Excepcion) {
                LOGGER.error("No se pudo enviar el correo de la decisión {}.",
                        pvo_EnDecision.getIdDecision(), vlo_Excepcion);
                vlo_EnNotificacion.setEstadoEnvio(Constantes.ESTADO_ENVIO_NOTIFICACION_ERROR);
            }
        }
        this.vgo_DalDecisionExoneracion.SIMT_NOTIFICACION_EXONERACION_ModificarRegistro(vlo_EnNotificacion);
        return vlo_EnNotificacion;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Arma el resumen del expediente para la pantalla de aprobación.
     *
     * @param pvo_EnTramite expediente
     * @param pvb_UsuarioAutorizado si el usuario autenticado puede decidir
     * @return resumen para la aprobación
     */
    private ModeloAprobacionRespuesta ArmarAprobacion(EntTramite pvo_EnTramite, boolean pvb_UsuarioAutorizado) {
        EntExoneracion vlo_EnExoneracion =
                this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(pvo_EnTramite.getIdTramite());
        EntEstadoTramite vlo_EnEstado =
                this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(pvo_EnTramite.getIdEstadoTramite());
        ModeloControlDocumentalRespuesta vlo_Control =
                this.vgo_BllControlDocumental.ObtenerControlDocumental(pvo_EnTramite.getIdTramite());
        List<EntObservacionExpediente> vlo_LsObservaciones =
                this.vgo_BllObservacionExpediente.ObtenerHistorial(pvo_EnTramite.getIdTramite());
        Map<Integer, String> vlo_MpNombres = new HashMap<>();

        ModeloAprobacionRespuesta vlo_Respuesta = new ModeloAprobacionRespuesta();
        vlo_Respuesta.setIdTramite(pvo_EnTramite.getIdTramite());
        vlo_Respuesta.setReferenciaInterna(pvo_EnTramite.getReferenciaInterna());
        vlo_Respuesta.setTipoExoneracion(vlo_EnExoneracion.getTipoExoneracion());
        vlo_Respuesta.setNumeroContratacion(vlo_EnExoneracion.getNumeroContratacion());
        vlo_Respuesta.setContratoSicop(vlo_EnExoneracion.getContratoSicop());
        vlo_Respuesta.setOrdenCompraGeco(vlo_EnExoneracion.getOrdenCompraGeco());
        vlo_Respuesta.setEstadoExpediente(vlo_EnEstado == null ? null : vlo_EnEstado.getNombre());
        vlo_Respuesta.setUsuarioResponsable(
                this.ObtenerNombreUsuario(pvo_EnTramite.getIdUsuarioResponsable(), vlo_MpNombres));

        EntEstadoTramite vlo_EnEstadoAprobacion = this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                pvo_EnTramite.getIdTipoTramite(),
                Constantes.NOMBRE_ESTADO_PENDIENTE_APROBACION_EXONERACION,
                Constantes.ESTADO_CATALOGO_ACTIVO);
        if (vlo_EnEstadoAprobacion != null) {
            EntHistorialEstado vlo_EnEnvio = this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_ObtenerUltimoIngreso(
                    pvo_EnTramite.getIdTramite(), vlo_EnEstadoAprobacion.getIdEstadoTramite());
            if (vlo_EnEnvio != null) {
                vlo_Respuesta.setEnviadoPor(this.ObtenerNombreUsuario(vlo_EnEnvio.getIdUsuario(), vlo_MpNombres));
                vlo_Respuesta.setFechaEnvio(vlo_EnEnvio.getFechaHora());
            }
        }

        vlo_Respuesta.setDocumentosRecibidos(vlo_Control.getObligatoriosCumplidos());
        vlo_Respuesta.setTotalObligatorios(vlo_Control.getTotalObligatorios());
        vlo_Respuesta.setCumpleDocumentacion(vlo_Control.getCumpleDocumentacion());
        vlo_Respuesta.setDocumentosPendientes(vlo_Control.getDocumentosPendientes());
        vlo_Respuesta.setObservacionesRegistradas(vlo_LsObservaciones.size());
        vlo_Respuesta.setUltimaObservacion(vlo_LsObservaciones.stream()
                .map(EntObservacionExpediente::getFechaConfirmacion)
                .filter(vld_Fecha -> vld_Fecha != null)
                .max(LocalDateTime::compareTo)
                .orElse(null));

        boolean vlb_Pendiente = this.EstaPendienteAprobacion(vlo_EnEstado);
        vlo_Respuesta.setUsuarioAutorizado(pvb_UsuarioAutorizado);
        vlo_Respuesta.setPuedeDecidir(pvb_UsuarioAutorizado && vlb_Pendiente);
        if (!pvb_UsuarioAutorizado) {
            vlo_Respuesta.setMotivoNoDecide(Constantes.MENSAJE_USUARIO_NO_AUTORIZADO_APROBAR);
        } else if (!vlb_Pendiente) {
            vlo_Respuesta.setMotivoNoDecide(Constantes.MENSAJE_EXONERACION_NO_PENDIENTE_APROBACION);
        }

        List<ModeloDecisionRespuesta> vlo_LsDecisiones = new ArrayList<>();
        for (EntDecisionExoneracion vlo_EnDecision : this.vgo_DalDecisionExoneracion
                .SIMT_DECISION_EXONERACION_ObtenerPorTramite(
                        pvo_EnTramite.getIdTramite(), Constantes.ESTADO_REGISTRO_DECISION_ACTIVO)) {
            vlo_LsDecisiones.add(this.ConvertirDecision(vlo_EnDecision, vlo_MpNombres));
        }
        vlo_Respuesta.setDecisiones(vlo_LsDecisiones);
        return vlo_Respuesta;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Convierte una decisión al modelo de respuesta, con los nombres del decisor, de
     *   los estados y del destinatario de la notificación ya resueltos.
     *
     * @param pvo_EnDecision decisión registrada
     * @param pvo_MpNombres nombres de usuario ya consultados
     * @return modelo de respuesta
     */
    private ModeloDecisionRespuesta ConvertirDecision(
            EntDecisionExoneracion pvo_EnDecision,
            Map<Integer, String> pvo_MpNombres) {
        ModeloDecisionRespuesta vlo_Modelo = new ModeloDecisionRespuesta();
        vlo_Modelo.setIdDecision(pvo_EnDecision.getIdDecision());
        vlo_Modelo.setDecision(pvo_EnDecision.getDecision());
        vlo_Modelo.setNombreDecision(this.NombreDecision(pvo_EnDecision.getDecision()));
        vlo_Modelo.setJustificacion(pvo_EnDecision.getJustificacion());
        vlo_Modelo.setDecisor(this.ObtenerNombreUsuario(pvo_EnDecision.getIdUsuarioDecisor(), pvo_MpNombres));
        vlo_Modelo.setEstadoAnterior(this.ObtenerNombreEstado(pvo_EnDecision.getIdEstadoAnterior()));
        vlo_Modelo.setEstadoNuevo(this.ObtenerNombreEstado(pvo_EnDecision.getIdEstadoNuevo()));
        vlo_Modelo.setFechaHora(pvo_EnDecision.getFechaHora());

        EntNotificacionExoneracion vlo_EnNotificacion = this.vgo_DalDecisionExoneracion
                .SIMT_NOTIFICACION_EXONERACION_ObtenerPorDecision(
                        pvo_EnDecision.getIdDecision(), Constantes.ESTADO_REGISTRO_DECISION_ACTIVO);
        if (vlo_EnNotificacion != null) {
            vlo_Modelo.setNotificado(this.ObtenerNombreUsuario(vlo_EnNotificacion.getIdUsuarioDestino(), pvo_MpNombres));
            vlo_Modelo.setEstadoNotificacion(vlo_EnNotificacion.getEstadoEnvio());
        }
        return vlo_Modelo;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Comprueba que el usuario exista y tenga el permiso de aprobar exoneraciones.
     *
     * @param pvn_IdUsuario usuario autenticado
     * @return usuario autorizado
     */
    private EntUsuario ValidarAutorizacion(Integer pvn_IdUsuario) {
        EntUsuario vlo_EnUsuario = pvn_IdUsuario == null
                ? null
                : this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(pvn_IdUsuario);
        if (vlo_EnUsuario == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_USUARIO_DECISION_INVALIDO);
        }
        if (!this.EsUsuarioAutorizado(pvn_IdUsuario)) {
            throw new ExcepcionAutenticacion(HttpStatus.FORBIDDEN, Constantes.MENSAJE_USUARIO_NO_AUTORIZADO_APROBAR);
        }
        return vlo_EnUsuario;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Indica si el usuario tiene, por alguno de sus roles activos, el permiso de
     *   aprobar exoneraciones.
     *
     * @param pvn_IdUsuario usuario autenticado
     * @return verdadero si está autorizado
     */
    private boolean EsUsuarioAutorizado(Integer pvn_IdUsuario) {
        return pvn_IdUsuario != null
                && this.vgo_DalAutorizacion.SIMT_USUARIO_ROL_ObtenerPermisosActivos(pvn_IdUsuario)
                .contains(Permisos.EXONERACION_APROBAR);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Convierte la decisión recibida (APROBAR, RECHAZAR o MODIFICAR, o su código) al
     *   código que se guarda.
     *
     * @param pvc_Decision decisión recibida
     * @return APR, REC o MOD
     */
    private String NormalizarDecision(String pvc_Decision) {
        String vlc_Decision = pvc_Decision == null ? "" : pvc_Decision.trim().toUpperCase();
        return switch (vlc_Decision) {
            case "APROBAR", Constantes.DECISION_EXONERACION_APROBAR -> Constantes.DECISION_EXONERACION_APROBAR;
            case "RECHAZAR", Constantes.DECISION_EXONERACION_RECHAZAR -> Constantes.DECISION_EXONERACION_RECHAZAR;
            case "MODIFICAR", Constantes.DECISION_EXONERACION_MODIFICAR -> Constantes.DECISION_EXONERACION_MODIFICAR;
            default -> throw new ExcepcionNegocio(Constantes.MENSAJE_DECISION_INVALIDA);
        };
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Valida la justificación: obligatoria al rechazar o pedir modificación y con un
     *   máximo de 1000 caracteres.
     *
     * @param pvc_Decision código de la decisión
     * @param pvc_Justificacion justificación recibida
     * @return justificación limpia, o null si no se indicó
     */
    private String ValidarJustificacion(String pvc_Decision, String pvc_Justificacion) {
        String vlc_Justificacion = pvc_Justificacion == null || pvc_Justificacion.isBlank()
                ? null
                : pvc_Justificacion.trim();
        if (vlc_Justificacion == null && !Constantes.DECISION_EXONERACION_APROBAR.equals(pvc_Decision)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_JUSTIFICACION_DECISION_REQUERIDA);
        }
        if (vlc_Justificacion != null
                && vlc_Justificacion.length() > Constantes.LONGITUD_MAXIMA_JUSTIFICACION_DECISION) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_JUSTIFICACION_DECISION_INVALIDA);
        }
        return vlc_Justificacion;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Recupera el expediente y comprueba que sea una solicitud de exoneración.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvb_Bloquear si se bloquea la fila hasta el final de la transacción
     * @return expediente
     */
    private EntTramite ObtenerTramite(Integer pvn_IdTramite, boolean pvb_Bloquear) {
        if (pvn_IdTramite == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ID_TRAMITE_REQUERIDO);
        }
        EntTramite vlo_EnTramite = pvb_Bloquear
                ? this.vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistroParaModificar(pvn_IdTramite)
                : this.vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistro(pvn_IdTramite);
        if (vlo_EnTramite == null || this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(pvn_IdTramite) == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXONERACION_ID_NO_ENCONTRADA);
        }
        return vlo_EnTramite;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene el identificador del tipo de trámite EXONERACION.
     *
     * @return identificador del tipo de trámite
     */
    private Integer ObtenerIdTipoTramiteExoneracion() {
        EntTipoTramite vlo_EnTipoTramite = this.vgo_DalTipoTramite
                .SIMM_TIPO_TRAMITE_ObtenerPorCodigo(Constantes.CODIGO_TIPO_TRAMITE_EXONERACION);
        if (vlo_EnTipoTramite == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_TRAMITE_EXONERACION_NO_CONFIGURADO);
        }
        return vlo_EnTipoTramite.getIdTipoTramite();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene un estado activo de la exoneración por su nombre.
     *
     * @param pvn_IdTipoTramite tipo de trámite
     * @param pvc_Nombre nombre del estado
     * @return estado encontrado
     */
    private EntEstadoTramite ObtenerEstadoPorNombre(Integer pvn_IdTipoTramite, String pvc_Nombre) {
        EntEstadoTramite vlo_EnEstado = this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                pvn_IdTipoTramite, pvc_Nombre, Constantes.ESTADO_CATALOGO_ACTIVO);
        if (vlo_EnEstado == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_DECISION_NO_CONFIGURADO);
        }
        return vlo_EnEstado;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Indica si el expediente está en "Pendiente de aprobación".
     */
    private boolean EstaPendienteAprobacion(EntEstadoTramite pvo_EnEstado) {
        return pvo_EnEstado != null
                && Constantes.NOMBRE_ESTADO_PENDIENTE_APROBACION_EXONERACION.equalsIgnoreCase(pvo_EnEstado.getNombre());
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Nombre del estado al que pasa el expediente según la decisión.
     */
    private String NombreEstadoDestino(String pvc_Decision) {
        return switch (pvc_Decision) {
            case Constantes.DECISION_EXONERACION_APROBAR -> Constantes.NOMBRE_ESTADO_EXONERACION_APROBADA;
            case Constantes.DECISION_EXONERACION_RECHAZAR -> Constantes.NOMBRE_ESTADO_EXONERACION_RECHAZADA;
            default -> Constantes.NOMBRE_ESTADO_PENDIENTE_MODIFICACION_EXONERACION;
        };
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Nombre legible de la decisión para la pantalla, la bitácora y el correo.
     */
    private String NombreDecision(String pvc_Decision) {
        return switch (pvc_Decision) {
            case Constantes.DECISION_EXONERACION_APROBAR -> "Aprobada";
            case Constantes.DECISION_EXONERACION_RECHAZAR -> "Rechazada";
            default -> "Modificación solicitada";
        };
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Nombre de un estado del catálogo, o un guion si no existe.
     */
    private String ObtenerNombreEstado(Integer pvn_IdEstado) {
        EntEstadoTramite vlo_EnEstado = this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(pvn_IdEstado);
        return vlo_EnEstado == null ? Constantes.TEXTO_SIN_DATO : vlo_EnEstado.getNombre();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Nombre de un usuario, consultado una sola vez por identificador.
     */
    private String ObtenerNombreUsuario(Integer pvn_IdUsuario, Map<Integer, String> pvo_MpNombres) {
        if (pvn_IdUsuario == null) {
            return Constantes.TEXTO_SIN_DATO;
        }
        return pvo_MpNombres.computeIfAbsent(pvn_IdUsuario, vln_Id -> {
            EntUsuario vlo_EnUsuario = this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(vln_Id);
            return vlo_EnUsuario == null ? Constantes.TEXTO_SIN_DATO : vlo_EnUsuario.getNombreCompleto();
        });
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Mensaje de confirmación de la decisión, con el resultado de la notificación.
     */
    private String DescribirResultado(
            String pvc_Decision,
            EntEstadoTramite pvo_EnEstadoNuevo,
            EntNotificacionExoneracion pvo_EnNotificacion) {
        String vlc_Notificacion = Constantes.ESTADO_ENVIO_NOTIFICACION_ENVIADA.equals(pvo_EnNotificacion.getEstadoEnvio())
                ? " Se notificó por correo a " + pvo_EnNotificacion.getCorreoDestino() + "."
                : " La notificación quedó registrada, pero el correo no se pudo enviar.";
        return "Decisión registrada: " + this.NombreDecision(pvc_Decision).toLowerCase()
                + ". El expediente pasó a \"" + pvo_EnEstadoNuevo.getNombre() + "\"." + vlc_Notificacion;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Recorta un texto a la longitud máxima de su columna.
     */
    private String Recortar(String pvc_Texto, int pvn_Longitud) {
        return pvc_Texto.length() <= pvn_Longitud ? pvc_Texto : pvc_Texto.substring(0, pvn_Longitud);
    }
}
