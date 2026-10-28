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
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntDecisionExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntNotificacionExoneracion;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionAutenticacion;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.logicanegocio.tramites.BllBitacora;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.Permisos;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloAprobacionRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloControlDocumentalRespuesta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Pruebas de las reglas de la aprobación de solicitudes de exoneración (HU-022).
 */
@ExtendWith(MockitoExtension.class)
class BllAprobacionExoneracionTest {

    private static final Integer ID_TRAMITE = 10;
    private static final Integer ID_TIPO_TRAMITE = 5;
    private static final Integer ID_JEFATURA = 7;
    private static final Integer ID_TECNICA = 3;
    private static final Integer ID_ESTADO_APROBACION = 51;
    private static final Integer ID_ESTADO_APROBADA = 52;
    private static final Integer ID_ESTADO_RECHAZADA = 53;
    private static final Integer ID_ESTADO_MODIFICACION = 54;

    @Mock private DalTramite vgo_DalTramite;
    @Mock private DalExoneracion vgo_DalExoneracion;
    @Mock private DalEstadoTramite vgo_DalEstadoTramite;
    @Mock private DalHistorialEstado vgo_DalHistorialEstado;
    @Mock private DalTipoTramite vgo_DalTipoTramite;
    @Mock private DalDecisionExoneracion vgo_DalDecisionExoneracion;
    @Mock private DalUsuario vgo_DalUsuario;
    @Mock private DalAutorizacion vgo_DalAutorizacion;
    @Mock private BllControlDocumental vgo_BllControlDocumental;
    @Mock private BllObservacionExpediente vgo_BllObservacionExpediente;
    @Mock private BllBitacora vgo_BllBitacora;
    @Mock private JavaMailSender vgo_Correo;

    private BllAprobacionExoneracion vgo_Bll;
    private EntTramite vgo_EnTramite;
    private ModeloControlDocumentalRespuesta vgo_Control;

