package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalMoneda;
import cr.ac.ucr.sim.accesodatos.tramites.DalPais;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntAgenteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCambioEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDatosTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExpedienteTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExtensionPlazoTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntFilaTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialProrrogaTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMoneda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntOpcionesTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntPais;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntProrrogaTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramiteTemporal;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.Enumerados.TipoTramiteTemporal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Autor: Camila Pastor Castro
 * Fecha: 03/10/2026
 * Descripción: Pruebas del registro y de la extensión del plazo de los trámites temporales.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: prueban la importación temporal de la HU-025: registro, vista de trámites
 *     activos, cuenta regresiva, pausa de los pendientes en Retenido en aduana, cambio de estado,
 *     datos de aduana, prórroga y la extensión del plazo de la HU-027. Las pruebas de alertas
 *     pasan a BllAlertaTemporalTest.
 */
@ExtendWith(MockitoExtension.class)
class BllTramiteTemporalTest {

    private static final LocalDate HOY = LocalDate.now(ZoneId.of("America/Costa_Rica"));
    private static final Integer ID_USUARIO = 1;
    private static final Integer ID_TIPO = 7;
    private static final Integer ID_TRAMITE = 31;
    private static final String REFERENCIA = "IT-001-26";
    private static final Integer ID_TIPO_EXPORTACION = 8;
    private static final Integer ID_PAIS = 5;
    private static final String REFERENCIA_EXPORTACION = "EXP-T-012-25";

    @Mock private DalTramite vgo_DalTramite;
    @Mock private DalTramiteTemporal vgo_DalTramiteTemporal;
    @Mock private DalTipoTramite vgo_DalTipoTramite;
    @Mock private DalEstadoTramite vgo_DalEstadoTramite;
    @Mock private DalHistorialEstado vgo_DalHistorialEstado;
    @Mock private DalMoneda vgo_DalMoneda;
    @Mock private DalPais vgo_DalPais;
    @Mock private DalDocumento vgo_DalDocumento;
    @Mock private DalUsuario vgo_DalUsuario;
    @Mock private AlmacenamientoDocumentos vgo_AlmacenamientoDocumentos;
    @Mock private BllDocumento vgo_BllDocumento;
    @Mock private BllBitacora vgo_BllBitacora;
    @Mock private BllAlertaTemporal vgo_BllAlertaTemporal;

    private BllTramiteTemporal vgo_BllTramiteTemporal;
    private final Map<String, EntEstadoTramite> vgo_MpEstados = new HashMap<>();
    private final Map<String, EntEstadoTramite> vgo_MpEstadosExportacion = new HashMap<>();

