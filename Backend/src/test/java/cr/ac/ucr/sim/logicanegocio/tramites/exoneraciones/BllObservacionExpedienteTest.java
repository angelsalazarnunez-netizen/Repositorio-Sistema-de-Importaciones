package cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalBitacora;
import cr.ac.ucr.sim.accesodatos.tramites.DalExoneracion;
import cr.ac.ucr.sim.accesodatos.tramites.exoneraciones.DalObservacionExpediente;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntBitacora;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntObservacionExpediente;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 09/10/2026
 * Descripción: Pruebas de las reglas de las observaciones y aclaraciones del expediente de
 *   exoneración (HU-020): borradores privados de su autor, confirmación inmutable que pasa al
 *   historial y a la bitácora, y baja lógica de los borradores.
 */
@ExtendWith(MockitoExtension.class)
class BllObservacionExpedienteTest {

    private static final Integer ID_TRAMITE = 10;
    private static final Integer ID_OTRO_TRAMITE = 99;
    private static final Integer ID_AUTOR = 1;
    private static final Integer ID_OTRO_USUARIO = 2;
    private static final Integer ID_USUARIO_SIN_REGISTRO = 3;
    private static final Long ID_OBSERVACION = 500L;
    private static final LocalDateTime FECHA_CREACION = LocalDateTime.of(2026, 10, 1, 8, 30);

    @Mock
    private DalObservacionExpediente vgo_DalObservacionExpediente;

    @Mock
    private DalExoneracion vgo_DalExoneracion;

    @Mock
    private DalUsuario vgo_DalUsuario;

    @Mock
    private DalBitacora vgo_DalBitacora;

    @InjectMocks
    private BllObservacionExpediente vgo_BllObservacionExpediente;

    private EntObservacionExpediente vgo_EnBorrador;

