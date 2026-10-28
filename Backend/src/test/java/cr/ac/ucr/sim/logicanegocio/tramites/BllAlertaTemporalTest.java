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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Autor: Camila Pastor Castro
 * Fecha: 03/10/2026
 * Descripción: Pruebas de las alertas de vencimiento de los trámites temporales (HU-027).
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: las pruebas de alertas pasan de BllTramiteTemporalTest a esta clase. Se prueban
 *     el responsable como destinatario, los vencimientos registrados dentro del periodo de alerta
 *     y la atención de las alertas pendientes de un expediente (HU-025).
 */
@ExtendWith(MockitoExtension.class)
class BllAlertaTemporalTest {

    private static final Integer ID_RESPONSABLE = 1;
    private static final Integer ID_AGENTE = 10;
    private static final Integer ID_TRAMITE = 31;
    private static final LocalDate HOY = LocalDate.now(ZoneId.of("America/Costa_Rica"));

    @Mock private DalAlertaTemporal vgo_DalAlertaTemporal;
    @Mock private DalTramiteTemporal vgo_DalTramiteTemporal;
    @Mock private DalUsuario vgo_DalUsuario;
    @Mock private JavaMailSender vgo_Correo;

    private BllAlertaTemporal vgo_BllAlertaTemporal;