    @BeforeEach
    void PrepararPrueba() {
        this.vgo_BllTramiteTemporal = new BllTramiteTemporal(
                vgo_DalTramite, vgo_DalTramiteTemporal, vgo_DalTipoTramite, vgo_DalEstadoTramite,
                vgo_DalHistorialEstado, vgo_DalMoneda, vgo_DalPais, vgo_DalDocumento, vgo_DalUsuario,
                vgo_AlmacenamientoDocumentos, vgo_BllDocumento, vgo_BllBitacora, vgo_BllAlertaTemporal);

        EntTipoTramite vlo_Tipo = new EntTipoTramite();
        vlo_Tipo.setIdTipoTramite(ID_TIPO);
        vlo_Tipo.setCodigo(Constantes.CODIGO_TIPO_TRAMITE_IMPORTACION_TEMPORAL);
        vlo_Tipo.setNombre("Importación temporal");
        vlo_Tipo.setPrefijoReferencia("IT-");
        lenient().when(vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerPorCodigo(
                Constantes.CODIGO_TIPO_TRAMITE_IMPORTACION_TEMPORAL)).thenReturn(vlo_Tipo);

        String[] vlo_Nombres = {"Por llegar", "En gestión aduanera", "Retenido en aduana", "Activo en el país",
                "Vencido", "En reexportación", "Finalizado", "Cancelado"};
        for (int vln_Indice = 0; vln_Indice < vlo_Nombres.length; vln_Indice++) {
            EntEstadoTramite vlo_Estado = new EntEstadoTramite();
            vlo_Estado.setIdEstadoTramite(100 + vln_Indice);
            vlo_Estado.setIdTipoTramite(ID_TIPO);
            vlo_Estado.setNombre(vlo_Nombres[vln_Indice]);
            vlo_Estado.setEsInicial(vln_Indice == 0);
            vlo_Estado.setEsFinal(vln_Indice >= 6);
            this.vgo_MpEstados.put(vlo_Nombres[vln_Indice], vlo_Estado);
            lenient().when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(100 + vln_Indice))
                    .thenReturn(vlo_Estado);
            lenient().when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                    ID_TIPO, vlo_Nombres[vln_Indice], Constantes.ESTADO_CATALOGO_ACTIVO)).thenReturn(vlo_Estado);
        }
        lenient().when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerEstadoInicial(ID_TIPO))
                .thenReturn(this.vgo_MpEstados.get("Por llegar"));

        EntTipoTramite vlo_TipoExportacion = new EntTipoTramite();
        vlo_TipoExportacion.setIdTipoTramite(ID_TIPO_EXPORTACION);
        vlo_TipoExportacion.setCodigo(Constantes.CODIGO_TIPO_TRAMITE_EXPORTACION_TEMPORAL);
        vlo_TipoExportacion.setNombre("Exportación temporal");
        vlo_TipoExportacion.setPrefijoReferencia("EXP-T-");
        lenient().when(vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerPorCodigo(
                Constantes.CODIGO_TIPO_TRAMITE_EXPORTACION_TEMPORAL)).thenReturn(vlo_TipoExportacion);

        String[] vlo_NombresExportacion = {"Por salir", "En gestión aduanera", "Retenido en aduana", "Fuera del país",
                "Vencido", "En reimportación", "Finalizado", "Salida definitiva", "Anulado"};
        for (int vln_Indice = 0; vln_Indice < vlo_NombresExportacion.length; vln_Indice++) {
            EntEstadoTramite vlo_Estado = new EntEstadoTramite();
            vlo_Estado.setIdEstadoTramite(200 + vln_Indice);
            vlo_Estado.setIdTipoTramite(ID_TIPO_EXPORTACION);
            vlo_Estado.setNombre(vlo_NombresExportacion[vln_Indice]);
            vlo_Estado.setEsInicial(vln_Indice == 0);
            vlo_Estado.setEsFinal(vln_Indice >= 6);
            this.vgo_MpEstadosExportacion.put(vlo_NombresExportacion[vln_Indice], vlo_Estado);
            lenient().when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(200 + vln_Indice))
                    .thenReturn(vlo_Estado);
            lenient().when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                    ID_TIPO_EXPORTACION, vlo_NombresExportacion[vln_Indice], Constantes.ESTADO_CATALOGO_ACTIVO))
                    .thenReturn(vlo_Estado);
        }
        lenient().when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerEstadoInicial(ID_TIPO_EXPORTACION))
                .thenReturn(this.vgo_MpEstadosExportacion.get("Por salir"));

        EntPais vlo_Pais = new EntPais();
        vlo_Pais.setIdPais(ID_PAIS);
        vlo_Pais.setNombre("Dinamarca");
        vlo_Pais.setEstado(Constantes.ESTADO_CATALOGO_ACTIVO);
        lenient().when(vgo_DalPais.SIMM_PAIS_ObtenerRegistro(ID_PAIS)).thenReturn(vlo_Pais);

        EntMoneda vlo_Colon = new EntMoneda();
        vlo_Colon.setIdMoneda(1);
        vlo_Colon.setCodigoIso("CRC");
        lenient().when(vgo_DalMoneda.SIMM_MONEDA_ObtenerPorCodigoIso("CRC")).thenReturn(vlo_Colon);

        EntUsuario vlo_Usuario = new EntUsuario();
        vlo_Usuario.setIdUsuario(ID_USUARIO);
        vlo_Usuario.setNombreCompleto("Técnica de Exoneraciones");
        lenient().when(vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_USUARIO)).thenReturn(vlo_Usuario);

        EntMoneda vlo_Moneda = new EntMoneda();
        vlo_Moneda.setIdMoneda(4);
        vlo_Moneda.setCodigoIso("GBP");
        lenient().when(vgo_DalMoneda.SIMM_MONEDA_ObtenerPorCodigoIso("GBP")).thenReturn(vlo_Moneda);

        lenient().when(vgo_BllAlertaTemporal.Hoy()).thenReturn(HOY);
        lenient().when(vgo_BllAlertaTemporal.ObtenerIndicacion(any(), any(), any(), any(), any())).thenReturn("");
        lenient().doAnswer(pvo_Invocacion -> {
            ((EntTramite) pvo_Invocacion.getArgument(0)).setIdTramite(ID_TRAMITE);
            return null;
        }).when(vgo_DalTramite).SIMT_TRAMITE_InsertarRegistro(any());
    }

    // -----------------------------------------------------------------------
    // Registro
    // -----------------------------------------------------------------------

    @Test
    void InsertarRegistro_DatosMinimos_CreaElExpedientePorLlegarConElUsuarioComoResponsable() {
        EntRespuestaTramiteTemporal vlo_Respuesta = this.vgo_BllTramiteTemporal.InsertarRegistro(
                TipoTramiteTemporal.IMPORTACION, CrearRegistro("it-001-26", BigDecimal.valueOf(99145), null, null), Map.of(), ID_USUARIO);

        ArgumentCaptor<EntTramite> vlo_CaptorTramite = ArgumentCaptor.forClass(EntTramite.class);
        verify(vgo_DalTramite).SIMT_TRAMITE_InsertarRegistro(vlo_CaptorTramite.capture());
        EntTramite vlo_Tramite = vlo_CaptorTramite.getValue();
        assertEquals(REFERENCIA, vlo_Tramite.getReferenciaInterna());
        assertEquals(ID_TIPO, vlo_Tramite.getIdTipoTramite());
        assertEquals(ID_USUARIO, vlo_Tramite.getIdUsuarioResponsable());
        assertEquals(Constantes.REGIMEN_IMPORTACION_TEMPORAL, vlo_Tramite.getRegimenAduanero());
        assertEquals(4, vlo_Tramite.getIdMoneda());

        ArgumentCaptor<EntTramiteTemporal> vlo_CaptorTemporal = ArgumentCaptor.forClass(EntTramiteTemporal.class);
        verify(vgo_DalTramiteTemporal).SIMT_TRAMITE_TEMPORAL_InsertarRegistro(vlo_CaptorTemporal.capture());
        assertEquals(ID_TRAMITE, vlo_CaptorTemporal.getValue().getIdTramite());
        assertEquals("USADA", vlo_CaptorTemporal.getValue().getEstadoMercancia());
        assertEquals("SR", vlo_CaptorTemporal.getValue().getNumeroFactura());

        verify(vgo_DalTramiteTemporal, never()).SIMT_TEMPORAL_AGENTE_Insertar(anyInt(), anyInt());
        verify(vgo_DalHistorialEstado).SIMH_ESTADO_TRAMITE_InsertarRegistro(any(EntHistorialEstado.class));
        verify(vgo_BllBitacora).RegistrarAccion(eq(ID_TRAMITE), eq(ID_USUARIO), eq(Constantes.ACCION_BITACORA_REGISTRO),
                eq(Constantes.ENTIDAD_BITACORA_TRAMITE_TEMPORAL), eq(ID_TRAMITE), any(), anyString());
        assertEquals("Por llegar", vlo_Respuesta.estado());
        assertEquals(Constantes.MENSAJE_REGISTRO_TEMPORAL_SIN_VENCIMIENTO, vlo_Respuesta.mensaje());
    }

    @Test
    void InsertarRegistro_ConVencimientoYAgentes_AsignaLosAgentesEIniciaLaCuentaRegresiva() {
        when(vgo_DalTramiteTemporal.SIMF_USUARIO_ObtenerAgentesResponsables())
                .thenReturn(List.of(new EntAgenteTemporal(10, "Agente de aduanas")));

        EntRespuestaTramiteTemporal vlo_Respuesta = this.vgo_BllTramiteTemporal.InsertarRegistro(
                TipoTramiteTemporal.IMPORTACION, CrearRegistro(REFERENCIA, BigDecimal.TEN, HOY.plusMonths(6), List.of(10)), Map.of(), ID_USUARIO);

        verify(vgo_DalTramiteTemporal).SIMT_TEMPORAL_AGENTE_Insertar(ID_TRAMITE, 10);
        assertEquals(Constantes.MENSAJE_REGISTRO_TEMPORAL_EXITOSO, vlo_Respuesta.mensaje());
    }

    @Test
    void InsertarRegistro_DocumentoDelExpediente_SeGuardaConSuTipo() {
        MultipartFile vlo_Archivo = new MockMultipartFile("OFICIO_ADUANA", "oficio.pdf", "application/pdf", new byte[] {1});

        this.vgo_BllTramiteTemporal.InsertarRegistro(TipoTramiteTemporal.IMPORTACION, CrearRegistro(REFERENCIA, BigDecimal.TEN, null, null),
                Map.of("OFICIO_ADUANA", List.of(vlo_Archivo)), ID_USUARIO);

        verify(vgo_BllDocumento).GuardarDocumento(any(EntTramite.class), eq(ID_USUARIO), eq(vlo_Archivo),
                eq(Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_ADUANA), any());
    }

    @Test
    void InsertarRegistro_ReferenciaSinSiglaIT_RechazaElRegistro() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarRegistro(
                        TipoTramiteTemporal.IMPORTACION, CrearRegistro("D-001-26", BigDecimal.TEN, null, null), Map.of(), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_REFERENCIA_TEMPORAL_INVALIDA, vlo_Excepcion.getMessage());
        verify(vgo_DalTramite, never()).SIMT_TRAMITE_InsertarRegistro(any());
    }

    @Test
    void InsertarRegistro_ReferenciaDuplicada_RechazaElRegistro() {
        when(vgo_DalTramite.SIMT_TRAMITE_ExistePorReferenciaInterna(REFERENCIA)).thenReturn(true);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarRegistro(
                        TipoTramiteTemporal.IMPORTACION, CrearRegistro(REFERENCIA, BigDecimal.TEN, null, null), Map.of(), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_REFERENCIA_DUPLICADA, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_SinValorDelEquipo_RechazaElRegistro() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarRegistro(
                        TipoTramiteTemporal.IMPORTACION, CrearRegistro(REFERENCIA, null, null, null), Map.of(), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_VALOR_EQUIPO_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_FechaDeRecepcionFutura_RechazaElRegistro() {
        EntRegistroTramiteTemporal vlo_Registro = new EntRegistroTramiteTemporal(
                REFERENCIA, "ECG", "ECG-82-2025", HOY.plusDays(1), "Equipo sísmico", "usada",
                BigDecimal.TEN, "GBP", null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarRegistro(TipoTramiteTemporal.IMPORTACION, vlo_Registro, Map.of(), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_FECHA_RECEPCION_INVALIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_EstadoDeMercanciaNoAdmitido_RechazaElRegistro() {
        EntRegistroTramiteTemporal vlo_Registro = new EntRegistroTramiteTemporal(
                REFERENCIA, "ECG", "ECG-82-2025", HOY, "Equipo sísmico", "Dañada",
                BigDecimal.TEN, "GBP", null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarRegistro(TipoTramiteTemporal.IMPORTACION, vlo_Registro, Map.of(), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_ESTADO_MERCANCIA_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_AgenteSinRolDeAgente_RechazaElRegistro() {
        when(vgo_DalTramiteTemporal.SIMF_USUARIO_ObtenerAgentesResponsables()).thenReturn(List.of());

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarRegistro(
                        TipoTramiteTemporal.IMPORTACION, CrearRegistro(REFERENCIA, BigDecimal.TEN, null, List.of(99)), Map.of(), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_AGENTES_TEMPORAL_INVALIDOS, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_DocumentoDeOtroTramite_RechazaElRegistro() {
        MultipartFile vlo_Archivo = new MockMultipartFile("RESOLUCION_ACTA", "acta.pdf", "application/pdf", new byte[] {1});

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarRegistro(
                        TipoTramiteTemporal.IMPORTACION, CrearRegistro(REFERENCIA, BigDecimal.TEN, null, null),
                        Map.of("RESOLUCION_ACTA", List.of(vlo_Archivo)), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_TIPO_DOCUMENTO_TEMPORAL_INVALIDO, vlo_Excepcion.getMessage());
    }

    // -----------------------------------------------------------------------
    // Vista de trámites activos y expediente
    // -----------------------------------------------------------------------

    @Test
    void ObtenerLista_Activos_ExcluyeLosCerradosYOrdenaPorCercaniaDelVencimiento() {
        when(vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerLista(ID_TIPO)).thenReturn(List.of(
                CrearFila("IT-001-26", "Activo en el país", HOY.plusDays(90)),
                CrearFila("IT-002-26", "Por llegar", null),
                CrearFila("IT-003-26", "Finalizado", HOY.minusDays(10)),
                CrearFila("IT-004-26", "Retenido en aduana", HOY.plusDays(20)),
                CrearFila("IT-005-26", "Vencido", HOY.minusDays(2))));

        List<EntResumenTramiteTemporal> vlo_Lista = this.vgo_BllTramiteTemporal.ObtenerLista(TipoTramiteTemporal.IMPORTACION, null);

        assertEquals(List.of("IT-005-26", "IT-004-26", "IT-001-26", "IT-002-26"),
                vlo_Lista.stream().map(EntResumenTramiteTemporal::referenciaInterna).toList());
        assertEquals(Constantes.VENCIMIENTO_VENCIDO, vlo_Lista.get(0).vencimiento().nivel());
        assertEquals(Constantes.VENCIMIENTO_PROXIMO, vlo_Lista.get(1).vencimiento().nivel());
        assertEquals(20L, vlo_Lista.get(1).vencimiento().diasRestantes());
        assertEquals(Constantes.VENCIMIENTO_EN_PLAZO, vlo_Lista.get(2).vencimiento().nivel());
        assertEquals(Constantes.VENCIMIENTO_SIN_FECHA, vlo_Lista.get(3).vencimiento().nivel());
    }

    @Test
    void ObtenerLista_AlcanceNoAdmitido_RechazaLaConsulta() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ObtenerLista(TipoTramiteTemporal.IMPORTACION, "pendientes"));

        assertEquals(Constantes.MENSAJE_ALCANCE_TEMPORAL_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void ObtenerExpediente_ActivoSinDatosDeAduana_MuestraLosPendientesDeSuEtapa() {
        PrepararExpediente("Activo en el país", HOY.plusDays(100));

        EntExpedienteTramiteTemporal vlo_Expediente = this.vgo_BllTramiteTemporal.ObtenerExpediente(TipoTramiteTemporal.IMPORTACION, REFERENCIA);

        assertFalse(vlo_Expediente.validacionesPausadas());
        assertEquals(List.of("Oficio enviado a la aduana", "Número de gestión asignado por la aduana",
                "Resolución de autorización", "DUA de importación temporal"), vlo_Expediente.pendientes());
    }

    @Test
    void ObtenerExpediente_DocumentoRegistrado_NoLoMuestraComoPendiente() {
        PrepararExpediente("En gestión aduanera", null);
        when(vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerCodigosTipoVigentes(ID_TRAMITE, Constantes.ESTADO_DOCUMENTO_ACTIVO))
                .thenReturn(List.of(Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_ADUANA));

        EntExpedienteTramiteTemporal vlo_Expediente = this.vgo_BllTramiteTemporal.ObtenerExpediente(TipoTramiteTemporal.IMPORTACION, REFERENCIA);

        assertTrue(vlo_Expediente.pendientes().isEmpty());
    }

    @Test
    void ObtenerExpediente_RetenidoEnAduana_PausaLosPendientesPeroMantieneLaCuentaRegresiva() {
        PrepararExpediente("Retenido en aduana", HOY.plusDays(10));

        EntExpedienteTramiteTemporal vlo_Expediente = this.vgo_BllTramiteTemporal.ObtenerExpediente(TipoTramiteTemporal.IMPORTACION, REFERENCIA);

        assertTrue(vlo_Expediente.validacionesPausadas());
        assertTrue(vlo_Expediente.pendientes().isEmpty());
        assertEquals(Constantes.VENCIMIENTO_PROXIMO, vlo_Expediente.vencimiento().nivel());
        assertEquals(10L, vlo_Expediente.vencimiento().diasRestantes());
    }

    @Test
    void ObtenerExpediente_Cerrado_NoMuestraNivelDeVencimiento() {
        PrepararExpediente("Finalizado", HOY.minusDays(5));

        EntExpedienteTramiteTemporal vlo_Expediente = this.vgo_BllTramiteTemporal.ObtenerExpediente(TipoTramiteTemporal.IMPORTACION, REFERENCIA);

        assertNull(vlo_Expediente.vencimiento().nivel());
        assertTrue(vlo_Expediente.estadoFinal());
    }

    // -----------------------------------------------------------------------
    // Cambio de estado
    // -----------------------------------------------------------------------

    @Test
    void ActualizarEstado_ActivoSinVencimiento_RechazaElCambio() {
        PrepararExpediente("En gestión aduanera", null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ActualizarEstado(
                        TipoTramiteTemporal.IMPORTACION, REFERENCIA, CrearCambio("Activo en el país", "Ingresó el equipo"), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_VENCIMIENTO_REQUERIDO_ACTIVO, vlo_Excepcion.getMessage());
    }

    @Test
    void ActualizarEstado_RetenidoEnAduana_CambiaElEstadoSinPedirElRestoDelFormulario() {
        EntTramite vlo_Tramite = PrepararExpediente("Activo en el país", HOY.plusDays(60));

        EntExpedienteTramiteTemporal vlo_Expediente = this.vgo_BllTramiteTemporal.ActualizarEstado(
                TipoTramiteTemporal.IMPORTACION, REFERENCIA, CrearCambio("Retenido en aduana", "La aduana retuvo el equipo"), ID_USUARIO);

        assertEquals(102, vlo_Tramite.getIdEstadoTramite());
        assertEquals("Retenido en aduana", vlo_Expediente.estado());
        verify(vgo_DalTramite).SIMT_TRAMITE_ModificarRegistro(vlo_Tramite);
        verify(vgo_DalHistorialEstado).SIMH_ESTADO_TRAMITE_InsertarRegistro(any(EntHistorialEstado.class));
        verify(vgo_BllAlertaTemporal, never()).AtenderPendientes(any());
    }

    @Test
    void ActualizarEstado_FinalizarSinDuaDeCierreNiCancelacion_RechazaElCambio() {
        PrepararExpediente("En reexportación", HOY.plusDays(5));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ActualizarEstado(
                        TipoTramiteTemporal.IMPORTACION, REFERENCIA, CrearCambio("Finalizado", "Se reexportó el equipo"), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_REQUISITOS_FINALIZACION, vlo_Excepcion.getMessage());
    }

    @Test
    void ActualizarEstado_FinalizarConDuaYOficioDeCancelacion_CierraElExpedienteYAtiendeLasAlertas() {
        EntTramite vlo_Tramite = PrepararExpediente("En reexportación", HOY.plusDays(5));
        EntTramiteTemporal vlo_Temporal = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);
        vlo_Temporal.setNumeroDuaCierre("005-2026-349066");
        when(vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerCodigosTipoVigentes(ID_TRAMITE, Constantes.ESTADO_DOCUMENTO_ACTIVO))
                .thenReturn(List.of(Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_CANCELACION));

        this.vgo_BllTramiteTemporal.ActualizarEstado(
                TipoTramiteTemporal.IMPORTACION, REFERENCIA, CrearCambio("Finalizado", "Se reexportó el equipo"), ID_USUARIO);

        assertEquals(106, vlo_Tramite.getIdEstadoTramite());
        assertEquals(HOY, vlo_Tramite.getFechaCierre());
        verify(vgo_BllAlertaTemporal).AtenderPendientes(ID_TRAMITE);
    }

    @Test
    void ActualizarEstado_ExpedienteCerrado_RechazaElCambio() {
        PrepararExpediente("Cancelado", null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ActualizarEstado(
                        TipoTramiteTemporal.IMPORTACION, REFERENCIA, CrearCambio("Por llegar", "Reapertura"), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_EXPEDIENTE_TEMPORAL_CERRADO, vlo_Excepcion.getMessage());
    }

    @Test
    void ActualizarEstado_SinJustificacion_RechazaElCambio() {
        PrepararExpediente("Por llegar", null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ActualizarEstado(
                        TipoTramiteTemporal.IMPORTACION, REFERENCIA, CrearCambio("En gestión aduanera", "  "), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_JUSTIFICACION_ESTADO_INVALIDA, vlo_Excepcion.getMessage());
    }

    // -----------------------------------------------------------------------
    // Datos de aduana y prórroga
    // -----------------------------------------------------------------------

    @Test
    void ModificarDatos_ExpedienteCerrado_PermiteCorregirSoloElDuaDeCierre() {
        PrepararExpediente("Finalizado", HOY.minusDays(5));

        this.vgo_BllTramiteTemporal.ModificarDatos(TipoTramiteTemporal.IMPORTACION, REFERENCIA,
                CrearDatos(null, "005-2026-102473", HOY.minusDays(1), HOY.minusDays(5)), ID_USUARIO);
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ModificarDatos(TipoTramiteTemporal.IMPORTACION, REFERENCIA,
                        CrearDatos("1236-2025", "005-2026-102473", HOY.minusDays(1), HOY.minusDays(5)), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_SOLO_DUA_CIERRE_EDITABLE, vlo_Excepcion.getMessage());
        verify(vgo_DalTramiteTemporal).SIMT_TRAMITE_TEMPORAL_ModificarRegistro(any());
    }

    @Test
    void ModificarDatos_CambiaElVencimiento_AtiendeLasAlertasDelVencimientoAnterior() {
        PrepararExpediente("Activo en el país", HOY.plusDays(30));

        this.vgo_BllTramiteTemporal.ModificarDatos(TipoTramiteTemporal.IMPORTACION, REFERENCIA,
                CrearDatos(null, null, null, HOY.plusDays(40)), ID_USUARIO);

        verify(vgo_BllAlertaTemporal).AtenderPendientes(ID_TRAMITE);
        verify(vgo_BllBitacora).RegistrarAccion(eq(ID_TRAMITE), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_DATOS_MODIFICADOS), any(), any(), anyString(), anyString());
    }

    @Test
    void ModificarDatos_BorraElVencimientoConElEquipoEnElPais_RechazaElCambio() {
        PrepararExpediente("Activo en el país", HOY.plusDays(30));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ModificarDatos(TipoTramiteTemporal.IMPORTACION, REFERENCIA,
                        CrearDatos(null, null, null, null), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_VENCIMIENTO_NO_SE_PUEDE_BORRAR, vlo_Excepcion.getMessage());
    }

    @Test
    void RegistrarProrroga_SinDocumentosRequeridos_NoActualizaElVencimiento() {
        PrepararExpediente("Activo en el país", HOY.plusDays(20));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.RegistrarProrroga(TipoTramiteTemporal.IMPORTACION, REFERENCIA,
                        CrearProrroga(HOY.plusDays(20)), null, null, ID_USUARIO));

        assertEquals(Constantes.MENSAJE_PRORROGA_DOCUMENTOS_REQUERIDOS, vlo_Excepcion.getMessage());
        verify(vgo_DalTramiteTemporal, never()).SIMT_TRAMITE_TEMPORAL_ModificarRegistro(any());
    }

    @Test
    void RegistrarProrroga_SinAutorizacion_NoActualizaElVencimiento() {
        PrepararExpediente("Activo en el país", HOY.plusDays(20));
        EntTramiteTemporal vlo_Temporal = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.RegistrarProrroga(TipoTramiteTemporal.IMPORTACION, REFERENCIA,
                        new EntProrrogaTemporal("Motivo", "OF-123", "GEST-123", false, null,
                                HOY.plusDays(20), null, false),
                        ArchivoProrroga("solicitud.pdf"), ArchivoProrroga("resolucion.pdf"), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_PRORROGA_REQUISITOS_INCOMPLETOS, vlo_Excepcion.getMessage());
        assertEquals(HOY.plusDays(20), vlo_Temporal.getFechaVencimiento());
        verify(vgo_BllDocumento, never()).GuardarDocumento(any(), anyInt(), any(), anyString(), anyList());
    }

    @Test
    void RegistrarProrroga_PermanecePorAnosSinContrato_RechazaElRegistro() {
        PrepararExpediente("Activo en el país", HOY.plusDays(20));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.RegistrarProrroga(TipoTramiteTemporal.IMPORTACION, REFERENCIA,
                        new EntProrrogaTemporal("Motivo", "OF-123", "GEST-123", false, "AUT-123",
                                HOY.plusDays(20), null, true),
                        ArchivoProrroga("solicitud.pdf"), ArchivoProrroga("resolucion.pdf"), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_PRORROGA_CONTRATO_REQUERIDO, vlo_Excepcion.getMessage());
        verify(vgo_DalTramiteTemporal, never()).SIMT_TRAMITE_TEMPORAL_ModificarRegistro(any());
    }

    @Test
    void RegistrarProrroga_Importacion_ActualizaSeisMesesYConservaHistorial() {
        PrepararExpediente("Activo en el país", HOY.plusDays(20));
        EntTramiteTemporal vlo_Temporal = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);
        EntDocumento vlo_Oficio = new EntDocumento();
        vlo_Oficio.setIdDocumento(501);
        EntDocumento vlo_Resolucion = new EntDocumento();
        vlo_Resolucion.setIdDocumento(502);
        when(vgo_BllDocumento.GuardarDocumento(any(), eq(ID_USUARIO), any(MultipartFile.class),
                eq(Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_PRORROGA), anyList())).thenReturn(vlo_Oficio);
        when(vgo_BllDocumento.GuardarDocumento(any(), eq(ID_USUARIO), any(MultipartFile.class),
                eq(Constantes.CODIGO_TIPO_DOCUMENTO_AUTORIZACION_PRORROGA), anyList())).thenReturn(vlo_Resolucion);

        this.vgo_BllTramiteTemporal.RegistrarProrroga(TipoTramiteTemporal.IMPORTACION, REFERENCIA,
                CrearProrroga(HOY.plusDays(20)), ArchivoProrroga("solicitud.pdf"),
                ArchivoProrroga("resolucion.pdf"), ID_USUARIO);

        assertEquals(HOY.plusDays(20).plusMonths(6), vlo_Temporal.getFechaVencimiento());
        ArgumentCaptor<EntHistorialProrrogaTemporal> vlo_Captor =
                ArgumentCaptor.forClass(EntHistorialProrrogaTemporal.class);
        verify(vgo_DalTramiteTemporal).SIMT_HISTORIAL_PRORROGA_Insertar(vlo_Captor.capture());
        assertEquals(501, vlo_Captor.getValue().getIdDocumentoOficio());
        assertEquals(502, vlo_Captor.getValue().getIdDocumentoResolucion());
        verify(vgo_BllAlertaTemporal).AtenderPendientes(ID_TRAMITE);
        verify(vgo_BllBitacora).RegistrarAccion(eq(ID_TRAMITE), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_PRORROGA), any(), any(), anyString(), anyString());
    }

    @Test
    void RegistrarProrroga_ImportacionPermiteVariasProrrogasConsecutivas() {
        LocalDate vld_VencimientoInicial = HOY.plusDays(20);
        PrepararExpediente("Activo en el país", vld_VencimientoInicial);
        EntDocumento[] vlo_Oficios = {new EntDocumento(), new EntDocumento()};
        EntDocumento[] vlo_Resoluciones = {new EntDocumento(), new EntDocumento()};
        for (int vln_Indice = 0; vln_Indice < 2; vln_Indice++) {
            vlo_Oficios[vln_Indice].setIdDocumento(501 + (vln_Indice * 2));
            vlo_Resoluciones[vln_Indice].setIdDocumento(502 + (vln_Indice * 2));
        }
        when(vgo_BllDocumento.GuardarDocumento(any(), eq(ID_USUARIO), any(MultipartFile.class),
                eq(Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_PRORROGA), anyList()))
                .thenReturn(vlo_Oficios[0], vlo_Oficios[1]);
        when(vgo_BllDocumento.GuardarDocumento(any(), eq(ID_USUARIO), any(MultipartFile.class),
                eq(Constantes.CODIGO_TIPO_DOCUMENTO_AUTORIZACION_PRORROGA), anyList()))
                .thenReturn(vlo_Resoluciones[0], vlo_Resoluciones[1]);

        this.vgo_BllTramiteTemporal.RegistrarProrroga(TipoTramiteTemporal.IMPORTACION, REFERENCIA,
                CrearProrroga(vld_VencimientoInicial), ArchivoProrroga("solicitud-1.pdf"),
                ArchivoProrroga("resolucion-1.pdf"), ID_USUARIO);
        this.vgo_BllTramiteTemporal.RegistrarProrroga(TipoTramiteTemporal.IMPORTACION, REFERENCIA,
                CrearProrroga(vld_VencimientoInicial.plusMonths(6)), ArchivoProrroga("solicitud-2.pdf"),
                ArchivoProrroga("resolucion-2.pdf"), ID_USUARIO);

        ArgumentCaptor<EntHistorialProrrogaTemporal> vlo_Captor =
                ArgumentCaptor.forClass(EntHistorialProrrogaTemporal.class);
        verify(vgo_DalTramiteTemporal, org.mockito.Mockito.times(2))
                .SIMT_HISTORIAL_PRORROGA_Insertar(vlo_Captor.capture());
        assertEquals(vld_VencimientoInicial.plusMonths(12),
                vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE).getFechaVencimiento());
        assertEquals(vld_VencimientoInicial, vlo_Captor.getAllValues().get(0).getFechaVencimientoAnterior());
        assertEquals(vld_VencimientoInicial.plusMonths(6),
                vlo_Captor.getAllValues().get(1).getFechaVencimientoAnterior());
    }

    @Test
    void RegistrarProrroga_ExportacionConProrrogaPrevia_RechazaOtra() {
        PrepararExpedienteExportacion("Fuera del país", HOY.plusDays(20));
        EntTramiteTemporal vlo_Temporal = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);
        vlo_Temporal.setOficioProrroga("OF-ANTERIOR");

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.RegistrarProrroga(TipoTramiteTemporal.EXPORTACION,
                        REFERENCIA_EXPORTACION, CrearProrroga(HOY.plusDays(20)),
                        ArchivoProrroga("solicitud.pdf"), ArchivoProrroga("resolucion.pdf"), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_PRORROGA_MAXIMA_EXPORTACION, vlo_Excepcion.getMessage());
        assertEquals(HOY.plusDays(20), vlo_Temporal.getFechaVencimiento());
        verify(vgo_BllDocumento, never()).GuardarDocumento(any(), anyInt(), any(), anyString(), anyList());
    }

    // -----------------------------------------------------------------------
    // Extensión del plazo (HU-027)
    // -----------------------------------------------------------------------

    @Test
    void ExtenderPlazo_PlazoEnAniosSinContrato_RechazaLaExtension() {
        PrepararExpediente("Activo en el país", HOY.plusDays(7));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ExtenderPlazo(
                        REFERENCIA, new EntExtensionPlazoTemporal(2, "ANOS", null), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_CONTRATO_TEMPORAL_REQUERIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void ExtenderPlazo_ActualizaElVencimientoYAtiendeLasAlertas() {
        PrepararExpediente("Activo en el país", HOY.plusDays(7));
        EntTramiteTemporal vlo_Temporal = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);

        this.vgo_BllTramiteTemporal.ExtenderPlazo(REFERENCIA, new EntExtensionPlazoTemporal(1, "meses", null), ID_USUARIO);

        assertEquals(HOY.plusDays(7).plusMonths(1), vlo_Temporal.getFechaVencimiento());
        assertEquals(1, vlo_Temporal.getCantidadExtension());
        assertEquals("MESES", vlo_Temporal.getUnidadExtension());
        verify(vgo_BllAlertaTemporal).AtenderPendientes(ID_TRAMITE);
    }

    // -----------------------------------------------------------------------
    // Exportación temporal (HU-026)
    // -----------------------------------------------------------------------

    @Test
    void InsertarRegistro_Exportacion_CreaElExpedientePorSalirConSusDatosDeSalida() {
        EntRespuestaTramiteTemporal vlo_Respuesta = this.vgo_BllTramiteTemporal.InsertarRegistro(
                TipoTramiteTemporal.EXPORTACION,
                CrearRegistroExportacion("exp-t-012-25", true, new BigDecimal("1.50"), ID_PAIS, null, null),
                Map.of(), ID_USUARIO);

        ArgumentCaptor<EntTramite> vlo_CaptorTramite = ArgumentCaptor.forClass(EntTramite.class);
        verify(vgo_DalTramite).SIMT_TRAMITE_InsertarRegistro(vlo_CaptorTramite.capture());
        assertEquals(REFERENCIA_EXPORTACION, vlo_CaptorTramite.getValue().getReferenciaInterna());
        assertEquals(ID_TIPO_EXPORTACION, vlo_CaptorTramite.getValue().getIdTipoTramite());
        assertEquals(Constantes.REGIMEN_EXPORTACION_TEMPORAL, vlo_CaptorTramite.getValue().getRegimenAduanero());
        assertEquals(1, vlo_CaptorTramite.getValue().getIdMoneda());

        ArgumentCaptor<EntTramiteTemporal> vlo_CaptorTemporal = ArgumentCaptor.forClass(EntTramiteTemporal.class);
        verify(vgo_DalTramiteTemporal).SIMT_TRAMITE_TEMPORAL_InsertarRegistro(vlo_CaptorTemporal.capture());
        EntTramiteTemporal vlo_Temporal = vlo_CaptorTemporal.getValue();
        assertEquals("P-011-2024", vlo_Temporal.getAntecedente());
        assertTrue(vlo_Temporal.getEnGarantia());
        assertEquals(new BigDecimal("1.50"), vlo_Temporal.getPesoKg());
        assertEquals(ID_PAIS, vlo_Temporal.getIdPaisDestino());
        assertEquals("OS-1570-2025", vlo_Temporal.getOficioNotificacionUsuario());
        assertNull(vlo_Temporal.getUnidadPlazo());
        assertEquals("Por salir", vlo_Respuesta.estado());
        assertEquals(Constantes.MENSAJE_REGISTRO_EXPORTACION_TEMPORAL_SIN_VENCIMIENTO, vlo_Respuesta.mensaje());
    }

    @Test
    void InsertarRegistro_ExportacionConVencimientoEstandar_RegistraElPlazoDeSeisMeses() {
        LocalDate vld_Dua = HOY.minusDays(10);

        EntRespuestaTramiteTemporal vlo_Respuesta = this.vgo_BllTramiteTemporal.InsertarRegistro(
                TipoTramiteTemporal.EXPORTACION,
                CrearRegistroExportacion(REFERENCIA_EXPORTACION, false, BigDecimal.ONE, ID_PAIS,
                        vld_Dua, vld_Dua.plusMonths(6)),
                Map.of(), ID_USUARIO);

        ArgumentCaptor<EntTramiteTemporal> vlo_Captor = ArgumentCaptor.forClass(EntTramiteTemporal.class);
        verify(vgo_DalTramiteTemporal).SIMT_TRAMITE_TEMPORAL_InsertarRegistro(vlo_Captor.capture());
        assertEquals("MESES", vlo_Captor.getValue().getUnidadPlazo());
        assertEquals(6, vlo_Captor.getValue().getCantidadPlazo());
        assertEquals(Constantes.MENSAJE_REGISTRO_EXPORTACION_TEMPORAL_EXITOSO, vlo_Respuesta.mensaje());
    }

    @Test
    void InsertarRegistro_ExportacionConOtroVencimiento_NoRegistraElPlazoEstandar() {
        LocalDate vld_Dua = HOY.minusDays(10);

        this.vgo_BllTramiteTemporal.InsertarRegistro(TipoTramiteTemporal.EXPORTACION,
                CrearRegistroExportacion(REFERENCIA_EXPORTACION, false, BigDecimal.ONE, ID_PAIS,
                        vld_Dua, vld_Dua.plusMonths(3)),
                Map.of(), ID_USUARIO);

        ArgumentCaptor<EntTramiteTemporal> vlo_Captor = ArgumentCaptor.forClass(EntTramiteTemporal.class);
        verify(vgo_DalTramiteTemporal).SIMT_TRAMITE_TEMPORAL_InsertarRegistro(vlo_Captor.capture());
        assertNull(vlo_Captor.getValue().getUnidadPlazo());
        assertNull(vlo_Captor.getValue().getCantidadPlazo());
    }

    @Test
    void InsertarRegistro_VencimientoAnteriorAlDua_RechazaElRegistro() {
        LocalDate vld_Dua = HOY.minusDays(10);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarRegistro(TipoTramiteTemporal.EXPORTACION,
                        CrearRegistroExportacion(REFERENCIA_EXPORTACION, false, BigDecimal.ONE, ID_PAIS,
                                vld_Dua, vld_Dua.minusMonths(6)),
                        Map.of(), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_VENCIMIENTO_ANTERIOR_AL_DUA, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_ExportacionSinGarantia_RechazaElRegistro() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarRegistro(TipoTramiteTemporal.EXPORTACION,
                        CrearRegistroExportacion(REFERENCIA_EXPORTACION, null, BigDecimal.ONE, ID_PAIS, null, null),
                        Map.of(), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_GARANTIA_REQUERIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_ExportacionSinPeso_RechazaElRegistro() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarRegistro(TipoTramiteTemporal.EXPORTACION,
                        CrearRegistroExportacion(REFERENCIA_EXPORTACION, false, BigDecimal.ZERO, ID_PAIS, null, null),
                        Map.of(), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_PESO_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_ExportacionConPaisNoRegistrado_RechazaElRegistro() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarRegistro(TipoTramiteTemporal.EXPORTACION,
                        CrearRegistroExportacion(REFERENCIA_EXPORTACION, false, BigDecimal.ONE, 999, null, null),
                        Map.of(), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_PAIS_DESTINO_INVALIDO, vlo_Excepcion.getMessage());
        verify(vgo_DalTramite, never()).SIMT_TRAMITE_InsertarRegistro(any());
    }

    @Test
    void InsertarRegistro_ExportacionConSiglaDeImportacion_RechazaElRegistro() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarRegistro(TipoTramiteTemporal.EXPORTACION,
                        CrearRegistroExportacion(REFERENCIA, false, BigDecimal.ONE, ID_PAIS, null, null),
                        Map.of(), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_REFERENCIA_EXPORTACION_TEMPORAL_INVALIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_ExportacionConDuaDeImportacion_RechazaElDocumento() {
        MultipartFile vlo_Archivo = new MockMultipartFile("DUA_IMPORT_TEMPORAL", "dua.pdf", "application/pdf", new byte[] {1});

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarRegistro(TipoTramiteTemporal.EXPORTACION,
                        CrearRegistroExportacion(REFERENCIA_EXPORTACION, false, BigDecimal.ONE, ID_PAIS, null, null),
                        Map.of("DUA_IMPORT_TEMPORAL", List.of(vlo_Archivo)), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_TIPO_DOCUMENTO_EXPORTACION_TEMPORAL_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_ImportacionConDatosDeSalida_LosIgnora() {
        EntRegistroTramiteTemporal vlo_Registro = new EntRegistroTramiteTemporal(
                REFERENCIA, "ECG", "ECG-82-2025", HOY, "Equipo sísmico", "usada",
                BigDecimal.TEN, "GBP", null, null, null, null, null, null, null, null, null, null,
                "Placa UCR 409901", true, BigDecimal.TEN, ID_PAIS, "OS-1062-2025", "correo 29/9/2025");

        this.vgo_BllTramiteTemporal.InsertarRegistro(TipoTramiteTemporal.IMPORTACION, vlo_Registro, Map.of(), ID_USUARIO);

        ArgumentCaptor<EntTramiteTemporal> vlo_Captor = ArgumentCaptor.forClass(EntTramiteTemporal.class);
        verify(vgo_DalTramiteTemporal).SIMT_TRAMITE_TEMPORAL_InsertarRegistro(vlo_Captor.capture());
        assertNull(vlo_Captor.getValue().getAntecedente());
        assertNull(vlo_Captor.getValue().getIdPaisDestino());
        assertNull(vlo_Captor.getValue().getPesoKg());
        assertNull(vlo_Captor.getValue().getNotificacionDuaAduana());
    }

    @Test
    void ObtenerOpciones_Exportacion_OfreceLosPaisesDeDestino() {
        EntPais vlo_Pais = vgo_DalPais.SIMM_PAIS_ObtenerRegistro(ID_PAIS);
        when(vgo_DalPais.SIMM_PAIS_ObtenerActivos()).thenReturn(List.of(vlo_Pais));

        EntOpcionesTramiteTemporal vlo_Exportacion = this.vgo_BllTramiteTemporal.ObtenerOpciones(TipoTramiteTemporal.EXPORTACION);
        EntOpcionesTramiteTemporal vlo_Importacion = this.vgo_BllTramiteTemporal.ObtenerOpciones(TipoTramiteTemporal.IMPORTACION);

        assertEquals(List.of(vlo_Pais), vlo_Exportacion.paises());
        assertTrue(vlo_Importacion.paises().isEmpty());
    }

    @Test
    void ObtenerExpediente_ExportacionFueraDelPais_MuestraSuPaisYLosPendientesDeSuEtapa() {
        PrepararExpedienteExportacion("Fuera del país", HOY.plusDays(100));

        EntExpedienteTramiteTemporal vlo_Expediente = this.vgo_BllTramiteTemporal.ObtenerExpediente(
                TipoTramiteTemporal.EXPORTACION, REFERENCIA_EXPORTACION);

        assertEquals("Dinamarca", vlo_Expediente.paisDestino());
        assertEquals(List.of("Oficio enviado a la aduana", "Número de gestión asignado por la aduana",
                "Resolución de autorización", "Oficio de notificación al usuario", "DUA de exportación temporal",
                "Notificación del DUA de salida a la aduana"), vlo_Expediente.pendientes());
    }

    @Test
    void ObtenerExpediente_ExportacionEnReimportacion_PideLosDatosDelReingreso() {
        PrepararExpedienteExportacion("En reimportación", HOY.plusDays(20));
        when(vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerCodigosTipoVigentes(ID_TRAMITE, Constantes.ESTADO_DOCUMENTO_ACTIVO))
                .thenReturn(List.of(Constantes.CODIGO_TIPO_DOCUMENTO_EMBARQUE));

        EntExpedienteTramiteTemporal vlo_Expediente = this.vgo_BllTramiteTemporal.ObtenerExpediente(
                TipoTramiteTemporal.EXPORTACION, REFERENCIA_EXPORTACION);

        assertTrue(vlo_Expediente.pendientes().contains("Solicitud de reingreso del usuario"));
        assertTrue(vlo_Expediente.pendientes().contains("DUA de reingreso"));
        assertFalse(vlo_Expediente.pendientes().contains("Documento de embarque"));
    }

    @Test
    void ObtenerExpediente_ExportacionRetenidaEnAduana_PausaLosPendientesPeroMantieneLaCuentaRegresiva() {
        PrepararExpedienteExportacion("Retenido en aduana", HOY.plusDays(15));

        EntExpedienteTramiteTemporal vlo_Expediente = this.vgo_BllTramiteTemporal.ObtenerExpediente(
                TipoTramiteTemporal.EXPORTACION, REFERENCIA_EXPORTACION);

        assertTrue(vlo_Expediente.validacionesPausadas());
        assertTrue(vlo_Expediente.pendientes().isEmpty());
        assertEquals(Constantes.VENCIMIENTO_PROXIMO, vlo_Expediente.vencimiento().nivel());
        assertEquals(15L, vlo_Expediente.vencimiento().diasRestantes());
    }

    @Test
    void ObtenerExpediente_ImportacionPedidaComoExportacion_NoLaEncuentra() {
        PrepararExpediente("Activo en el país", HOY.plusDays(30));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ObtenerExpediente(TipoTramiteTemporal.EXPORTACION, REFERENCIA));

        assertEquals(Constantes.MENSAJE_EXPEDIENTE_EXPORTACION_TEMPORAL_NO_ENCONTRADO, vlo_Excepcion.getMessage());
    }

    @Test
    void ActualizarEstado_FueraDelPaisSinVencimiento_RechazaElCambio() {
        PrepararExpedienteExportacion("En gestión aduanera", null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ActualizarEstado(TipoTramiteTemporal.EXPORTACION,
                        REFERENCIA_EXPORTACION, CrearCambio("Fuera del país", "Salió el equipo"), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_VENCIMIENTO_REQUERIDO_FUERA_DEL_PAIS, vlo_Excepcion.getMessage());
    }

    @Test
    void ActualizarEstado_FinalizarExportacionSinDuaDeReingreso_RechazaElCambio() {
        EntTramite vlo_Tramite = PrepararExpedienteExportacion("En reimportación", HOY.plusDays(5));
        EntTramiteTemporal vlo_Temporal = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);
        vlo_Temporal.setCancelacionRegimen("correo 08-04-2025");
        when(vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerCodigosTipoVigentes(ID_TRAMITE, Constantes.ESTADO_DOCUMENTO_ACTIVO))
                .thenReturn(List.of(Constantes.CODIGO_TIPO_DOCUMENTO_DUA_REEXPORTACION));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ActualizarEstado(TipoTramiteTemporal.EXPORTACION,
                        REFERENCIA_EXPORTACION, CrearCambio("Finalizado", "Regresó el equipo"), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_REQUISITOS_FINALIZACION_EXPORTACION, vlo_Excepcion.getMessage());
        assertEquals(205, vlo_Tramite.getIdEstadoTramite());
    }

    @Test
    void ActualizarEstado_FinalizarExportacionConDuaDeReingresoYCancelacion_CierraElExpediente() {
        EntTramite vlo_Tramite = PrepararExpedienteExportacion("En reimportación", HOY.plusDays(5));
        EntTramiteTemporal vlo_Temporal = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);
        vlo_Temporal.setNumeroDuaCierre("005-2025-222578");
        vlo_Temporal.setCancelacionRegimen("correo 08-04-2025");

        this.vgo_BllTramiteTemporal.ActualizarEstado(TipoTramiteTemporal.EXPORTACION,
                REFERENCIA_EXPORTACION, CrearCambio("Finalizado", "Regresó el equipo"), ID_USUARIO);

        assertEquals(206, vlo_Tramite.getIdEstadoTramite());
        assertEquals(HOY, vlo_Tramite.getFechaCierre());
        verify(vgo_BllAlertaTemporal).AtenderPendientes(ID_TRAMITE);
    }

    @Test
    void ActualizarEstado_SalidaDefinitiva_CierraElExpedienteSinDuaDeReingreso() {
        EntTramite vlo_Tramite = PrepararExpedienteExportacion("Fuera del país", HOY.plusDays(60));

        EntExpedienteTramiteTemporal vlo_Expediente = this.vgo_BllTramiteTemporal.ActualizarEstado(
                TipoTramiteTemporal.EXPORTACION, REFERENCIA_EXPORTACION,
                CrearCambio("Salida definitiva", "El equipo salió por DUA definitivo"), ID_USUARIO);

        assertEquals(207, vlo_Tramite.getIdEstadoTramite());
        assertTrue(vlo_Expediente.estadoFinal());
        assertEquals(HOY, vlo_Tramite.getFechaCierre());
        verify(vgo_BllAlertaTemporal).AtenderPendientes(ID_TRAMITE);
    }

    @Test
    void ActualizarEstado_EstadoDeImportacionEnUnaExportacion_RechazaElCambio() {
        PrepararExpedienteExportacion("Por salir", null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ActualizarEstado(TipoTramiteTemporal.EXPORTACION,
                        REFERENCIA_EXPORTACION, CrearCambio("Activo en el país", "Cambio"), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_ESTADO_EXPORTACION_TEMPORAL_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void ModificarDatos_ExportacionDatosDeReimportacion_LosGuardaYLosAnotaEnLaBitacora() {
        PrepararExpedienteExportacion("En reimportación", HOY.plusDays(20));
        EntTramiteTemporal vlo_Temporal = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);

        this.vgo_BllTramiteTemporal.ModificarDatos(TipoTramiteTemporal.EXPORTACION, REFERENCIA_EXPORTACION,
                CrearDatosReimportacion(HOY.minusDays(3), HOY.minusDays(1)), ID_USUARIO);

        assertEquals("correo", vlo_Temporal.getSolicitudIngreso());
        assertEquals(HOY.minusDays(3), vlo_Temporal.getFechaReingreso());
        assertEquals("1Z93043E6790235617", vlo_Temporal.getDocumentoEmbarque());
        assertEquals(HOY.minusDays(1), vlo_Temporal.getFechaDesalmacenaje());
        verify(vgo_BllBitacora).RegistrarAccion(eq(ID_TRAMITE), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_DATOS_MODIFICADOS), any(), any(), anyString(),
                eq("Solicitud de reingreso: correo. Fecha de reingreso al país: " + HOY.minusDays(3) + "."
                        + " Documento de embarque: 1Z93043E6790235617. Fecha de desalmacenaje: "
                        + HOY.minusDays(1) + "."));
    }

    @Test
    void ModificarDatos_ReimportacionQuePagaImpuestos_GuardaElMonto() {
        PrepararExpedienteExportacion("En reimportación", HOY.plusDays(20));
        EntTramiteTemporal vlo_Temporal = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);

        this.vgo_BllTramiteTemporal.ModificarDatos(TipoTramiteTemporal.EXPORTACION, REFERENCIA_EXPORTACION,
                CrearDatosImpuestosReimportacion(Boolean.TRUE, new BigDecimal("152500.50")), ID_USUARIO);

        assertEquals(Boolean.TRUE, vlo_Temporal.getPagaImpuestosReimportacion());
        assertEquals(new BigDecimal("152500.50"), vlo_Temporal.getMontoImpuestosReimportacion());
    }

    @Test
    void ModificarDatos_ReimportacionSinImpuestosPorGarantia_NoGuardaMonto() {
        PrepararExpedienteExportacion("En reimportación", HOY.plusDays(20));
        EntTramiteTemporal vlo_Temporal = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);

        this.vgo_BllTramiteTemporal.ModificarDatos(TipoTramiteTemporal.EXPORTACION, REFERENCIA_EXPORTACION,
                CrearDatosImpuestosReimportacion(Boolean.FALSE, new BigDecimal("1000")), ID_USUARIO);

        assertEquals(Boolean.FALSE, vlo_Temporal.getPagaImpuestosReimportacion());
        assertNull(vlo_Temporal.getMontoImpuestosReimportacion());
    }

    @Test
    void ModificarDatos_ReimportacionConImpuestosNegativos_RechazaElCambio() {
        PrepararExpedienteExportacion("En reimportación", HOY.plusDays(20));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ModificarDatos(TipoTramiteTemporal.EXPORTACION,
                        REFERENCIA_EXPORTACION,
                        CrearDatosImpuestosReimportacion(Boolean.TRUE, new BigDecimal("-1")), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_IMPUESTOS_REIMPORTACION_INVALIDOS, vlo_Excepcion.getMessage());
    }

    @Test
    void ModificarDatos_ExportacionConFechaDeReingresoFutura_RechazaElCambio() {
        PrepararExpedienteExportacion("En reimportación", HOY.plusDays(20));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.ModificarDatos(TipoTramiteTemporal.EXPORTACION,
                        REFERENCIA_EXPORTACION, CrearDatosReimportacion(HOY.plusDays(1), null), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_FECHA_REIMPORTACION_INVALIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void RegistrarProrroga_ExportacionConPlazoEstandar_ElVencimientoSeCalculaYSeLimpiaElPlazo() {
        LocalDate vld_Dua = HOY.minusMonths(5);
        PrepararExpedienteExportacion("Fuera del país", vld_Dua.plusMonths(6));
        EntTramiteTemporal vlo_Temporal = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);
        vlo_Temporal.setFechaDuaInicio(vld_Dua);
        vlo_Temporal.setUnidadPlazo("MESES");
        vlo_Temporal.setCantidadPlazo(6);
        EntDocumento vlo_Oficio = new EntDocumento();
        vlo_Oficio.setIdDocumento(501);
        EntDocumento vlo_Resolucion = new EntDocumento();
        vlo_Resolucion.setIdDocumento(502);
        when(vgo_BllDocumento.GuardarDocumento(any(), eq(ID_USUARIO), any(MultipartFile.class),
                eq(Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_PRORROGA), anyList())).thenReturn(vlo_Oficio);
        when(vgo_BllDocumento.GuardarDocumento(any(), eq(ID_USUARIO), any(MultipartFile.class),
                eq(Constantes.CODIGO_TIPO_DOCUMENTO_AUTORIZACION_PRORROGA), anyList())).thenReturn(vlo_Resolucion);

        this.vgo_BllTramiteTemporal.RegistrarProrroga(TipoTramiteTemporal.EXPORTACION, REFERENCIA_EXPORTACION,
                CrearProrroga(vld_Dua.plusMonths(6)), ArchivoProrroga("solicitud.pdf"),
                ArchivoProrroga("resolucion.pdf"), ID_USUARIO);

        assertEquals(vld_Dua.plusMonths(12), vlo_Temporal.getFechaVencimiento());
        assertNull(vlo_Temporal.getUnidadPlazo());
        assertNull(vlo_Temporal.getCantidadPlazo());
    }

    @Test
    void ExtenderPlazo_Exportacion_TomaElTipoDelExpediente() {
        PrepararExpedienteExportacion("Fuera del país", HOY.plusDays(7));
        EntTramiteTemporal vlo_Temporal = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);

        EntExpedienteTramiteTemporal vlo_Expediente = this.vgo_BllTramiteTemporal.ExtenderPlazo(
                REFERENCIA_EXPORTACION, new EntExtensionPlazoTemporal(2, "MESES", null), ID_USUARIO);

        assertEquals(HOY.plusDays(7).plusMonths(2), vlo_Temporal.getFechaVencimiento());
        assertEquals("Fuera del país", vlo_Expediente.estado());
    }

    @Test
    void InsertarDocumentos_ExportacionCerrada_SoloAdmiteElDuaDeReingreso() {
        PrepararExpedienteExportacion("Finalizado", HOY.minusDays(5));
        MultipartFile vlo_Dua = new MockMultipartFile("DUA_REIMPORTACION", "dua.pdf", "application/pdf", new byte[] {1});
        MultipartFile vlo_Oficio = new MockMultipartFile("OFICIO_ADUANA", "oficio.pdf", "application/pdf", new byte[] {1});
        EntDocumento vlo_Documento = new EntDocumento();
        vlo_Documento.setNombreArchivo("dua.pdf");
        when(vgo_BllDocumento.GuardarDocumento(any(), any(), any(), any(), any())).thenReturn(vlo_Documento);

        this.vgo_BllTramiteTemporal.InsertarDocumentos(TipoTramiteTemporal.EXPORTACION, REFERENCIA_EXPORTACION,
                Map.of("DUA_REIMPORTACION", List.of(vlo_Dua)), ID_USUARIO);
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramiteTemporal.InsertarDocumentos(TipoTramiteTemporal.EXPORTACION,
                        REFERENCIA_EXPORTACION, Map.of("OFICIO_ADUANA", List.of(vlo_Oficio)), ID_USUARIO));

        assertEquals(Constantes.MENSAJE_SOLO_DUA_CIERRE_EDITABLE, vlo_Excepcion.getMessage());
        verify(vgo_BllDocumento).GuardarDocumento(any(), eq(ID_USUARIO), eq(vlo_Dua),
                eq(Constantes.CODIGO_TIPO_DOCUMENTO_DUA_REIMPORTACION), any());
    }

    // -----------------------------------------------------------------------
    // Utilidades
    // -----------------------------------------------------------------------

    private EntRegistroTramiteTemporal CrearRegistroExportacion(
            String pvc_Referencia,
            Boolean pvb_EnGarantia,
            BigDecimal pvn_Peso,
            Integer pvn_IdPais,
            LocalDate pvd_FechaDua,
            LocalDate pvd_Vencimiento) {
        return new EntRegistroTramiteTemporal(
                pvc_Referencia, "CICIMA", "CICIMA-368-2025", HOY.minusDays(3),
                "Lámpara LED acoplada a fibra óptica, marca Thorlabs, modelo M530F3, número de serie M01245859",
                "nueva", BigDecimal.valueOf(228078), "CRC", "Santamaría", null, null, null, null,
                pvd_FechaDua == null ? null : "005-2025-639487", pvd_FechaDua, pvd_Vencimiento, null, null,
                "P-011-2024", pvb_EnGarantia, pvn_Peso, pvn_IdPais, "OS-1570-2025", null);
    }

    private EntTramite PrepararExpedienteExportacion(String pvc_Estado, LocalDate pvd_Vencimiento) {
        EntTramite vlo_Tramite = new EntTramite();
        vlo_Tramite.setIdTramite(ID_TRAMITE);
        vlo_Tramite.setReferenciaInterna(REFERENCIA_EXPORTACION);
        vlo_Tramite.setIdTipoTramite(ID_TIPO_EXPORTACION);
        vlo_Tramite.setIdEstadoTramite(this.vgo_MpEstadosExportacion.get(pvc_Estado).getIdEstadoTramite());
        vlo_Tramite.setIdUsuarioResponsable(ID_USUARIO);
        lenient().when(vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(REFERENCIA_EXPORTACION))
                .thenReturn(vlo_Tramite);
        lenient().when(vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistroParaModificar(ID_TRAMITE))
                .thenReturn(vlo_Tramite);

        EntTramiteTemporal vlo_Temporal = new EntTramiteTemporal();
        vlo_Temporal.setIdTramite(ID_TRAMITE);
        vlo_Temporal.setUnidadSolicitante("CICIMA");
        vlo_Temporal.setIdPaisDestino(ID_PAIS);
        vlo_Temporal.setFechaVencimiento(pvd_Vencimiento);
        lenient().when(vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE)).thenReturn(vlo_Temporal);
        lenient().when(vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerCodigosTipoVigentes(
                ID_TRAMITE, Constantes.ESTADO_DOCUMENTO_ACTIVO)).thenReturn(List.of());
        return vlo_Tramite;
    }

    private EntDatosTramiteTemporal CrearDatosImpuestosReimportacion(Boolean pvb_Paga, BigDecimal pvn_Monto) {
        EntTramiteTemporal vlo_Actual = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);
        return new EntDatosTramiteTemporal(null, null, null, null, null, null, null,
                vlo_Actual.getFechaVencimiento(), null, null, null, null, null,
                null, null, null, null, null, null, pvb_Paga, pvn_Monto);
    }

    private EntDatosTramiteTemporal CrearDatosReimportacion(LocalDate pvd_FechaReingreso, LocalDate pvd_Desalmacenaje) {
        EntTramiteTemporal vlo_Actual = vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE);
        return new EntDatosTramiteTemporal(null, null, null, null, null, null, null,
                vlo_Actual.getFechaVencimiento(), null, null, null, null, null,
                null, null, "correo", pvd_FechaReingreso, "1Z93043E6790235617", pvd_Desalmacenaje,
                null, null);
    }

    private EntRegistroTramiteTemporal CrearRegistro(
            String pvc_Referencia,
            BigDecimal pvn_Valor,
            LocalDate pvd_Vencimiento,
            List<Integer> pvo_LsAgentes) {
        return new EntRegistroTramiteTemporal(
                pvc_Referencia, "ECG", "ECG-82-2025", HOY.minusDays(3), "Equipo sísmico", "usada",
                pvn_Valor, "gbp", "Santamaría", "SR", null, null, null, null, null, pvd_Vencimiento,
                null, pvo_LsAgentes, null, null, null, null, null, null);
    }

    private EntTramite PrepararExpediente(String pvc_Estado, LocalDate pvd_Vencimiento) {
        EntTramite vlo_Tramite = new EntTramite();
        vlo_Tramite.setIdTramite(ID_TRAMITE);
        vlo_Tramite.setReferenciaInterna(REFERENCIA);
        vlo_Tramite.setIdTipoTramite(ID_TIPO);
        vlo_Tramite.setIdEstadoTramite(this.vgo_MpEstados.get(pvc_Estado).getIdEstadoTramite());
        vlo_Tramite.setIdUsuarioResponsable(ID_USUARIO);
        lenient().when(vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(REFERENCIA)).thenReturn(vlo_Tramite);
        lenient().when(vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistroParaModificar(ID_TRAMITE))
                .thenReturn(vlo_Tramite);

        EntTramiteTemporal vlo_Temporal = new EntTramiteTemporal();
        vlo_Temporal.setIdTramite(ID_TRAMITE);
        vlo_Temporal.setUnidadSolicitante("ECG");
        vlo_Temporal.setFechaVencimiento(pvd_Vencimiento);
        lenient().when(vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(ID_TRAMITE)).thenReturn(vlo_Temporal);
        lenient().when(vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerCodigosTipoVigentes(
                ID_TRAMITE, Constantes.ESTADO_DOCUMENTO_ACTIVO)).thenReturn(List.of());
        return vlo_Tramite;
    }

    private static EntProrrogaTemporal CrearProrroga(LocalDate pvd_VencimientoActual) {
        return new EntProrrogaTemporal("Necesidad de más tiempo", "OF-123", "GEST-123", false,
                "AUT-123", pvd_VencimientoActual, null, false);
    }

    private static MockMultipartFile ArchivoProrroga(String pvc_Nombre) {
        return new MockMultipartFile("archivo", pvc_Nombre, "application/pdf", new byte[] {1});
    }

    private EntFilaTramiteTemporal CrearFila(String pvc_Referencia, String pvc_Estado, LocalDate pvd_Vencimiento) {
        EntTramite vlo_Tramite = new EntTramite();
        vlo_Tramite.setReferenciaInterna(pvc_Referencia);
        EntTramiteTemporal vlo_Temporal = new EntTramiteTemporal();
        vlo_Temporal.setFechaVencimiento(pvd_Vencimiento);
        return new EntFilaTramiteTemporal(vlo_Tramite, vlo_Temporal, this.vgo_MpEstados.get(pvc_Estado), "USD", null);
    }

    private EntCambioEstado CrearCambio(String pvc_Estado, String pvc_Justificacion) {
        EntCambioEstado vlo_Cambio = new EntCambioEstado();
        vlo_Cambio.setNuevoEstado(pvc_Estado);
        vlo_Cambio.setJustificacion(pvc_Justificacion);
        return vlo_Cambio;
    }

    private EntDatosTramiteTemporal CrearDatos(
            String pvc_NumeroGestion,
            String pvc_DuaCierre,
            LocalDate pvd_FechaDuaCierre,
            LocalDate pvd_Vencimiento) {
        return new EntDatosTramiteTemporal(null, null, null, pvc_NumeroGestion, null, null, null,
                pvd_Vencimiento, pvc_DuaCierre, pvd_FechaDuaCierre, null, null, null,
                null, null, null, null, null, null, null, null);
    }
}
