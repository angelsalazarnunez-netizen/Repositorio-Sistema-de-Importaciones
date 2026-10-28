package cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalExoneracion;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.accesodatos.tramites.exoneraciones.DalAutorizacionExonet;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntAutorizacionExonet;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.logicanegocio.tramites.BllBitacora;
import cr.ac.ucr.sim.logicanegocio.tramites.BllDocumento;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 09/10/2026
 * Descripción: Pruebas de las reglas de las solicitudes / autorizaciones EXONET (HU-023): una sola
 *   vigente por expediente, completar sin sobrescribir, anulación y sustitución que conservan el
 *   historial, validación de los datos y orden de los bloqueos.
 */
@ExtendWith(MockitoExtension.class)
class BllAutorizacionExonetTest {

    private static final Integer ID_TRAMITE = 10;
    private static final Integer ID_OTRO_TRAMITE = 99;
    private static final Integer ID_USUARIO = 1;
    private static final Integer ID_OTRO_USUARIO = 2;
    private static final Integer ID_USUARIO_SIN_REGISTRO = 3;
    private static final Long ID_VIGENTE = 700L;
    private static final Long ID_NUEVA = 701L;
    private static final Integer ID_DOCUMENTO = 300;
    private static final LocalDate HOY = LocalDate.now();
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern(Constantes.FORMATO_FECHA_BITACORA_AUTORIZACION);

    @Mock private DalAutorizacionExonet vgo_DalAutorizacionExonet;
    @Mock private DalExoneracion vgo_DalExoneracion;
    @Mock private DalTramite vgo_DalTramite;
    @Mock private DalDocumento vgo_DalDocumento;
    @Mock private DalUsuario vgo_DalUsuario;
    @Mock private BllDocumento vgo_BllDocumento;
    @Mock private BllBitacora vgo_BllBitacora;

    @InjectMocks
    private BllAutorizacionExonet vgo_BllAutorizacionExonet;

    private EntExoneracion vgo_EnExoneracion;
    private EntAutorizacionExonet vgo_EnVigente;