    @BeforeEach
    void Preparar() {
        this.vgo_Bll = new BllAprobacionExoneracion(
                this.vgo_DalTramite, this.vgo_DalExoneracion, this.vgo_DalEstadoTramite,
                this.vgo_DalHistorialEstado, this.vgo_DalTipoTramite, this.vgo_DalDecisionExoneracion,
                this.vgo_DalUsuario, this.vgo_DalAutorizacion, this.vgo_BllControlDocumental,
                this.vgo_BllObservacionExpediente, this.vgo_BllBitacora, this.vgo_Correo,
                "notificaciones@ucr.ac.cr");

        this.vgo_EnTramite = new EntTramite();
        this.vgo_EnTramite.setIdTramite(ID_TRAMITE);
        this.vgo_EnTramite.setIdTipoTramite(ID_TIPO_TRAMITE);
        this.vgo_EnTramite.setIdEstadoTramite(ID_ESTADO_APROBACION);
        this.vgo_EnTramite.setIdUsuarioResponsable(ID_TECNICA);
        this.vgo_EnTramite.setReferenciaInterna("EXO-2026-0091");

        EntExoneracion vlo_EnExoneracion = new EntExoneracion();
        vlo_EnExoneracion.setIdTramite(ID_TRAMITE);
        vlo_EnExoneracion.setTipoExoneracion(Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL);

        this.vgo_Control = new ModeloControlDocumentalRespuesta();
        this.vgo_Control.setCumpleDocumentacion(true);
        this.vgo_Control.setTotalObligatorios(4);
        this.vgo_Control.setObligatoriosCumplidos(4);

        this.Estado(ID_ESTADO_APROBACION, Constantes.NOMBRE_ESTADO_PENDIENTE_APROBACION_EXONERACION, false);
        this.Estado(ID_ESTADO_APROBADA, Constantes.NOMBRE_ESTADO_EXONERACION_APROBADA, false);
        this.Estado(ID_ESTADO_RECHAZADA, Constantes.NOMBRE_ESTADO_EXONERACION_RECHAZADA, true);
        this.Estado(ID_ESTADO_MODIFICACION, Constantes.NOMBRE_ESTADO_PENDIENTE_MODIFICACION_EXONERACION, false);

        lenient().when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistro(ID_TRAMITE)).thenReturn(this.vgo_EnTramite);
        lenient().when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistroParaModificar(ID_TRAMITE))
                .thenReturn(this.vgo_EnTramite);
        lenient().when(this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(ID_TRAMITE))
                .thenReturn(vlo_EnExoneracion);
        lenient().when(this.vgo_BllControlDocumental.ObtenerControlDocumental(ID_TRAMITE)).thenReturn(this.vgo_Control);
        lenient().when(this.vgo_BllObservacionExpediente.ObtenerHistorial(ID_TRAMITE)).thenReturn(List.of());
        lenient().when(this.vgo_DalDecisionExoneracion.SIMT_DECISION_EXONERACION_ObtenerPorTramite(anyInt(), anyString()))
                .thenReturn(List.of());

        lenient().when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_JEFATURA))
                .thenReturn(this.Usuario(ID_JEFATURA, "M. Barrantes Jiménez", "jefatura@ucr.ac.cr"));
        lenient().when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_TECNICA))
                .thenReturn(this.Usuario(ID_TECNICA, "K. Valverde Fallas", "tecnica@ucr.ac.cr"));
        lenient().when(this.vgo_DalAutorizacion.SIMT_USUARIO_ROL_ObtenerPermisosActivos(ID_JEFATURA))
                .thenReturn(List.of(Permisos.EXONERACION_APROBAR));
        lenient().when(this.vgo_DalAutorizacion.SIMT_USUARIO_ROL_ObtenerPermisosActivos(ID_TECNICA))
                .thenReturn(List.of());

        EntHistorialEstado vlo_EnEnvio = new EntHistorialEstado();
        vlo_EnEnvio.setIdUsuario(ID_TECNICA);
        vlo_EnEnvio.setFechaHora(LocalDateTime.of(2026, 9, 3, 9, 0));
        lenient().when(this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_ObtenerUltimoIngreso(ID_TRAMITE, ID_ESTADO_APROBACION))
                .thenReturn(vlo_EnEnvio);

        // Simula los identificadores que asigna la base de datos.
        lenient().doAnswer(vlo_Invocacion -> {
            ((EntDecisionExoneracion) vlo_Invocacion.getArgument(0)).setIdDecision(900);
            return null;
        }).when(this.vgo_DalDecisionExoneracion).SIMT_DECISION_EXONERACION_InsertarRegistro(any());
    }

    @Test
    void RegistrarDecision_Aprobar_PasaAAprobadaRegistraHistorialBitacoraYNotifica() {
        ModeloAprobacionRespuesta vlo_Respuesta =
                this.vgo_Bll.RegistrarDecision(ID_TRAMITE, ID_JEFATURA, "APROBAR", null);

        assertEquals(ID_ESTADO_APROBADA, this.vgo_EnTramite.getIdEstadoTramite());
        assertNull(this.vgo_EnTramite.getFechaCierre());

        ArgumentCaptor<EntDecisionExoneracion> vlo_Decision = ArgumentCaptor.forClass(EntDecisionExoneracion.class);
        verify(this.vgo_DalDecisionExoneracion).SIMT_DECISION_EXONERACION_InsertarRegistro(vlo_Decision.capture());
        assertEquals(Constantes.DECISION_EXONERACION_APROBAR, vlo_Decision.getValue().getDecision());
        assertEquals(ID_JEFATURA, vlo_Decision.getValue().getIdUsuarioDecisor());
        assertEquals(ID_ESTADO_APROBACION, vlo_Decision.getValue().getIdEstadoAnterior());
        assertEquals(ID_ESTADO_APROBADA, vlo_Decision.getValue().getIdEstadoNuevo());
        assertNotNull(vlo_Decision.getValue().getFechaHora());

        ArgumentCaptor<EntHistorialEstado> vlo_Historial = ArgumentCaptor.forClass(EntHistorialEstado.class);
        verify(this.vgo_DalHistorialEstado).SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_Historial.capture());
        assertEquals(ID_JEFATURA, vlo_Historial.getValue().getIdUsuario());

        verify(this.vgo_BllBitacora).RegistrarAccion(eq(ID_TRAMITE), eq(ID_JEFATURA),
                eq(Constantes.ACCION_BITACORA_DECISION_EXONERACION), anyString(), eq(900), any(), anyString());
        verify(this.vgo_BllBitacora).RegistrarAccion(eq(ID_TRAMITE), eq(ID_JEFATURA),
                eq(Constantes.ACCION_BITACORA_CAMBIO_ESTADO), anyString(), eq(ID_TRAMITE), anyString(), anyString());

        ArgumentCaptor<SimpleMailMessage> vlo_Correo = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(this.vgo_Correo).send(vlo_Correo.capture());
        assertEquals("tecnica@ucr.ac.cr", vlo_Correo.getValue().getTo()[0]);

        ArgumentCaptor<EntNotificacionExoneracion> vlo_Notificacion =
                ArgumentCaptor.forClass(EntNotificacionExoneracion.class);
        verify(this.vgo_DalDecisionExoneracion)
                .SIMT_NOTIFICACION_EXONERACION_ModificarRegistro(vlo_Notificacion.capture());
        assertEquals(ID_TECNICA, vlo_Notificacion.getValue().getIdUsuarioDestino());
        assertEquals(Constantes.ESTADO_ENVIO_NOTIFICACION_ENVIADA, vlo_Notificacion.getValue().getEstadoEnvio());
        assertTrue(vlo_Respuesta.getMensaje().contains("Exoneración aprobada"));
    }

    @Test
    void RegistrarDecision_Rechazar_CierraElExpediente() {
        this.vgo_Bll.RegistrarDecision(ID_TRAMITE, ID_JEFATURA, "RECHAZAR", "No cumple el requisito de la ley.");

        assertEquals(ID_ESTADO_RECHAZADA, this.vgo_EnTramite.getIdEstadoTramite());
        assertNotNull(this.vgo_EnTramite.getFechaCierre());
    }

    @Test
    void RegistrarDecision_Modificar_VuelveALaTecnica() {
        this.vgo_Bll.RegistrarDecision(ID_TRAMITE, ID_JEFATURA, "MODIFICAR", "Corregir el formulario S1.");

        assertEquals(ID_ESTADO_MODIFICACION, this.vgo_EnTramite.getIdEstadoTramite());
        assertNull(this.vgo_EnTramite.getFechaCierre());
    }

    @Test
    void RegistrarDecision_UsuarioSinPermiso_Responde403YNoRegistra() {
        ExcepcionAutenticacion vlo_Excepcion = assertThrows(ExcepcionAutenticacion.class,
                () -> this.vgo_Bll.RegistrarDecision(ID_TRAMITE, ID_TECNICA, "APROBAR", null));

        assertEquals(HttpStatus.FORBIDDEN, vlo_Excepcion.getEstadoHttp());
        verify(this.vgo_DalDecisionExoneracion, never()).SIMT_DECISION_EXONERACION_InsertarRegistro(any());
        verify(this.vgo_DalTramite, never()).SIMT_TRAMITE_ModificarRegistro(any());
    }

    @Test
    void RegistrarDecision_AprobarExpedienteIncompleto_Rechaza() {
        this.vgo_Control.setCumpleDocumentacion(false);
        this.vgo_Control.setDocumentosPendientes(List.of("Formulario S1 (sin adjuntar)"));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_Bll.RegistrarDecision(ID_TRAMITE, ID_JEFATURA, "APROBAR", null));

        assertTrue(vlo_Excepcion.getMessage().startsWith(Constantes.MENSAJE_EXONERACION_INCOMPLETA_NO_APROBABLE));
        assertTrue(vlo_Excepcion.getMessage().contains("Formulario S1"));
        verify(this.vgo_DalDecisionExoneracion, never()).SIMT_DECISION_EXONERACION_InsertarRegistro(any());
        assertEquals(ID_ESTADO_APROBACION, this.vgo_EnTramite.getIdEstadoTramite());
    }

    @Test
    void RegistrarDecision_ExpedienteNoPendienteDeAprobacion_Rechaza() {
        this.vgo_EnTramite.setIdEstadoTramite(ID_ESTADO_APROBADA);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_Bll.RegistrarDecision(ID_TRAMITE, ID_JEFATURA, "RECHAZAR", "Motivo"));

        assertEquals(Constantes.MENSAJE_EXONERACION_NO_PENDIENTE_APROBACION, vlo_Excepcion.getMessage());
    }

    @Test
    void RegistrarDecision_RechazarSinJustificacion_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_Bll.RegistrarDecision(ID_TRAMITE, ID_JEFATURA, "RECHAZAR", "   "));

        assertEquals(Constantes.MENSAJE_JUSTIFICACION_DECISION_REQUERIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void RegistrarDecision_DecisionInvalida_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_Bll.RegistrarDecision(ID_TRAMITE, ID_JEFATURA, "ARCHIVAR", "x"));

        assertEquals(Constantes.MENSAJE_DECISION_INVALIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void RegistrarDecision_JustificacionDemasiadoLarga_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_Bll.RegistrarDecision(ID_TRAMITE, ID_JEFATURA, "MODIFICAR", "a".repeat(1001)));

        assertEquals(Constantes.MENSAJE_JUSTIFICACION_DECISION_INVALIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void RegistrarDecision_CorreoFalla_LaDecisionQuedaRegistradaYLaNotificacionConError() {
        doThrow(new MailSendException("SMTP no disponible")).when(this.vgo_Correo).send(any(SimpleMailMessage.class));

        ModeloAprobacionRespuesta vlo_Respuesta =
                this.vgo_Bll.RegistrarDecision(ID_TRAMITE, ID_JEFATURA, "APROBAR", null);

        assertEquals(ID_ESTADO_APROBADA, this.vgo_EnTramite.getIdEstadoTramite());
        ArgumentCaptor<EntNotificacionExoneracion> vlo_Notificacion =
                ArgumentCaptor.forClass(EntNotificacionExoneracion.class);
        verify(this.vgo_DalDecisionExoneracion)
                .SIMT_NOTIFICACION_EXONERACION_ModificarRegistro(vlo_Notificacion.capture());
        assertEquals(Constantes.ESTADO_ENVIO_NOTIFICACION_ERROR, vlo_Notificacion.getValue().getEstadoEnvio());
        assertTrue(vlo_Respuesta.getMensaje().contains("no se pudo enviar"));
    }

    @Test
    void ObtenerAprobacion_UsuarioSinPermiso_PuedeVerPeroNoDecidir() {
        ModeloAprobacionRespuesta vlo_Respuesta = this.vgo_Bll.ObtenerAprobacion(ID_TRAMITE, ID_TECNICA);

        assertFalse(vlo_Respuesta.getUsuarioAutorizado());
        assertFalse(vlo_Respuesta.getPuedeDecidir());
        assertEquals(Constantes.MENSAJE_USUARIO_NO_AUTORIZADO_APROBAR, vlo_Respuesta.getMotivoNoDecide());
        assertEquals("K. Valverde Fallas", vlo_Respuesta.getEnviadoPor());
        assertEquals(4, vlo_Respuesta.getDocumentosRecibidos());
    }

    @Test
    void ObtenerAprobacion_JefaturaConExpedientePendiente_PuedeDecidir() {
        ModeloAprobacionRespuesta vlo_Respuesta = this.vgo_Bll.ObtenerAprobacion(ID_TRAMITE, ID_JEFATURA);

        assertTrue(vlo_Respuesta.getPuedeDecidir());
        assertNull(vlo_Respuesta.getMotivoNoDecide());
    }

    @Test
    void ObtenerPendientes_UsuarioSinPermiso_Responde403() {
        ExcepcionAutenticacion vlo_Excepcion = assertThrows(ExcepcionAutenticacion.class,
                () -> this.vgo_Bll.ObtenerPendientes(ID_TECNICA));

        assertEquals(HttpStatus.FORBIDDEN, vlo_Excepcion.getEstadoHttp());
    }

    private void Estado(Integer pvn_Id, String pvc_Nombre, boolean pvb_EsFinal) {
        EntEstadoTramite vlo_EnEstado = new EntEstadoTramite();
        vlo_EnEstado.setIdEstadoTramite(pvn_Id);
        vlo_EnEstado.setIdTipoTramite(ID_TIPO_TRAMITE);
        vlo_EnEstado.setNombre(pvc_Nombre);
        vlo_EnEstado.setEsFinal(pvb_EsFinal);
        lenient().when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(pvn_Id)).thenReturn(vlo_EnEstado);
        lenient().when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                ID_TIPO_TRAMITE, pvc_Nombre, Constantes.ESTADO_CATALOGO_ACTIVO)).thenReturn(vlo_EnEstado);
    }

    private EntUsuario Usuario(Integer pvn_Id, String pvc_Nombre, String pvc_Correo) {
        EntUsuario vlo_EnUsuario = new EntUsuario();
        vlo_EnUsuario.setIdUsuario(pvn_Id);
        vlo_EnUsuario.setNombreCompleto(pvc_Nombre);
        vlo_EnUsuario.setCorreoInstitucional(pvc_Correo);
        return vlo_EnUsuario;
    }
}
