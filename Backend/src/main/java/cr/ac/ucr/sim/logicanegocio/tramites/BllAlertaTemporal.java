package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalAlertaTemporal;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntAgenteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntAlertaTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenAlertaTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntVencimientoTemporal;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Autor: Camila Pastor Castro
 * Fecha: 03/10/2026
 * Descripción: Reglas de las alertas de vencimiento de los trámites temporales (HU-027): genera
 *   una alerta por destinatario cuando el vencimiento se acerca, la envía por correo y permite
 *   marcarla como atendida.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: las alertas se separan de BllTramiteTemporal, que pasa a registrar el expediente
 *     de la HU-025. La alerta se genera para todo expediente abierto cuyo vencimiento caiga dentro
 *     de los próximos 45 días, aunque la fecha se haya registrado tarde, y se dirige al responsable
 *     del expediente y a los agentes asignados, que pasan a ser opcionales.
 */
@Service
public class BllAlertaTemporal {

    private static final Logger LOGGER = LoggerFactory.getLogger(BllAlertaTemporal.class);
    private static final String UNIDAD_MESES = "MESES";
    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_ATENDIDA = "ATENDIDA";
    private static final String ESTADO_CORREO_ENVIADO = "ENVIADO";
    private static final ZoneId ZONA_HORARIA_SISTEMA = ZoneId.of("America/Costa_Rica");

    private final DalAlertaTemporal vgo_DalAlertaTemporal;
    private final DalTramiteTemporal vgo_DalTramiteTemporal;
    private final DalUsuario vgo_DalUsuario;
    private final JavaMailSender vgo_Correo;
    private final String vgc_CorreoRemitente;