    @BeforeEach
    void Preparar() {
        EntExoneracion vlo_EnExoneracion = new EntExoneracion();
        vlo_EnExoneracion.setIdTramite(ID_TRAMITE);
        lenient().when(this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(ID_TRAMITE))
                .thenReturn(vlo_EnExoneracion);

        lenient().when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_AUTOR))
                .thenReturn(this.Usuario(ID_AUTOR, "K. Valverde Fallas"));
        lenient().when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_OTRO_USUARIO))
                .thenReturn(this.Usuario(ID_OTRO_USUARIO, "M. Barrantes Jiménez"));

        this.vgo_EnBorrador = this.Observacion(ID_OBSERVACION, ID_TRAMITE, ID_AUTOR,
                Constantes.ESTADO_OBSERVACION_BORRADOR, Constantes.ESTADO_REGISTRO_OBSERVACION_ACTIVO);
        lenient().when(this.vgo_DalObservacionExpediente.SIMT_OBSERVACION_EXPED_ObtenerRegistroParaModificar(ID_OBSERVACION))
                .thenReturn(this.vgo_EnBorrador);

        lenient().doAnswer(vlo_Invocacion -> {
            ((EntObservacionExpediente) vlo_Invocacion.getArgument(0)).setIdObservacion(ID_OBSERVACION);
            return null;
        }).when(this.vgo_DalObservacionExpediente).SIMT_OBSERVACION_EXPED_InsertarRegistro(any());
    }

    @Test
    void ObtenerHistorial_ExoneracionValida_ConsultaSoloConfirmadasActivas() {
        List<EntObservacionExpediente> vlo_LsHistorial = List.of(this.vgo_EnBorrador);
        when(this.vgo_DalObservacionExpediente.SIMT_OBSERVACION_EXPED_ObtenerHistorialPorTramite(
                ID_TRAMITE, Constantes.ESTADO_OBSERVACION_CONFIRMADA, Constantes.ESTADO_REGISTRO_OBSERVACION_ACTIVO))
                .thenReturn(vlo_LsHistorial);

        assertSame(vlo_LsHistorial, this.vgo_BllObservacionExpediente.ObtenerHistorial(ID_TRAMITE));
    }

    @Test
    void ObtenerHistorial_SinTramite_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllObservacionExpediente.ObtenerHistorial(null));

        assertEquals(Constantes.MENSAJE_ID_TRAMITE_REQUERIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void ObtenerHistorial_TramiteQueNoEsExoneracion_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllObservacionExpediente.ObtenerHistorial(ID_OTRO_TRAMITE));

        assertEquals(Constantes.MENSAJE_EXONERACION_ID_NO_ENCONTRADA, vlo_Excepcion.getMessage());
    }

    @Test
    void ObtenerBorradoresPropios_ConsultaSoloLosBorradoresVigentesDelAutor() {
        this.vgo_BllObservacionExpediente.ObtenerBorradoresPropios(ID_TRAMITE, ID_AUTOR);

        verify(this.vgo_DalObservacionExpediente).SIMT_OBSERVACION_EXPED_ObtenerPorTramiteYAutor(
                ID_TRAMITE, ID_AUTOR,
                Constantes.ESTADO_OBSERVACION_BORRADOR, Constantes.ESTADO_REGISTRO_OBSERVACION_ACTIVO);
    }

    @Test
    void ObtenerBorradoresPropios_UsuarioNoRegistrado_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllObservacionExpediente.ObtenerBorradoresPropios(ID_TRAMITE, ID_USUARIO_SIN_REGISTRO));

        assertEquals(Constantes.MENSAJE_USUARIO_OBSERVACION_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void ObtenerNombresResponsables_ConsultaUnaSolaVezPorAutor() {
        List<EntObservacionExpediente> vlo_LsObservaciones = List.of(
                this.Observacion(1L, ID_TRAMITE, ID_AUTOR, Constantes.ESTADO_OBSERVACION_CONFIRMADA,
                        Constantes.ESTADO_REGISTRO_OBSERVACION_ACTIVO),
                this.Observacion(2L, ID_TRAMITE, ID_AUTOR, Constantes.ESTADO_OBSERVACION_CONFIRMADA,
                        Constantes.ESTADO_REGISTRO_OBSERVACION_ACTIVO),
                this.Observacion(3L, ID_TRAMITE, ID_USUARIO_SIN_REGISTRO, Constantes.ESTADO_OBSERVACION_CONFIRMADA,
                        Constantes.ESTADO_REGISTRO_OBSERVACION_ACTIVO));

        Map<Integer, String> vlo_MpNombres =
                this.vgo_BllObservacionExpediente.ObtenerNombresResponsables(vlo_LsObservaciones);

        assertEquals(2, vlo_MpNombres.size());
        assertEquals("K. Valverde Fallas", vlo_MpNombres.get(ID_AUTOR));
        assertEquals(Constantes.TEXTO_SIN_DATO, vlo_MpNombres.get(ID_USUARIO_SIN_REGISTRO));
        verify(this.vgo_DalUsuario, times(1)).SIMF_USUARIO_ObtenerRegistro(ID_AUTOR);
    }

    @Test
    void ObtenerNombresResponsables_ListaNula_DevuelveMapaVacio() {
        assertTrue(this.vgo_BllObservacionExpediente.ObtenerNombresResponsables(null).isEmpty());
    }


    @Test
    void CrearObservacion_SinConfirmar_QuedaComoBorradorYNoVaALaBitacora() {
        EntObservacionExpediente vlo_EnObservacion = this.vgo_BllObservacionExpediente.CrearObservacion(
                ID_TRAMITE, ID_AUTOR, "   ", "  Falta aclarar el valor CIF.  ", false);

        ArgumentCaptor<EntObservacionExpediente> vlo_Captor = ArgumentCaptor.forClass(EntObservacionExpediente.class);
        verify(this.vgo_DalObservacionExpediente).SIMT_OBSERVACION_EXPED_InsertarRegistro(vlo_Captor.capture());
        assertSame(vlo_EnObservacion, vlo_Captor.getValue());

        assertEquals(ID_TRAMITE, vlo_EnObservacion.getIdTramite());
        assertEquals(ID_AUTOR, vlo_EnObservacion.getIdUsuarioAutor());
        assertNull(vlo_EnObservacion.getMotivo());
        assertEquals("Falta aclarar el valor CIF.", vlo_EnObservacion.getObservacion());
        assertEquals(Constantes.ESTADO_OBSERVACION_BORRADOR, vlo_EnObservacion.getEstadoObservacion());
        assertEquals(Constantes.ESTADO_REGISTRO_OBSERVACION_ACTIVO, vlo_EnObservacion.getEstado());
        assertNotNull(vlo_EnObservacion.getFechaCreacion());
        assertNull(vlo_EnObservacion.getFechaConfirmacion());
        verify(this.vgo_DalBitacora, never()).SIML_BITACORA_InsertarRegistro(any());
    }

    @Test
    void CrearObservacion_Confirmada_PasaAlHistorialYQuedaEnLaBitacoraConLaMismaFecha() {
        EntObservacionExpediente vlo_EnObservacion = this.vgo_BllObservacionExpediente.CrearObservacion(
                ID_TRAMITE, ID_AUTOR, " Aclaración ", "Se adjuntó la factura corregida.", true);

        assertEquals(Constantes.ESTADO_OBSERVACION_CONFIRMADA, vlo_EnObservacion.getEstadoObservacion());
        assertNotNull(vlo_EnObservacion.getFechaConfirmacion());
        assertEquals(vlo_EnObservacion.getFechaCreacion(), vlo_EnObservacion.getFechaConfirmacion());

        ArgumentCaptor<EntBitacora> vlo_Captor = ArgumentCaptor.forClass(EntBitacora.class);
        verify(this.vgo_DalBitacora).SIML_BITACORA_InsertarRegistro(vlo_Captor.capture());
        EntBitacora vlo_EnBitacora = vlo_Captor.getValue();
        assertEquals(ID_TRAMITE, vlo_EnBitacora.getIdTramite());
        assertEquals(Long.valueOf(ID_AUTOR), vlo_EnBitacora.getIdUsuario());
        assertEquals(Constantes.ACCION_BITACORA_OBSERVACION_CONFIRMADA, vlo_EnBitacora.getAccion());
        assertEquals(Constantes.ENTIDAD_BITACORA_OBSERVACION, vlo_EnBitacora.getEntidadAfectada());
        assertEquals(500, vlo_EnBitacora.getIdRegistroAfectado());
        assertNull(vlo_EnBitacora.getDatosAnteriores());
        assertEquals(Constantes.PREFIJO_BITACORA_MOTIVO_OBSERVACION + "Aclaración. Se adjuntó la factura corregida.",
                vlo_EnBitacora.getDatosNuevos());
        assertEquals(Constantes.ESTADO_BITACORA_ACTIVO, vlo_EnBitacora.getEstado());
        assertEquals(vlo_EnObservacion.getFechaConfirmacion(), vlo_EnBitacora.getFechaHora());
    }

    @Test
    void CrearObservacion_TextoEnBlanco_RechazaSinRegistrar() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllObservacionExpediente.CrearObservacion(ID_TRAMITE, ID_AUTOR, null, "   ", false));

        assertEquals(Constantes.MENSAJE_OBSERVACION_INVALIDA, vlo_Excepcion.getMessage());
        verify(this.vgo_DalObservacionExpediente, never()).SIMT_OBSERVACION_EXPED_InsertarRegistro(any());
    }

    @Test
    void CrearObservacion_TextoEnElLimite_LoAcepta() {
        this.vgo_BllObservacionExpediente.CrearObservacion(ID_TRAMITE, ID_AUTOR, null,
                "a".repeat(Constantes.LONGITUD_MAXIMA_OBSERVACION_EXPEDIENTE), false);

        verify(this.vgo_DalObservacionExpediente).SIMT_OBSERVACION_EXPED_InsertarRegistro(any());
    }

    @Test
    void CrearObservacion_TextoMayorAlMaximo_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllObservacionExpediente.CrearObservacion(ID_TRAMITE, ID_AUTOR, null,
                        "a".repeat(Constantes.LONGITUD_MAXIMA_OBSERVACION_EXPEDIENTE + 1), false));

        assertEquals(Constantes.MENSAJE_OBSERVACION_INVALIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void CrearObservacion_MotivoMayorAlMaximo_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllObservacionExpediente.CrearObservacion(ID_TRAMITE, ID_AUTOR,
                        "m".repeat(Constantes.LONGITUD_MAXIMA_MOTIVO_OBSERVACION + 1), "Texto válido", false));

        assertEquals(Constantes.MENSAJE_MOTIVO_OBSERVACION_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void CrearObservacion_UsuarioNoRegistrado_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllObservacionExpediente.CrearObservacion(
                        ID_TRAMITE, ID_USUARIO_SIN_REGISTRO, null, "Texto", false));

        assertEquals(Constantes.MENSAJE_USUARIO_OBSERVACION_INVALIDO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalObservacionExpediente, never()).SIMT_OBSERVACION_EXPED_InsertarRegistro(any());
    }


    @Test
    void ModificarBorrador_Propio_CambiaTextoSinTocarAutorNiFechaDeCreacion() {
        EntObservacionExpediente vlo_EnModificado = this.vgo_BllObservacionExpediente.ModificarBorrador(
                ID_TRAMITE, ID_OBSERVACION, ID_AUTOR, "Corrección", "  Texto corregido.  ");

        assertEquals("Corrección", vlo_EnModificado.getMotivo());
        assertEquals("Texto corregido.", vlo_EnModificado.getObservacion());
        assertEquals(ID_AUTOR, vlo_EnModificado.getIdUsuarioAutor());
        assertEquals(FECHA_CREACION, vlo_EnModificado.getFechaCreacion());
        assertNotNull(vlo_EnModificado.getFechaModificacion());
        assertEquals(Constantes.ESTADO_OBSERVACION_BORRADOR, vlo_EnModificado.getEstadoObservacion());
        verify(this.vgo_DalObservacionExpediente).SIMT_OBSERVACION_EXPED_ModificarRegistro(this.vgo_EnBorrador);
    }

    @Test
    void ModificarBorrador_DeOtroUsuario_NoRevelaQueExiste() {
        this.AfirmarNoModifica(ID_OBSERVACION, ID_OTRO_USUARIO, Constantes.MENSAJE_BORRADOR_NO_DISPONIBLE);
    }

    @Test
    void ModificarBorrador_Confirmado_InformaQueEsInmutable() {
        this.vgo_EnBorrador.setEstadoObservacion(Constantes.ESTADO_OBSERVACION_CONFIRMADA);

        this.AfirmarNoModifica(ID_OBSERVACION, ID_AUTOR, Constantes.MENSAJE_OBSERVACION_CONFIRMADA_INMUTABLE);
    }

    @Test
    void ModificarBorrador_DeOtroExpediente_NoEstaDisponible() {
        this.vgo_EnBorrador.setIdTramite(ID_OTRO_TRAMITE);

        this.AfirmarNoModifica(ID_OBSERVACION, ID_AUTOR, Constantes.MENSAJE_BORRADOR_NO_DISPONIBLE);
    }

    @Test
    void ModificarBorrador_Eliminado_NoEstaDisponible() {
        this.vgo_EnBorrador.setEstado(Constantes.ESTADO_REGISTRO_OBSERVACION_INACTIVO);

        this.AfirmarNoModifica(ID_OBSERVACION, ID_AUTOR, Constantes.MENSAJE_BORRADOR_NO_DISPONIBLE);
    }

    @Test
    void ModificarBorrador_Inexistente_NoEstaDisponible() {
        this.AfirmarNoModifica(999L, ID_AUTOR, Constantes.MENSAJE_BORRADOR_NO_DISPONIBLE);
    }

    @Test
    void ModificarBorrador_SinIdentificador_Rechaza() {
        this.AfirmarNoModifica(null, ID_AUTOR, Constantes.MENSAJE_ID_OBSERVACION_REQUERIDO);
    }


    @Test
    void EliminarBorrador_Propio_LoMarcaInactivoSinBorrarloNiAnotarEnBitacora() {
        this.vgo_BllObservacionExpediente.EliminarBorrador(ID_TRAMITE, ID_OBSERVACION, ID_AUTOR);

        assertEquals(Constantes.ESTADO_REGISTRO_OBSERVACION_INACTIVO, this.vgo_EnBorrador.getEstado());
        assertNotNull(this.vgo_EnBorrador.getFechaModificacion());
        verify(this.vgo_DalObservacionExpediente).SIMT_OBSERVACION_EXPED_ModificarRegistro(this.vgo_EnBorrador);
        verify(this.vgo_DalBitacora, never()).SIML_BITACORA_InsertarRegistro(any());
    }

    @Test
    void EliminarBorrador_Confirmado_Rechaza() {
        this.vgo_EnBorrador.setEstadoObservacion(Constantes.ESTADO_OBSERVACION_CONFIRMADA);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllObservacionExpediente.EliminarBorrador(ID_TRAMITE, ID_OBSERVACION, ID_AUTOR));

        assertEquals(Constantes.MENSAJE_OBSERVACION_CONFIRMADA_INMUTABLE, vlo_Excepcion.getMessage());
        assertEquals(Constantes.ESTADO_REGISTRO_OBSERVACION_ACTIVO, this.vgo_EnBorrador.getEstado());
    }

    @Test
    void ConfirmarBorrador_Propio_PasaAConfirmadaYRegistraLaBitacora() {
        this.vgo_EnBorrador.setMotivo(null);
        this.vgo_EnBorrador.setObservacion("Se solicitó el formulario S1.");

        EntObservacionExpediente vlo_EnConfirmada =
                this.vgo_BllObservacionExpediente.ConfirmarBorrador(ID_TRAMITE, ID_OBSERVACION, ID_AUTOR);

        assertEquals(Constantes.ESTADO_OBSERVACION_CONFIRMADA, vlo_EnConfirmada.getEstadoObservacion());
        assertNotNull(vlo_EnConfirmada.getFechaConfirmacion());
        verify(this.vgo_DalObservacionExpediente).SIMT_OBSERVACION_EXPED_ModificarRegistro(this.vgo_EnBorrador);

        ArgumentCaptor<EntBitacora> vlo_Captor = ArgumentCaptor.forClass(EntBitacora.class);
        verify(this.vgo_DalBitacora).SIML_BITACORA_InsertarRegistro(vlo_Captor.capture());
        assertEquals("Se solicitó el formulario S1.", vlo_Captor.getValue().getDatosNuevos());
        assertEquals(vlo_EnConfirmada.getFechaConfirmacion(), vlo_Captor.getValue().getFechaHora());
    }

    @Test
    void ConfirmarBorrador_YaConfirmado_RechazaSinDuplicarLaBitacora() {
        this.vgo_EnBorrador.setEstadoObservacion(Constantes.ESTADO_OBSERVACION_CONFIRMADA);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllObservacionExpediente.ConfirmarBorrador(ID_TRAMITE, ID_OBSERVACION, ID_AUTOR));

        assertEquals(Constantes.MENSAJE_OBSERVACION_CONFIRMADA_INMUTABLE, vlo_Excepcion.getMessage());
        verify(this.vgo_DalBitacora, never()).SIML_BITACORA_InsertarRegistro(any());
    }

    @Test
    void ConfirmarBorrador_DeOtroUsuario_NoEstaDisponible() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllObservacionExpediente.ConfirmarBorrador(ID_TRAMITE, ID_OBSERVACION, ID_OTRO_USUARIO));

        assertEquals(Constantes.MENSAJE_BORRADOR_NO_DISPONIBLE, vlo_Excepcion.getMessage());
        assertEquals(Constantes.ESTADO_OBSERVACION_BORRADOR, this.vgo_EnBorrador.getEstadoObservacion());
    }


    private void AfirmarNoModifica(Long pvn_IdObservacion, Integer pvn_IdUsuario, String pvc_Mensaje) {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllObservacionExpediente.ModificarBorrador(
                        ID_TRAMITE, pvn_IdObservacion, pvn_IdUsuario, null, "Texto nuevo"));
        assertEquals(pvc_Mensaje, vlo_Excepcion.getMessage());
        verify(this.vgo_DalObservacionExpediente, never()).SIMT_OBSERVACION_EXPED_ModificarRegistro(any());
    }

    private EntObservacionExpediente Observacion(
            Long pvn_Id, Integer pvn_IdTramite, Integer pvn_IdAutor, String pvc_EstadoObservacion, String pvc_Estado) {
        EntObservacionExpediente vlo_EnObservacion = new EntObservacionExpediente();
        vlo_EnObservacion.setIdObservacion(pvn_Id);
        vlo_EnObservacion.setIdTramite(pvn_IdTramite);
        vlo_EnObservacion.setIdUsuarioAutor(pvn_IdAutor);
        vlo_EnObservacion.setMotivo("Motivo original");
        vlo_EnObservacion.setObservacion("Texto original");
        vlo_EnObservacion.setFechaCreacion(FECHA_CREACION);
        vlo_EnObservacion.setEstadoObservacion(pvc_EstadoObservacion);
        vlo_EnObservacion.setEstado(pvc_Estado);
        return vlo_EnObservacion;
    }

    private EntUsuario Usuario(Integer pvn_Id, String pvc_Nombre) {
        EntUsuario vlo_EnUsuario = new EntUsuario();
        vlo_EnUsuario.setIdUsuario(pvn_Id);
        vlo_EnUsuario.setNombreCompleto(pvc_Nombre);
        return vlo_EnUsuario;
    }
}