    @BeforeEach
    void PrepararPrueba() {
        this.vgo_BllAlertaTemporal = new BllAlertaTemporal(
                vgo_DalAlertaTemporal, vgo_DalTramiteTemporal, vgo_DalUsuario, vgo_Correo,
                "notificaciones@ucr.ac.cr");
        lenient().when(vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_AGENTE)).thenReturn(CrearUsuario());
        lenient().when(vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ObtenerCorreosPendientes()).thenReturn(List.of());
    }

    @Test
    void ProcesarAlertas_VencimientoProximo_AlertaAlResponsableYALosAgentesSinDuplicar() {
        LocalDate vld_Vencimiento = HOY.plusDays(20);
        when(vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerPorVencimiento(
                HOY.plusDays(Constantes.DIAS_ANTICIPACION_ALERTA_TEMPORAL)))
                .thenReturn(List.of(new EntVencimientoTemporal(ID_TRAMITE, ID_RESPONSABLE, vld_Vencimiento)));
        when(vgo_DalTramiteTemporal.SIMT_TEMPORAL_AGENTE_ObtenerLista(ID_TRAMITE))
                .thenReturn(List.of(new EntAgenteTemporal(ID_AGENTE, "Agente"),
                        new EntAgenteTemporal(ID_RESPONSABLE, "Responsable que también es agente")));
        when(vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_Existe(ID_TRAMITE, ID_RESPONSABLE, vld_Vencimiento))
                .thenReturn(false, true);
        when(vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_Existe(ID_TRAMITE, ID_AGENTE, vld_Vencimiento))
                .thenReturn(false, true);

        this.vgo_BllAlertaTemporal.ProcesarAlertas(HOY);
        this.vgo_BllAlertaTemporal.ProcesarAlertas(HOY);

        ArgumentCaptor<EntAlertaTemporal> vlo_Captor = ArgumentCaptor.forClass(EntAlertaTemporal.class);
        verify(vgo_DalAlertaTemporal, times(2)).SIMT_ALERTA_TEMPORAL_InsertarRegistro(vlo_Captor.capture());
        assertEquals(List.of(ID_RESPONSABLE, ID_AGENTE), vlo_Captor.getAllValues().stream()
                .map(EntAlertaTemporal::getIdUsuarioDestino)
                .toList());
        assertEquals("PENDIENTE", vlo_Captor.getAllValues().getFirst().getEstado());
        assertEquals(vld_Vencimiento, vlo_Captor.getAllValues().getFirst().getFechaVencimiento());
    }

    @Test
    void ProcesarAlertas_SinAgentes_AlertaSoloAlResponsable() {
        LocalDate vld_Vencimiento = HOY.plusDays(3);
        when(vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerPorVencimiento(any()))
                .thenReturn(List.of(new EntVencimientoTemporal(ID_TRAMITE, ID_RESPONSABLE, vld_Vencimiento)));
        when(vgo_DalTramiteTemporal.SIMT_TEMPORAL_AGENTE_ObtenerLista(ID_TRAMITE)).thenReturn(List.of());

        this.vgo_BllAlertaTemporal.ProcesarAlertas(HOY);

        ArgumentCaptor<EntAlertaTemporal> vlo_Captor = ArgumentCaptor.forClass(EntAlertaTemporal.class);
        verify(vgo_DalAlertaTemporal).SIMT_ALERTA_TEMPORAL_InsertarRegistro(vlo_Captor.capture());
        assertEquals(ID_RESPONSABLE, vlo_Captor.getValue().getIdUsuarioDestino());
    }

    @Test
    void ProcesarAlertas_CorreoPendiente_EnviaMensajeYMarcaElCorreoComoEnviado() {
        EntAlertaTemporal vlo_Alerta = CrearAlerta(ID_AGENTE);
        vlo_Alerta.setIdAlertaTemporal(4);
        when(vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerPorVencimiento(any())).thenReturn(List.of());
        when(vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ObtenerCorreosPendientes()).thenReturn(List.of(vlo_Alerta));
        when(vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ObtenerResumen(4)).thenReturn(CrearResumen("PENDIENTE"));

        this.vgo_BllAlertaTemporal.ProcesarAlertas(HOY);

        assertEquals("ENVIADO", vlo_Alerta.getEstadoCorreo());
        assertNotNull(vlo_Alerta.getFechaEnvioCorreo());
        ArgumentCaptor<SimpleMailMessage> vlo_Captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(vgo_Correo).send(vlo_Captor.capture());
        assertEquals("agente@ucr.ac.cr", vlo_Captor.getValue().getTo()[0]);
        assertEquals("IT-001-26", vlo_Captor.getValue().getSubject().substring(
                vlo_Captor.getValue().getSubject().lastIndexOf(' ') + 1));
        verify(vgo_DalAlertaTemporal).SIMT_ALERTA_TEMPORAL_ModificarRegistro(vlo_Alerta);
    }

    @Test
    void ProcesarAlertas_CorreoFalla_DejaLaAlertaPendienteParaReintentar() {
        EntAlertaTemporal vlo_Alerta = CrearAlerta(ID_AGENTE);
        vlo_Alerta.setIdAlertaTemporal(4);
        when(vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerPorVencimiento(any())).thenReturn(List.of());
        when(vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ObtenerCorreosPendientes()).thenReturn(List.of(vlo_Alerta));
        when(vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ObtenerResumen(4)).thenReturn(CrearResumen("PENDIENTE"));
        doThrow(new MailSendException("Sin servidor de correo")).when(vgo_Correo).send(any(SimpleMailMessage.class));

        this.vgo_BllAlertaTemporal.ProcesarAlertas(HOY);

        assertEquals("PENDIENTE", vlo_Alerta.getEstadoCorreo());
        verify(vgo_DalAlertaTemporal, never()).SIMT_ALERTA_TEMPORAL_ModificarRegistro(any());
    }

    @Test
    void ObtenerAlertas_PlazoEstandarVencido_IndicaReimportacion() {
        EntResumenAlertaTemporal vlo_Alerta = new EntResumenAlertaTemporal(
                4, "IT-001-26", "Importación temporal", "Escuela de Física", "Equipo temporal",
                HOY.minusDays(1), 6, "MESES", null, null, null, "PENDIENTE", "ENVIADO", "");
        when(vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ObtenerLista(ID_AGENTE)).thenReturn(List.of(vlo_Alerta));

        EntResumenAlertaTemporal vlo_Resultado = this.vgo_BllAlertaTemporal.ObtenerAlertas(ID_AGENTE).getFirst();

        assertEquals("Venció el plazo estándar de 6 meses; corresponde una declaración de reimportación.",
                vlo_Resultado.indicacionVencimiento());
    }

    @Test
    void MarcarAtendida_AlertaYaAtendida_RechazaLaOperacion() {
        EntAlertaTemporal vlo_Alerta = CrearAlerta(ID_AGENTE);
        vlo_Alerta.setEstado("ATENDIDA");
        when(vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ObtenerRegistro(4, ID_AGENTE)).thenReturn(vlo_Alerta);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAlertaTemporal.MarcarAtendida(4, ID_AGENTE));

        assertEquals(Constantes.MENSAJE_ALERTA_TEMPORAL_NO_ENCONTRADA, vlo_Excepcion.getMessage());
    }

    @Test
    void AtenderPendientes_MarcaComoAtendidasLasAlertasDeTodosLosDestinatarios() {
        EntAlertaTemporal vlo_AlertaResponsable = CrearAlerta(ID_RESPONSABLE);
        EntAlertaTemporal vlo_AlertaAgente = CrearAlerta(ID_AGENTE);
        when(vgo_DalAlertaTemporal.SIMT_ALERTA_TEMPORAL_ObtenerPendientesPorTramite(ID_TRAMITE))
                .thenReturn(List.of(vlo_AlertaResponsable, vlo_AlertaAgente));

        this.vgo_BllAlertaTemporal.AtenderPendientes(ID_TRAMITE);

        assertEquals("ATENDIDA", vlo_AlertaResponsable.getEstado());
        assertEquals("ATENDIDA", vlo_AlertaAgente.getEstado());
        verify(vgo_DalAlertaTemporal, times(2)).SIMT_ALERTA_TEMPORAL_ModificarRegistro(any());
    }

    private EntUsuario CrearUsuario() {
        EntUsuario vlo_Usuario = new EntUsuario();
        vlo_Usuario.setIdUsuario(ID_AGENTE);
        vlo_Usuario.setNombreCompleto("Agente de prueba");
        vlo_Usuario.setCorreoInstitucional("agente@ucr.ac.cr");
        return vlo_Usuario;
    }

    private EntAlertaTemporal CrearAlerta(Integer pvn_IdDestino) {
        EntAlertaTemporal vlo_Alerta = new EntAlertaTemporal();
        vlo_Alerta.setIdTramite(ID_TRAMITE);
        vlo_Alerta.setIdUsuarioDestino(pvn_IdDestino);
        vlo_Alerta.setFechaVencimiento(HOY.plusDays(7));
        vlo_Alerta.setFechaGeneracion(HOY);
        vlo_Alerta.setEstado("PENDIENTE");
        vlo_Alerta.setEstadoCorreo("PENDIENTE");
        return vlo_Alerta;
    }

    private EntResumenAlertaTemporal CrearResumen(String pvc_Estado) {
        return new EntResumenAlertaTemporal(
                4, "IT-001-26", "Importación temporal", "Escuela de Física", "Equipo temporal",
                HOY.plusDays(7), null, null, null, null, null, pvc_Estado, "PENDIENTE", "");
    }
}