    public BllAlertaTemporal(
            DalAlertaTemporal pvo_DalAlertaTemporal,
            DalTramiteTemporal pvo_DalTramiteTemporal,
            DalUsuario pvo_DalUsuario,
            JavaMailSender pvo_Correo,
            @Value("${app.notificaciones.correo-remitente}") String pvc_CorreoRemitente) {
        this.vgo_DalAlertaTemporal = pvo_DalAlertaTemporal;
        this.vgo_DalTramiteTemporal = pvo_DalTramiteTemporal;
        this.vgo_DalUsuario = pvo_DalUsuario;
        this.vgo_Correo = pvo_Correo;
        this.vgc_CorreoRemitente = pvc_CorreoRemitente;
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Obtiene la bandeja de alertas de un usuario, con la indicación de las que
     *   siguen pendientes.
     *
     * @param pvn_IdUsuario usuario que recibe las alertas
     * @return alertas del usuario
     */
    @Transactional(readOnly = true)
    public List<EntResumenAlertaTemporal> ObtenerAlertas(Integer pvn_IdUsuario) {
        LocalDate vld_Hoy = this.Hoy();
        return this.vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ObtenerLista(pvn_IdUsuario).stream()
                .map(pvo_Alerta -> this.AgregarIndicacion(pvo_Alerta, vld_Hoy))
                .toList();
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Marca como atendida una alerta pendiente del usuario.
     *
     * @param pvn_IdAlerta identificador de la alerta
     * @param pvn_IdUsuario usuario que recibe la alerta
     * @return alerta actualizada
     */
    @Transactional
    public EntResumenAlertaTemporal MarcarAtendida(Integer pvn_IdAlerta, Integer pvn_IdUsuario) {
        EntAlertaTemporal vlo_Alerta = this.vgo_DalAlertaTemporal
                .SIMT_ALERTA_TEMPORAL_ObtenerRegistro(pvn_IdAlerta, pvn_IdUsuario);
        if (vlo_Alerta == null || !ESTADO_PENDIENTE.equals(vlo_Alerta.getEstado())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ALERTA_TEMPORAL_NO_ENCONTRADA);
        }
        vlo_Alerta.setEstado(ESTADO_ATENDIDA);
        this.vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ModificarRegistro(vlo_Alerta);
        return this.AgregarIndicacion(
                this.vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ObtenerResumen(pvn_IdAlerta),
                this.Hoy());
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Marca como atendidas las alertas pendientes de un expediente. Se usa cuando el
     *   vencimiento cambia (prórroga o corrección) o cuando el expediente se cierra: las alertas
     *   del vencimiento anterior ya no requieren acción.
     *
     * @param pvn_IdTramite identificador del expediente
     */
    @Transactional
    public void AtenderPendientes(Integer pvn_IdTramite) {
        for (EntAlertaTemporal vlo_Alerta
                : this.vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ObtenerPendientesPorTramite(pvn_IdTramite)) {
            vlo_Alerta.setEstado(ESTADO_ATENDIDA);
            this.vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ModificarRegistro(vlo_Alerta);
        }
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Genera las alertas de los vencimientos próximos y envía los correos pendientes.
     *   Una alerta existente no se duplica, y un correo que falla se reintenta en la próxima
     *   ejecución.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: alerta todo expediente abierto que vence en los próximos 45 días o que ya
     *     venció, y no solo el que vence exactamente en la fecha límite. Los destinatarios son el
     *     responsable del expediente y los agentes asignados.
     *
     * @param pvd_Hoy fecha de la ejecución
     */
    @Transactional
    public void ProcesarAlertas(LocalDate pvd_Hoy) {
        LocalDate vld_FechaLimite = pvd_Hoy.plusDays(Constantes.DIAS_ANTICIPACION_ALERTA_TEMPORAL);
        for (EntVencimientoTemporal vlo_Vencimiento
                : this.vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerPorVencimiento(vld_FechaLimite)) {
            for (Integer vln_IdDestino : this.ObtenerDestinatarios(vlo_Vencimiento)) {
                if (!this.vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_Existe(
                        vlo_Vencimiento.idTramite(), vln_IdDestino, vlo_Vencimiento.fechaVencimiento())) {
                    EntAlertaTemporal vlo_Alerta = new EntAlertaTemporal();
                    vlo_Alerta.setIdTramite(vlo_Vencimiento.idTramite());
                    vlo_Alerta.setIdUsuarioDestino(vln_IdDestino);
                    vlo_Alerta.setFechaVencimiento(vlo_Vencimiento.fechaVencimiento());
                    vlo_Alerta.setFechaGeneracion(pvd_Hoy);
                    vlo_Alerta.setEstado(ESTADO_PENDIENTE);
                    vlo_Alerta.setEstadoCorreo(ESTADO_PENDIENTE);
                    this.vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_InsertarRegistro(vlo_Alerta);
                }
            }
        }

        this.EnviarCorreosPendientes();
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Recomienda la declaración de reimportación cuando venció el plazo estándar de
     *   6 meses sin extensión. Solo aplica a los trámites que registran su plazo en meses.
     *
     * @param pvc_Unidad unidad del plazo registrado
     * @param pvn_Cantidad cantidad del plazo registrado
     * @param pvn_CantidadExtension cantidad de la extensión, si existe
     * @param pvd_Vencimiento vencimiento vigente
     * @param pvd_Hoy fecha de la consulta
     * @return la recomendación o texto vacío
     */
    public String ObtenerIndicacion(
            String pvc_Unidad,
            Integer pvn_Cantidad,
            Integer pvn_CantidadExtension,
            LocalDate pvd_Vencimiento,
            LocalDate pvd_Hoy) {
        return UNIDAD_MESES.equals(pvc_Unidad)
                && Integer.valueOf(6).equals(pvn_Cantidad)
                && pvn_CantidadExtension == null
                && pvd_Vencimiento != null
                && !pvd_Hoy.isBefore(pvd_Vencimiento)
                ? "Venció el plazo estándar de 6 meses; corresponde una declaración de reimportación."
                : "";
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Fecha actual en la zona horaria de Costa Rica.
     *
     * @return fecha de hoy
     */
    public LocalDate Hoy() {
        return LocalDate.now(ZONA_HORARIA_SISTEMA);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Reúne los destinatarios de la alerta de un expediente: su responsable, que
     *   siempre la recibe, y los agentes asignados, sin repetir usuarios.
     *
     * @param pvo_Vencimiento expediente por alertar
     * @return usuarios destinatarios
     */
    private Set<Integer> ObtenerDestinatarios(EntVencimientoTemporal pvo_Vencimiento) {
        Set<Integer> vlo_LsDestinos = new LinkedHashSet<>();
        vlo_LsDestinos.add(pvo_Vencimiento.idUsuarioResponsable());
        for (EntAgenteTemporal vlo_Agente
                : this.vgo_DalTramiteTemporal.SIMT_TEMPORAL_AGENTE_ObtenerLista(pvo_Vencimiento.idTramite())) {
            vlo_LsDestinos.add(vlo_Agente.idUsuario());
        }
        return vlo_LsDestinos;
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Envía el correo de cada alerta pendiente de envío. Si el destinatario no tiene
     *   correo o el envío falla, la alerta queda pendiente para la próxima ejecución.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: el correo indica el tipo de trámite y pide gestionar el retorno del equipo, que
     *     sirve tanto para la reexportación como para la reimportación (HU-026).
     */
    private void EnviarCorreosPendientes() {
        for (EntAlertaTemporal vlo_Alerta
                : this.vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ObtenerCorreosPendientes()) {
            EntUsuario vlo_Destino = this.vgo_DalUsuario
                    .SIMF_USUARIO_ObtenerRegistro(vlo_Alerta.getIdUsuarioDestino());
            if (vlo_Destino == null || vlo_Destino.getCorreoInstitucional() == null
                    || vlo_Destino.getCorreoInstitucional().isBlank()) {
                LOGGER.error("No se pudo enviar el correo de la alerta {}: falta el correo del destinatario.",
                        vlo_Alerta.getIdAlertaTemporal());
                continue;
            }
            EntResumenAlertaTemporal vlo_Resumen = this.vgo_DalAlertaTemporal
                    .SIMT_ALERTA_TEMPORAL_ObtenerResumen(vlo_Alerta.getIdAlertaTemporal());
            SimpleMailMessage vlo_Mensaje = new SimpleMailMessage();
            vlo_Mensaje.setFrom(this.vgc_CorreoRemitente);
            vlo_Mensaje.setTo(vlo_Destino.getCorreoInstitucional());
            vlo_Mensaje.setSubject("Vencimiento próximo de trámite temporal " + vlo_Resumen.referenciaInterna());
            vlo_Mensaje.setText("El trámite temporal " + vlo_Resumen.referenciaInterna()
                    + " vence el " + vlo_Resumen.fechaVencimiento() + ".\n"
                    + "Tipo de trámite: " + vlo_Resumen.tipoTramite() + ".\n"
                    + "Unidad solicitante: " + vlo_Resumen.unidadSolicitante() + ".\n"
                    + "Mercancía: " + vlo_Resumen.descripcionMercancia() + ".\n"
                    + "Revise el trámite y gestione el retorno del equipo o la prórroga del plazo.");
            try {
                this.vgo_Correo.send(vlo_Mensaje);
                vlo_Alerta.setEstadoCorreo(ESTADO_CORREO_ENVIADO);
                vlo_Alerta.setFechaEnvioCorreo(LocalDateTime.now(ZONA_HORARIA_SISTEMA));
                this.vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ModificarRegistro(vlo_Alerta);
            } catch (MailException vlo_Excepcion) {
                LOGGER.error("No se pudo enviar el correo de la alerta {}. Se reintentará en la próxima ejecución.",
                        vlo_Alerta.getIdAlertaTemporal(), vlo_Excepcion);
            }
        }
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Agrega la recomendación del plazo estándar a una alerta pendiente.
     *
     * @param pvo_Alerta alerta consultada
     * @param pvd_Hoy fecha de la consulta
     * @return alerta con su indicación
     */
    private EntResumenAlertaTemporal AgregarIndicacion(EntResumenAlertaTemporal pvo_Alerta, LocalDate pvd_Hoy) {
        String vlc_Indicacion = ESTADO_PENDIENTE.equals(pvo_Alerta.estado())
                ? this.ObtenerIndicacion(
                        pvo_Alerta.unidadPlazo(),
                        pvo_Alerta.cantidadPlazo(),
                        pvo_Alerta.cantidadExtension(),
                        pvo_Alerta.fechaVencimiento(),
                        pvd_Hoy)
                : "";
        return new EntResumenAlertaTemporal(
                pvo_Alerta.idAlerta(),
                pvo_Alerta.referenciaInterna(),
                pvo_Alerta.tipoTramite(),
                pvo_Alerta.unidadSolicitante(),
                pvo_Alerta.descripcionMercancia(),
                pvo_Alerta.fechaVencimiento(),
                pvo_Alerta.cantidadPlazo(),
                pvo_Alerta.unidadPlazo(),
                pvo_Alerta.cantidadExtension(),
                pvo_Alerta.unidadExtension(),
                pvo_Alerta.numeroContrato(),
                pvo_Alerta.estado(),
                pvo_Alerta.estadoCorreo(),
                vlc_Indicacion);
    }
}