    @BeforeEach
    void Preparar() {
        this.vgo_EnExoneracion = new EntExoneracion();
        this.vgo_EnExoneracion.setIdTramite(ID_TRAMITE);
        lenient().when(this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(ID_TRAMITE))
                .thenReturn(this.vgo_EnExoneracion);

        lenient().when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_USUARIO))
                .thenReturn(this.Usuario(ID_USUARIO, "K. Valverde Fallas"));
        lenient().when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_OTRO_USUARIO))
                .thenReturn(this.Usuario(ID_OTRO_USUARIO, "M. Barrantes Jiménez"));

        // El DAL devuelve la fila bloqueada del expediente; a la regla solo le importa que no sea nula,
        // así que se responde con un objeto simulado del tipo que declare el método.
        lenient().when(this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_BloquearExpediente(ID_TRAMITE))
                .thenAnswer(Answers.RETURNS_MOCKS);

        this.vgo_EnVigente = new EntAutorizacionExonet();
        this.vgo_EnVigente.setIdAutorizacionExonet(ID_VIGENTE);
        this.vgo_EnVigente.setIdTramite(ID_TRAMITE);
        this.vgo_EnVigente.setEstadoAutorizacion(Constantes.ESTADO_AUTORIZACION_VIGENTE);
        this.vgo_EnVigente.setNumeroSolicitudExonet("EXO-1");
        this.vgo_EnVigente.setFechaEnvioExonet(HOY.minusDays(10));
        this.vgo_EnVigente.setIdUsuarioRegistro(ID_USUARIO);
        this.vgo_EnVigente.setFechaRegistro(LocalDateTime.now().minusDays(10).withNano(0));
        lenient().when(this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ObtenerRegistroParaModificar(ID_VIGENTE))
                .thenReturn(this.vgo_EnVigente);

        lenient().doAnswer(vlo_Invocacion -> {
            ((EntAutorizacionExonet) vlo_Invocacion.getArgument(0)).setIdAutorizacionExonet(ID_NUEVA);
            return null;
        }).when(this.vgo_DalAutorizacionExonet).SIMT_AUTORIZACION_EXONET_InsertarRegistro(any());
    }


    @Test
    void ObtenerHistorial_TramiteQueNoEsExoneracion_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.ObtenerHistorial(ID_OTRO_TRAMITE));

        assertEquals(Constantes.MENSAJE_EXONERACION_ID_NO_ENCONTRADA, vlo_Excepcion.getMessage());
    }

    @Test
    void ObtenerVigente_ConsultaSoloLaAutorizacionVigente() {
        when(this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ObtenerVigentePorTramite(
                ID_TRAMITE, Constantes.ESTADO_AUTORIZACION_VIGENTE)).thenReturn(this.vgo_EnVigente);

        assertSame(this.vgo_EnVigente, this.vgo_BllAutorizacionExonet.ObtenerVigente(ID_TRAMITE));
    }

    @Test
    void ObtenerPrecargaHistorica_SinHistorialYConNumerosAntiguos_LosOfrece() {
        this.vgo_EnExoneracion.setNumeroExonet("EXO-2026-15");
        when(this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ContarPorTramite(ID_TRAMITE)).thenReturn(0L);

        assertSame(this.vgo_EnExoneracion, this.vgo_BllAutorizacionExonet.ObtenerPrecargaHistorica(ID_TRAMITE));
    }

    @Test
    void ObtenerPrecargaHistorica_ConHistorial_NoPrecarga() {
        this.vgo_EnExoneracion.setNumeroExonet("EXO-2026-15");
        when(this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ContarPorTramite(ID_TRAMITE)).thenReturn(1L);

        assertNull(this.vgo_BllAutorizacionExonet.ObtenerPrecargaHistorica(ID_TRAMITE));
    }

    @Test
    void ObtenerPrecargaHistorica_NumerosSinDato_NoPrecarga() {
        this.vgo_EnExoneracion.setNumeroExonet(" " + Constantes.VALOR_SIN_DATO_NO_APLICA.toLowerCase() + " ");
        this.vgo_EnExoneracion.setNumeroAutorizacionExonet("   ");
        when(this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ContarPorTramite(ID_TRAMITE)).thenReturn(0L);

        assertNull(this.vgo_BllAutorizacionExonet.ObtenerPrecargaHistorica(ID_TRAMITE));
    }

    @Test
    void ObtenerNombresUsuarios_ConsultaUnaSolaVezPorUsuario() {
        EntAutorizacionExonet vlo_EnSustituida = new EntAutorizacionExonet();
        vlo_EnSustituida.setIdUsuarioRegistro(ID_USUARIO);
        vlo_EnSustituida.setIdUsuarioCambioEstado(ID_OTRO_USUARIO);

        Map<Integer, String> vlo_MpNombres = this.vgo_BllAutorizacionExonet
                .ObtenerNombresUsuarios(List.of(vlo_EnSustituida, this.vgo_EnVigente));

        assertEquals(2, vlo_MpNombres.size());
        assertEquals("K. Valverde Fallas", vlo_MpNombres.get(ID_USUARIO));
        assertEquals("M. Barrantes Jiménez", vlo_MpNombres.get(ID_OTRO_USUARIO));
        verify(this.vgo_DalUsuario, times(1)).SIMF_USUARIO_ObtenerRegistro(ID_USUARIO);
    }

    @Test
    void ObtenerDocumentos_ConsultaUnaSolaVezPorDocumentoYOmiteLosInexistentes() {
        lenient().when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(ID_DOCUMENTO))
                .thenReturn(this.Documento(ID_DOCUMENTO, "autorizacion.pdf"));
        EntAutorizacionExonet vlo_EnPrimera = new EntAutorizacionExonet();
        vlo_EnPrimera.setIdDocumento(ID_DOCUMENTO);
        EntAutorizacionExonet vlo_EnSegunda = new EntAutorizacionExonet();
        vlo_EnSegunda.setIdDocumento(ID_DOCUMENTO);
        EntAutorizacionExonet vlo_EnTercera = new EntAutorizacionExonet();
        vlo_EnTercera.setIdDocumento(301);

        Map<Integer, EntDocumento> vlo_MpDocumentos = this.vgo_BllAutorizacionExonet
                .ObtenerDocumentos(List.of(vlo_EnPrimera, vlo_EnSegunda, vlo_EnTercera, this.vgo_EnVigente));

        assertEquals(1, vlo_MpDocumentos.size());
        assertEquals("autorizacion.pdf", vlo_MpDocumentos.get(ID_DOCUMENTO).getNombreArchivo());
        verify(this.vgo_DalDocumento, times(1)).SIMF_DOCUMENTO_ObtenerRegistro(ID_DOCUMENTO);
    }


    @Test
    void RegistrarAutorizacion_SoloSolicitud_CreaVigenteNormalizadaYRegistraBitacora() {
        EntAutorizacionExonet vlo_EnNueva = this.vgo_BllAutorizacionExonet.RegistrarAutorizacion(
                ID_TRAMITE, ID_USUARIO, "  exo-123  ", HOY.minusDays(1),
                null, null, null, Constantes.VALOR_SIN_DATO_NULO, null);

        ArgumentCaptor<EntAutorizacionExonet> vlo_Captor = ArgumentCaptor.forClass(EntAutorizacionExonet.class);
        verify(this.vgo_DalAutorizacionExonet).SIMT_AUTORIZACION_EXONET_InsertarRegistro(vlo_Captor.capture());
        assertSame(vlo_EnNueva, vlo_Captor.getValue());

        assertEquals(ID_TRAMITE, vlo_EnNueva.getIdTramite());
        assertEquals("EXO-123", vlo_EnNueva.getNumeroSolicitudExonet());
        assertEquals(HOY.minusDays(1), vlo_EnNueva.getFechaEnvioExonet());
        assertNull(vlo_EnNueva.getObservaciones());
        assertEquals(Constantes.ESTADO_AUTORIZACION_VIGENTE, vlo_EnNueva.getEstadoAutorizacion());
        assertNull(vlo_EnNueva.getIdAutorizacionAnterior());
        assertEquals(ID_USUARIO, vlo_EnNueva.getIdUsuarioRegistro());
        assertNotNull(vlo_EnNueva.getFechaRegistro());
        assertEquals(0, vlo_EnNueva.getFechaRegistro().getNano());
        assertNull(vlo_EnNueva.getIdDocumento());

        verify(this.vgo_BllDocumento, never()).GuardarDocumento(any(), any(), any(), any(), anyList());
        verify(this.vgo_BllBitacora).RegistrarAccion(
                eq(ID_TRAMITE), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_AUTORIZACION_REGISTRADA),
                eq(Constantes.ENTIDAD_BITACORA_AUTORIZACION_EXONET),
                eq(701), isNull(), contains("EXO-123"));
    }

    @Test
    void RegistrarAutorizacion_ConDocumento_LoGuardaYLoAsociaALaAutorizacion() {
        MockMultipartFile vlo_Archivo = this.Archivo();
        EntTramite vlo_EnTramite = new EntTramite();
        vlo_EnTramite.setIdTramite(ID_TRAMITE);
        EntDocumento vlo_EnDocumento = this.Documento(ID_DOCUMENTO, "autorizacion.pdf");
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistro(ID_TRAMITE)).thenReturn(vlo_EnTramite);
        when(this.vgo_BllDocumento.GuardarDocumento(eq(vlo_EnTramite), eq(ID_USUARIO), eq(vlo_Archivo),
                eq(Constantes.CODIGO_TIPO_DOCUMENTO_AUTORIZACION_EXONET), anyList())).thenReturn(vlo_EnDocumento);
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(ID_DOCUMENTO)).thenReturn(vlo_EnDocumento);

        EntAutorizacionExonet vlo_EnNueva = this.vgo_BllAutorizacionExonet.RegistrarAutorizacion(
                ID_TRAMITE, ID_USUARIO, "EXO-123", null, "AUT-9", HOY.minusDays(1), HOY, null, vlo_Archivo);

        assertEquals(ID_DOCUMENTO, vlo_EnNueva.getIdDocumento());
        verify(this.vgo_BllDocumento).RegistrarLimpiezaSiFallaLaTransaccion(anyList());
        verify(this.vgo_DalAutorizacionExonet).SIMT_AUTORIZACION_EXONET_ModificarRegistro(vlo_EnNueva);
        verify(this.vgo_BllBitacora).RegistrarAccion(
                eq(ID_TRAMITE), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_AUTORIZACION_REGISTRADA),
                eq(Constantes.ENTIDAD_BITACORA_AUTORIZACION_EXONET),
                eq(701), isNull(), contains("autorizacion.pdf"));
    }

    @Test
    void RegistrarAutorizacion_YaExisteUnaVigente_RechazaSinInsertar() {
        when(this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ObtenerVigentePorTramite(
                ID_TRAMITE, Constantes.ESTADO_AUTORIZACION_VIGENTE)).thenReturn(this.vgo_EnVigente);

        this.AfirmarRegistroRechazado(Constantes.MENSAJE_AUTORIZACION_VIGENTE_EXISTENTE,
                "EXO-2", null, null, null, null);
    }

    @Test
    void RegistrarAutorizacion_SinNingunIdentificador_Rechaza() {
        this.AfirmarRegistroRechazado(Constantes.MENSAJE_IDENTIFICADOR_EXONET_REQUERIDO,
                Constantes.VALOR_SIN_DATO_NO_APLICA, null, "   ", null, null);
    }

    @Test
    void RegistrarAutorizacion_FechaEnvioSinSolicitud_Rechaza() {
        this.AfirmarRegistroRechazado(Constantes.MENSAJE_FECHA_ENVIO_SIN_SOLICITUD_EXONET,
                null, HOY.minusDays(3), "AUT-9", HOY.minusDays(1), null);
    }

    @Test
    void RegistrarAutorizacion_FechaFutura_Rechaza() {
        this.AfirmarRegistroRechazado(Constantes.MENSAJE_FECHA_AUTORIZACION_FUTURA,
                "EXO-2", HOY.plusDays(1), null, null, null);
    }

    @Test
    void RegistrarAutorizacion_NumeroDeAutorizacionSinFecha_Rechaza() {
        this.AfirmarRegistroRechazado(Constantes.MENSAJE_AUTORIZACION_SIN_FECHA_O_NUMERO,
                "EXO-2", null, "AUT-9", null, null);
    }

    @Test
    void RegistrarAutorizacion_ComunicacionSinAutorizacion_Rechaza() {
        this.AfirmarRegistroRechazado(Constantes.MENSAJE_FECHA_COMUNICACION_SIN_AUTORIZACION,
                "EXO-2", null, null, null, HOY);
    }

    @Test
    void RegistrarAutorizacion_AutorizacionAnteriorAlEnvio_Rechaza() {
        this.AfirmarRegistroRechazado(Constantes.MENSAJE_ORDEN_FECHAS_AUTORIZACION,
                "EXO-2", HOY.minusDays(1), "AUT-9", HOY.minusDays(5), null);
    }

    @Test
    void RegistrarAutorizacion_UsuarioNoRegistrado_RechazaSinBloquear() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.RegistrarAutorizacion(
                        ID_TRAMITE, ID_USUARIO_SIN_REGISTRO, "EXO-2", null, null, null, null, null, null));

        assertEquals(Constantes.MENSAJE_USUARIO_AUTORIZACION_INVALIDO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalAutorizacionExonet, never()).SIMT_AUTORIZACION_EXONET_BloquearExpediente(any());
    }

    @Test
    void RegistrarAutorizacion_SinTramite_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.RegistrarAutorizacion(
                        null, ID_USUARIO, "EXO-2", null, null, null, null, null, null));

        assertEquals(Constantes.MENSAJE_ID_TRAMITE_REQUERIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void RegistrarAutorizacion_ExpedienteInexistente_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.RegistrarAutorizacion(
                        ID_OTRO_TRAMITE, ID_USUARIO, "EXO-2", null, null, null, null, null, null));

        assertEquals(Constantes.MENSAJE_EXONERACION_ID_NO_ENCONTRADA, vlo_Excepcion.getMessage());
    }


    @Test
    void CompletarAutorizacion_DatosVacios_LosCompletaIgnorandoLosIgualesYRegistraBitacora() {
        LocalDate vld_FechaAutorizacion = HOY.minusDays(2);

        EntAutorizacionExonet vlo_EnCompletada = this.vgo_BllAutorizacionExonet.CompletarAutorizacion(
                ID_TRAMITE, ID_VIGENTE, ID_USUARIO, "exo-1", null, " aut-9 ", vld_FechaAutorizacion,
                null, null, null);

        assertSame(this.vgo_EnVigente, vlo_EnCompletada);
        assertEquals("EXO-1", vlo_EnCompletada.getNumeroSolicitudExonet());
        assertEquals("AUT-9", vlo_EnCompletada.getNumeroAutorizacion());
        assertEquals(vld_FechaAutorizacion, vlo_EnCompletada.getFechaAutorizacion());
        verify(this.vgo_DalAutorizacionExonet).SIMT_AUTORIZACION_EXONET_ModificarRegistro(this.vgo_EnVigente);

        ArgumentCaptor<String> vlo_Anteriores = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> vlo_Nuevos = ArgumentCaptor.forClass(String.class);
        verify(this.vgo_BllBitacora).RegistrarAccion(
                eq(ID_TRAMITE), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_AUTORIZACION_COMPLETADA),
                eq(Constantes.ENTIDAD_BITACORA_AUTORIZACION_EXONET),
                eq(700), vlo_Anteriores.capture(), vlo_Nuevos.capture());
        String vlc_Prefijo = Constantes.PREFIJO_BITACORA_AUTORIZACION + ID_VIGENTE + ". ";
        assertTrue(vlo_Anteriores.getValue().startsWith(vlc_Prefijo));
        assertTrue(vlo_Anteriores.getValue().contains(
                Constantes.CAMPO_AUTORIZACION_NUMERO_AUTORIZACION + ": " + Constantes.TEXTO_SIN_DATO + "."));
        assertTrue(vlo_Nuevos.getValue().startsWith(vlc_Prefijo));
        assertTrue(vlo_Nuevos.getValue().contains(Constantes.CAMPO_AUTORIZACION_NUMERO_AUTORIZACION + ": AUT-9."));
        assertTrue(vlo_Nuevos.getValue().contains(Constantes.CAMPO_AUTORIZACION_FECHA_AUTORIZACION + ": "
                + vld_FechaAutorizacion.format(FORMATO_FECHA) + "."));
    }

    @Test
    void CompletarAutorizacion_DatoYaRegistradoDistinto_RechazaSinSobrescribir() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.CompletarAutorizacion(
                        ID_TRAMITE, ID_VIGENTE, ID_USUARIO, "EXO-2", null, null, null, null, null, null));

        assertEquals(String.format(Constantes.MENSAJE_DATO_AUTORIZACION_YA_REGISTRADO,
                Constantes.CAMPO_AUTORIZACION_NUMERO_SOLICITUD), vlo_Excepcion.getMessage());
        assertEquals("EXO-1", this.vgo_EnVigente.getNumeroSolicitudExonet());
        verify(this.vgo_DalAutorizacionExonet, never()).SIMT_AUTORIZACION_EXONET_ModificarRegistro(any());
    }

    @Test
    void CompletarAutorizacion_MismosDatos_EsIdempotenteYNoEscribe() {
        EntAutorizacionExonet vlo_EnResultado = this.vgo_BllAutorizacionExonet.CompletarAutorizacion(
                ID_TRAMITE, ID_VIGENTE, ID_USUARIO, "EXO-1", HOY.minusDays(10), null, null, null, null, null);

        assertSame(this.vgo_EnVigente, vlo_EnResultado);
        verify(this.vgo_DalAutorizacionExonet, never()).SIMT_AUTORIZACION_EXONET_ModificarRegistro(any());
        verify(this.vgo_BllBitacora, never())
                .RegistrarAccion(anyInt(), anyInt(), anyString(), anyString(), anyInt(), any(), any());
    }

    @Test
    void CompletarAutorizacion_SinDatos_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.CompletarAutorizacion(
                        ID_TRAMITE, ID_VIGENTE, ID_USUARIO, null, null, "  ", null, null,
                        Constantes.VALOR_SIN_DATO_NULO, null));

        assertEquals(Constantes.MENSAJE_AUTORIZACION_SIN_DATOS_PENDIENTES, vlo_Excepcion.getMessage());
    }

    @Test
    void CompletarAutorizacion_PropuestaInvalida_NoModificaLaEntidad() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.CompletarAutorizacion(
                        ID_TRAMITE, ID_VIGENTE, ID_USUARIO, null, null, "AUT-9", null, null, null, null));

        assertEquals(Constantes.MENSAJE_AUTORIZACION_SIN_FECHA_O_NUMERO, vlo_Excepcion.getMessage());
        assertNull(this.vgo_EnVigente.getNumeroAutorizacion());
        verify(this.vgo_DalAutorizacionExonet, never()).SIMT_AUTORIZACION_EXONET_ModificarRegistro(any());
    }

    @Test
    void CompletarAutorizacion_DocumentoYaAdjunto_Rechaza() {
        this.vgo_EnVigente.setIdDocumento(ID_DOCUMENTO);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.CompletarAutorizacion(
                        ID_TRAMITE, ID_VIGENTE, ID_USUARIO, null, null, null, null, null, null, this.Archivo()));

        assertEquals(String.format(Constantes.MENSAJE_DATO_AUTORIZACION_YA_REGISTRADO,
                Constantes.CAMPO_AUTORIZACION_DOCUMENTO), vlo_Excepcion.getMessage());
        verify(this.vgo_BllDocumento, never()).GuardarDocumento(any(), any(), any(), any(), anyList());
    }

    @Test
    void CompletarAutorizacion_AutorizacionDeOtroExpediente_NoEstaDisponible() {
        this.vgo_EnVigente.setIdTramite(ID_OTRO_TRAMITE);

        this.AfirmarCompletarRechazado(ID_VIGENTE, Constantes.MENSAJE_AUTORIZACION_NO_DISPONIBLE);
    }

    @Test
    void CompletarAutorizacion_AutorizacionAnulada_Rechaza() {
        this.vgo_EnVigente.setEstadoAutorizacion(Constantes.ESTADO_AUTORIZACION_ANULADA);

        this.AfirmarCompletarRechazado(ID_VIGENTE, Constantes.MENSAJE_AUTORIZACION_NO_VIGENTE);
    }

    @Test
    void CompletarAutorizacion_SinIdentificadorDeAutorizacion_Rechaza() {
        this.AfirmarCompletarRechazado(null, Constantes.MENSAJE_ID_AUTORIZACION_REQUERIDO);
    }


    @Test
    void AnularAutorizacion_ConMotivo_LaMarcaAnuladaConservandoSusDatos() {
        EntAutorizacionExonet vlo_EnAnulada = this.vgo_BllAutorizacionExonet.AnularAutorizacion(
                ID_TRAMITE, ID_VIGENTE, ID_OTRO_USUARIO, "  Número registrado por error  ");

        assertEquals(Constantes.ESTADO_AUTORIZACION_ANULADA, vlo_EnAnulada.getEstadoAutorizacion());
        assertEquals(ID_OTRO_USUARIO, vlo_EnAnulada.getIdUsuarioCambioEstado());
        assertNotNull(vlo_EnAnulada.getFechaCambioEstado());
        assertEquals("Número registrado por error", vlo_EnAnulada.getMotivoCambioEstado());
        assertEquals("EXO-1", vlo_EnAnulada.getNumeroSolicitudExonet());
        verify(this.vgo_DalAutorizacionExonet).SIMT_AUTORIZACION_EXONET_ModificarRegistro(this.vgo_EnVigente);
        verify(this.vgo_BllBitacora).RegistrarAccion(
                eq(ID_TRAMITE), eq(ID_OTRO_USUARIO),
                eq(Constantes.ACCION_BITACORA_AUTORIZACION_ANULADA),
                eq(Constantes.ENTIDAD_BITACORA_AUTORIZACION_EXONET),
                eq(700), contains("EXO-1"), contains("Número registrado por error"));
    }

    @Test
    void AnularAutorizacion_MotivoSinDato_RechazaSinBloquear() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.AnularAutorizacion(
                        ID_TRAMITE, ID_VIGENTE, ID_USUARIO, Constantes.VALOR_SIN_DATO_NO_APLICA));

        assertEquals(Constantes.MENSAJE_MOTIVO_CAMBIO_AUTORIZACION_INVALIDO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalAutorizacionExonet, never()).SIMT_AUTORIZACION_EXONET_BloquearExpediente(any());
        assertEquals(Constantes.ESTADO_AUTORIZACION_VIGENTE, this.vgo_EnVigente.getEstadoAutorizacion());
    }

    @Test
    void AnularAutorizacion_MotivoMayorAlMaximo_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.AnularAutorizacion(ID_TRAMITE, ID_VIGENTE, ID_USUARIO,
                        "m".repeat(Constantes.LONGITUD_MAXIMA_MOTIVO_CAMBIO_AUTORIZACION + 1)));

        assertEquals(Constantes.MENSAJE_MOTIVO_CAMBIO_AUTORIZACION_INVALIDO, vlo_Excepcion.getMessage());
    }


    @Test
    void SustituirAutorizacion_MarcaLaAnteriorSustituidaYCreaLaNuevaEnlazadaEnOrden() {
        EntAutorizacionExonet vlo_EnNueva = this.vgo_BllAutorizacionExonet.SustituirAutorizacion(
                ID_TRAMITE, ID_VIGENTE, ID_USUARIO, "Número de solicitud equivocado",
                "exo-2", HOY.minusDays(3), null, null, null, null, null);

        InOrder vlo_Orden = inOrder(this.vgo_DalAutorizacionExonet);
        vlo_Orden.verify(this.vgo_DalAutorizacionExonet).SIMT_AUTORIZACION_EXONET_BloquearExpediente(ID_TRAMITE);
        vlo_Orden.verify(this.vgo_DalAutorizacionExonet).SIMT_AUTORIZACION_EXONET_ObtenerRegistroParaModificar(ID_VIGENTE);
        vlo_Orden.verify(this.vgo_DalAutorizacionExonet).SIMT_AUTORIZACION_EXONET_ModificarRegistro(this.vgo_EnVigente);
        vlo_Orden.verify(this.vgo_DalAutorizacionExonet).SIMT_AUTORIZACION_EXONET_InsertarRegistro(vlo_EnNueva);

        assertEquals(Constantes.ESTADO_AUTORIZACION_SUSTITUIDA, this.vgo_EnVigente.getEstadoAutorizacion());
        assertEquals("Número de solicitud equivocado", this.vgo_EnVigente.getMotivoCambioEstado());
        assertEquals(ID_USUARIO, this.vgo_EnVigente.getIdUsuarioCambioEstado());

        assertEquals(ID_NUEVA, vlo_EnNueva.getIdAutorizacionExonet());
        assertEquals(ID_VIGENTE, vlo_EnNueva.getIdAutorizacionAnterior());
        assertEquals(Constantes.ESTADO_AUTORIZACION_VIGENTE, vlo_EnNueva.getEstadoAutorizacion());
        assertEquals("EXO-2", vlo_EnNueva.getNumeroSolicitudExonet());
        assertEquals(this.vgo_EnVigente.getFechaCambioEstado(), vlo_EnNueva.getFechaRegistro());

        verify(this.vgo_BllBitacora).RegistrarAccion(
                eq(ID_TRAMITE), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_AUTORIZACION_SUSTITUIDA),
                eq(Constantes.ENTIDAD_BITACORA_AUTORIZACION_EXONET),
                eq(700), contains("EXO-1"), contains(Constantes.PREFIJO_BITACORA_AUTORIZACION + ID_NUEVA));
        verify(this.vgo_BllBitacora).RegistrarAccion(
                eq(ID_TRAMITE), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_AUTORIZACION_REGISTRADA),
                eq(Constantes.ENTIDAD_BITACORA_AUTORIZACION_EXONET),
                eq(701), isNull(), contains(Constantes.PREFIJO_BITACORA_AUTORIZACION + ID_VIGENTE));
    }

    @Test
    void SustituirAutorizacion_DatosNuevosInvalidos_NoTocaLaAnterior() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.SustituirAutorizacion(
                        ID_TRAMITE, ID_VIGENTE, ID_USUARIO, "Motivo", null, null, null, null, null, null, null));

        assertEquals(Constantes.MENSAJE_IDENTIFICADOR_EXONET_REQUERIDO, vlo_Excepcion.getMessage());
        assertEquals(Constantes.ESTADO_AUTORIZACION_VIGENTE, this.vgo_EnVigente.getEstadoAutorizacion());
        verify(this.vgo_DalAutorizacionExonet, never()).SIMT_AUTORIZACION_EXONET_ModificarRegistro(any());
        verify(this.vgo_DalAutorizacionExonet, never()).SIMT_AUTORIZACION_EXONET_InsertarRegistro(any());
    }

    @Test
    void SustituirAutorizacion_SinMotivo_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.SustituirAutorizacion(
                        ID_TRAMITE, ID_VIGENTE, ID_USUARIO, "   ", "EXO-2", null, null, null, null, null, null));

        assertEquals(Constantes.MENSAJE_MOTIVO_CAMBIO_AUTORIZACION_INVALIDO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalAutorizacionExonet, never()).SIMT_AUTORIZACION_EXONET_InsertarRegistro(any());
    }

    @Test
    void SustituirAutorizacion_YaSustituida_Rechaza() {
        this.vgo_EnVigente.setEstadoAutorizacion(Constantes.ESTADO_AUTORIZACION_SUSTITUIDA);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.SustituirAutorizacion(
                        ID_TRAMITE, ID_VIGENTE, ID_USUARIO, "Motivo", "EXO-2", null, null, null, null, null, null));

        assertEquals(Constantes.MENSAJE_AUTORIZACION_NO_VIGENTE, vlo_Excepcion.getMessage());
        verify(this.vgo_DalAutorizacionExonet, never()).SIMT_AUTORIZACION_EXONET_InsertarRegistro(any());
    }


    private void AfirmarRegistroRechazado(
            String pvc_Mensaje,
            String pvc_NumeroSolicitud,
            LocalDate pvd_FechaEnvio,
            String pvc_NumeroAutorizacion,
            LocalDate pvd_FechaAutorizacion,
            LocalDate pvd_FechaComunicacion) {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.RegistrarAutorizacion(
                        ID_TRAMITE, ID_USUARIO, pvc_NumeroSolicitud, pvd_FechaEnvio, pvc_NumeroAutorizacion,
                        pvd_FechaAutorizacion, pvd_FechaComunicacion, null, null));
        assertEquals(pvc_Mensaje, vlo_Excepcion.getMessage());
        verify(this.vgo_DalAutorizacionExonet, never()).SIMT_AUTORIZACION_EXONET_InsertarRegistro(any());
    }

    private void AfirmarCompletarRechazado(Long pvn_IdAutorizacion, String pvc_Mensaje) {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllAutorizacionExonet.CompletarAutorizacion(
                        ID_TRAMITE, pvn_IdAutorizacion, ID_USUARIO, null, null, "AUT-9", HOY, null, null, null));
        assertEquals(pvc_Mensaje, vlo_Excepcion.getMessage());
        verify(this.vgo_DalAutorizacionExonet, never()).SIMT_AUTORIZACION_EXONET_ModificarRegistro(any());
    }

    private MockMultipartFile Archivo() {
        return new MockMultipartFile("archivo", "autorizacion.pdf", "application/pdf", new byte[]{37, 80, 68, 70});
    }

    private EntDocumento Documento(Integer pvn_Id, String pvc_Nombre) {
        EntDocumento vlo_EnDocumento = new EntDocumento();
        vlo_EnDocumento.setIdDocumento(pvn_Id);
        vlo_EnDocumento.setIdTramite(ID_TRAMITE);
        vlo_EnDocumento.setNombreArchivo(pvc_Nombre);
        return vlo_EnDocumento;
    }

    private EntUsuario Usuario(Integer pvn_Id, String pvc_Nombre) {
        EntUsuario vlo_EnUsuario = new EntUsuario();
        vlo_EnUsuario.setIdUsuario(pvn_Id);
        vlo_EnUsuario.setNombreCompleto(pvc_Nombre);
        return vlo_EnUsuario;
    }
}
