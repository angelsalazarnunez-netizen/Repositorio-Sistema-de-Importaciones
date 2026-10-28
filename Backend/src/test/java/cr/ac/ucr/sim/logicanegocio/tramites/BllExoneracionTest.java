package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalExoneracion;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCorreccionExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 15/09/2026
 * Descripción: Pruebas de las reglas de negocio de la solicitud de exoneración (HU-018).
 */
@ExtendWith(MockitoExtension.class)
class BllExoneracionTest {

    private static final Integer ID_TIPO_TRAMITE = 5;
    private static final Integer ID_ESTADO_INICIAL = 50;
    private static final Integer ID_USUARIO = 1;
    private static final Integer ID_TRAMITE_GENERADO = 200;
    private static final Map<String, String> NOMBRES_DOCUMENTO = Map.of(
            Constantes.CODIGO_TIPO_DOCUMENTO_EMBARQUE, "Documento de embarque",
            Constantes.CODIGO_TIPO_DOCUMENTO_FACTURA, "Factura o documento equivalente",
            Constantes.CODIGO_TIPO_DOCUMENTO_FORMULARIO_S1, "Formulario S1",
            Constantes.CODIGO_TIPO_DOCUMENTO_PACKING_LIST, "Lista de empaque",
            Constantes.CODIGO_TIPO_DOCUMENTO_EXONET, "EXONET",
            Constantes.CODIGO_TIPO_DOCUMENTO_PERMISO_REGULATORIO, "Permiso regulatorio",
            Constantes.CODIGO_TIPO_DOCUMENTO_TRADUCCION_FACTURA, "Traducción de factura",
            Constantes.CODIGO_TIPO_DOCUMENTO_EXPEDIENTE_SICOP, "Expediente de SICOP",
            Constantes.CODIGO_TIPO_DOCUMENTO_DUA_APLICACION, "DUA de aplicación",
            Constantes.CODIGO_TIPO_DOCUMENTO_OTROS, "Otros documentos");

    @Mock
    private DalTramite vgo_DalTramite;

    @Mock
    private DalExoneracion vgo_DalExoneracion;

    @Mock
    private DalDocumento vgo_DalDocumento;

    @Mock
    private DalHistorialEstado vgo_DalHistorialEstado;

    @Mock
    private DalTipoTramite vgo_DalTipoTramite;

    @Mock
    private DalEstadoTramite vgo_DalEstadoTramite;

    @Mock
    private DalTipoDocumento vgo_DalTipoDocumento;

    @Mock
    private DalUsuario vgo_DalUsuario;

    @Mock
    private AlmacenamientoDocumentos vgo_AlmacenamientoDocumentos;

    @Mock
    private BllBitacora vgo_BllBitacora;

    @Mock
    private BllDocumento vgo_BllDocumento;

    @InjectMocks
    private BllExoneracion vgo_BllExoneracion;

    @BeforeEach
    void PrepararPrueba() {
        this.PrepararCatalogosValidos();
    }

    // ---------------------------------------------------------------- Criterio 1: registro y referencia única

    @Test
    void InsertarRegistro_ConDocumentacionCompleta_DebeCrearLaSolicitudSinPendientes() {
        this.SimularDocumentosVigentes(BllExoneracion.DOCUMENTOS_OBLIGATORIOS);

        EntRespuestaExoneracion vlo_Respuesta = this.vgo_BllExoneracion.InsertarRegistro(
                this.CrearRegistroValido(), this.CrearDocumentosObligatorios());

        assertEquals("EXO-2026-0001", vlo_Respuesta.getReferenciaInterna());
        assertEquals(ID_TRAMITE_GENERADO, vlo_Respuesta.getIdTramite());
        assertEquals(4, vlo_Respuesta.getCantidadDocumentos());
        assertTrue(vlo_Respuesta.getDocumentosPendientes().isEmpty());
        assertEquals(Constantes.MENSAJE_REGISTRO_EXONERACION_EXITOSO, vlo_Respuesta.getMensaje());
    }

    @Test
    void InsertarRegistro_ConDatosValidos_DebeGuardarLosDatosContractualesDeLaExoneracion() {
        this.vgo_BllExoneracion.InsertarRegistro(this.CrearRegistroValido(), Map.of());

        ArgumentCaptor<EntExoneracion> vlo_Captor = ArgumentCaptor.forClass(EntExoneracion.class);
        verify(this.vgo_DalExoneracion).SIMT_EXONERACION_InsertarRegistro(vlo_Captor.capture());
        EntExoneracion vlo_EnExoneracion = vlo_Captor.getValue();

        assertEquals(ID_TRAMITE_GENERADO, vlo_EnExoneracion.getIdTramite());
        assertEquals(Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL, vlo_EnExoneracion.getTipoExoneracion());
        assertEquals("2026LA-000123", vlo_EnExoneracion.getNumeroContratacion());
        assertEquals("OC-45871", vlo_EnExoneracion.getOrdenCompraGeco());
        assertEquals("0432026000100045", vlo_EnExoneracion.getContratoSicop());
    }

    @Test
    void InsertarRegistro_SinMonedaNiValor_DebeCrearElTramiteSinEsosDatos() {
        this.vgo_BllExoneracion.InsertarRegistro(this.CrearRegistroValido(), Map.of());

        ArgumentCaptor<EntTramite> vlo_Captor = ArgumentCaptor.forClass(EntTramite.class);
        verify(this.vgo_DalTramite).SIMT_TRAMITE_InsertarRegistro(vlo_Captor.capture());

        assertNull(vlo_Captor.getValue().getIdMoneda());
        assertNull(vlo_Captor.getValue().getValorEstimado());
        assertEquals(ID_USUARIO, vlo_Captor.getValue().getIdUsuarioResponsable());
    }

    @Test
    void InsertarRegistro_ConReferenciaEnMinusculas_DebeNormalizarlaAMayusculas() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setReferenciaInterna("  exo-2026-0001 ");

        EntRespuestaExoneracion vlo_Respuesta = this.vgo_BllExoneracion.InsertarRegistro(vlo_Registro, Map.of());

        assertEquals("EXO-2026-0001", vlo_Respuesta.getReferenciaInterna());
    }

    @Test
    void InsertarRegistro_ConReferenciaDe45Caracteres_DebeAceptarla() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setReferenciaInterna("EXO-" + "9".repeat(41));

        EntRespuestaExoneracion vlo_Respuesta = this.vgo_BllExoneracion.InsertarRegistro(vlo_Registro, Map.of());

        assertEquals(45, vlo_Respuesta.getReferenciaInterna().length());
    }

    @Test
    void InsertarRegistro_ConReferenciaDe46Caracteres_DebeLanzarExcepcion() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setReferenciaInterna("EXO-" + "9".repeat(42));

        this.AfirmarExcepcion(vlo_Registro, Constantes.MENSAJE_REFERENCIA_EXONERACION_INVALIDA);
    }

    @Test
    void InsertarRegistro_ConReferenciaDeOtroTipoDeTramite_DebeLanzarExcepcion() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setReferenciaInterna("DO-2026-0001");

        this.AfirmarExcepcion(vlo_Registro, Constantes.MENSAJE_REFERENCIA_EXONERACION_INVALIDA);
        verify(this.vgo_DalTramite, never()).SIMT_TRAMITE_InsertarRegistro(any());
    }

    @Test
    void InsertarRegistro_ConReferenciaSoloConElPrefijo_DebeLanzarExcepcion() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setReferenciaInterna("EXO-");

        this.AfirmarExcepcion(vlo_Registro, Constantes.MENSAJE_REFERENCIA_EXONERACION_INVALIDA);
    }

    @Test
    void InsertarRegistro_ConReferenciaDuplicada_DebeLanzarExcepcion() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ExistePorReferenciaInterna("EXO-2026-0001")).thenReturn(true);

        this.AfirmarExcepcion(this.CrearRegistroValido(), Constantes.MENSAJE_REFERENCIA_DUPLICADA);
    }

    // ---------------------------------------------------------------- Criterio 2: documentación pendiente

    @Test
    void InsertarRegistro_SinDocumentacionObligatoria_DebeRegistrarlaYAdvertirLoPendiente() {
        EntRespuestaExoneracion vlo_Respuesta = this.vgo_BllExoneracion.InsertarRegistro(
                this.CrearRegistroValido(), Map.of());

        assertEquals(List.of("Documento de embarque", "Factura o documento equivalente", "Formulario S1",
                "Lista de empaque"), vlo_Respuesta.getDocumentosPendientes());
        assertEquals(Constantes.MENSAJE_REGISTRO_EXONERACION_CON_PENDIENTES, vlo_Respuesta.getMensaje());
        verify(this.vgo_DalExoneracion).SIMT_EXONERACION_InsertarRegistro(any());
    }

    @Test
    void InsertarRegistro_ConDocumentacionParcial_DebeInformarSoloLosDocumentosFaltantes() {
        this.SimularDocumentosVigentes(List.of(Constantes.CODIGO_TIPO_DOCUMENTO_EMBARQUE,
                Constantes.CODIGO_TIPO_DOCUMENTO_FACTURA));

        EntRespuestaExoneracion vlo_Respuesta = this.vgo_BllExoneracion.InsertarRegistro(
                this.CrearRegistroValido(), Map.of(
                        Constantes.CODIGO_TIPO_DOCUMENTO_EMBARQUE, List.of(this.CrearArchivo("guia.pdf")),
                        Constantes.CODIGO_TIPO_DOCUMENTO_FACTURA, List.of(this.CrearArchivo("factura.pdf"))));

        assertEquals(List.of("Formulario S1", "Lista de empaque"), vlo_Respuesta.getDocumentosPendientes());
    }

    @Test
    void InsertarRegistro_ConTipoDeDocumentoNoAdmitido_DebeDescartarlo() {
        Map<String, List<MultipartFile>> vlo_MpDocumentos = new LinkedHashMap<>();
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_RESOLUCION_ACTA, List.of(this.CrearArchivo("acta.pdf")));
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_EXONET, List.of(this.CrearArchivo("exonet.pdf")));

        EntRespuestaExoneracion vlo_Respuesta = this.vgo_BllExoneracion.InsertarRegistro(
                this.CrearRegistroValido(), vlo_MpDocumentos);

        assertEquals(1, vlo_Respuesta.getCantidadDocumentos());
        verify(this.vgo_DalDocumento, times(1)).SIMF_DOCUMENTO_InsertarRegistro(any());
    }

    @Test
    void InsertarRegistro_ConPermisosRegulatorios_DebeDescartarlosPorqueLaExoneracionNoLosLleva() {
        Map<String, List<MultipartFile>> vlo_MpDocumentos = new LinkedHashMap<>();
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_PERMISO_REGULATORIO,
                List.of(this.CrearArchivo("permiso-salud.pdf")));

        EntRespuestaExoneracion vlo_Respuesta = this.vgo_BllExoneracion.InsertarRegistro(
                this.CrearRegistroValido(), vlo_MpDocumentos);

        assertEquals(0, vlo_Respuesta.getCantidadDocumentos());
        verify(this.vgo_DalDocumento, never()).SIMF_DOCUMENTO_InsertarRegistro(any());
    }

    @Test
    void InsertarRegistro_ConExpedienteSicopYOtrosDocumentos_DebeRegistrarlosTodos() {
        Map<String, List<MultipartFile>> vlo_MpDocumentos = new LinkedHashMap<>();
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_EXPEDIENTE_SICOP,
                List.of(this.CrearArchivo("expediente-sicop.pdf")));
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_DUA_APLICACION,
                List.of(this.CrearArchivo("dua-aplicacion.pdf")));
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_OTROS,
                List.of(this.CrearArchivo("correo-1.pdf"), this.CrearArchivo("correo-2.pdf")));

        EntRespuestaExoneracion vlo_Respuesta = this.vgo_BllExoneracion.InsertarRegistro(
                this.CrearRegistroValido(), vlo_MpDocumentos);

        assertEquals(4, vlo_Respuesta.getCantidadDocumentos());
        verify(this.vgo_DalDocumento, times(4)).SIMF_DOCUMENTO_InsertarRegistro(any());
    }

    @Test
    void InsertarDocumentos_QueCompletanLoPendiente_DebeRegistrarlosYAnotarlosEnLaBitacora() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna("EXO-2026-0001"))
                .thenReturn(this.CrearTramiteRegistrado());
        when(this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(ID_TRAMITE_GENERADO))
                .thenReturn(this.CrearExoneracionRegistrada());
        this.SimularDocumentosVigentes(BllExoneracion.DOCUMENTOS_OBLIGATORIOS);

        this.vgo_BllExoneracion.InsertarDocumentos("EXO-2026-0001", ID_USUARIO, Map.of(
                Constantes.CODIGO_TIPO_DOCUMENTO_FORMULARIO_S1, List.of(this.CrearArchivo("s1.pdf"))));

        ArgumentCaptor<String> vlo_Captor = ArgumentCaptor.forClass(String.class);
        verify(this.vgo_BllBitacora).RegistrarAccion(eq(ID_TRAMITE_GENERADO), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_DOCUMENTO_AGREGADO), eq(Constantes.ENTIDAD_BITACORA_DOCUMENTO),
                any(), isNull(), vlo_Captor.capture());
        assertTrue(vlo_Captor.getValue().contains("La documentación obligatoria quedó completa."));
    }

    @Test
    void InsertarDocumentos_SinArchivos_DebeLanzarExcepcion() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna("EXO-2026-0001"))
                .thenReturn(this.CrearTramiteRegistrado());
        when(this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(ID_TRAMITE_GENERADO))
                .thenReturn(this.CrearExoneracionRegistrada());

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllExoneracion.InsertarDocumentos("EXO-2026-0001", ID_USUARIO, Map.of()));

        assertEquals(Constantes.MENSAJE_SIN_DOCUMENTOS_ADJUNTOS, vlo_Excepcion.getMessage());
    }

    // ---------------------------------------------------------------- Criterio 3: estado inicial

    @Test
    void InsertarRegistro_DebeAsignarElEstadoInicialPendienteRevisionDocumental() {
        EntRespuestaExoneracion vlo_Respuesta = this.vgo_BllExoneracion.InsertarRegistro(
                this.CrearRegistroValido(), Map.of());

        ArgumentCaptor<EntTramite> vlo_Captor = ArgumentCaptor.forClass(EntTramite.class);
        verify(this.vgo_DalTramite).SIMT_TRAMITE_InsertarRegistro(vlo_Captor.capture());
        assertEquals(ID_ESTADO_INICIAL, vlo_Captor.getValue().getIdEstadoTramite());
        assertEquals("Pendiente revisión documental", vlo_Respuesta.getEstado());
        verify(this.vgo_DalHistorialEstado).SIMH_ESTADO_TRAMITE_InsertarRegistro(any());
    }

    // ---------------------------------------------------------------- Criterio 4: bitácora

    @Test
    void InsertarRegistro_DebeRegistrarLaAccionEnLaBitacoraConLoPendiente() {
        this.vgo_BllExoneracion.InsertarRegistro(this.CrearRegistroValido(), Map.of());

        ArgumentCaptor<String> vlo_Captor = ArgumentCaptor.forClass(String.class);
        verify(this.vgo_BllBitacora).RegistrarAccion(eq(ID_TRAMITE_GENERADO), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_REGISTRO), eq(Constantes.ENTIDAD_BITACORA_EXONERACION),
                eq(ID_TRAMITE_GENERADO), isNull(), vlo_Captor.capture());
        assertTrue(vlo_Captor.getValue().contains("EXO-2026-0001"));
        assertTrue(vlo_Captor.getValue().contains("Documentación obligatoria pendiente"));
    }

    // ---------------------------------------------------------------- Validaciones de datos

    @Test
    void InsertarRegistro_ConTipoDeExoneracionNoAdmitido_DebeLanzarExcepcion() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setTipoExoneracion("PERSONAL");

        this.AfirmarExcepcion(vlo_Registro, Constantes.MENSAJE_TIPO_EXONERACION_INVALIDO);
    }

    @Test
    void InsertarRegistro_SinContratoSicop_DebeRegistrarLaSolicitud() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setContratoSicop("   ");

        EntRespuestaExoneracion vlo_Respuesta =
                this.vgo_BllExoneracion.InsertarRegistro(vlo_Registro, Map.of());

        assertEquals("EXO-2026-0001", vlo_Respuesta.getReferenciaInterna());
    }

    @Test
    void InsertarRegistro_ConContratoSicopDe46Caracteres_DebeLanzarExcepcion() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setContratoSicop("9".repeat(46));

        this.AfirmarExcepcion(vlo_Registro, Constantes.MENSAJE_CONTRATO_SICOP_INVALIDO);
    }

    @Test
    void InsertarRegistro_ConOrdenDeCompraDe46Caracteres_DebeLanzarExcepcion() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setOrdenCompraGeco("9".repeat(46));

        this.AfirmarExcepcion(vlo_Registro, Constantes.MENSAJE_ORDEN_COMPRA_GECO_INVALIDA);
    }

    @Test
    void InsertarRegistro_SinOrdenDeCompraGeco_DebeRegistrarLaSolicitud() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setOrdenCompraGeco("   ");

        EntRespuestaExoneracion vlo_Respuesta =
                this.vgo_BllExoneracion.InsertarRegistro(vlo_Registro, Map.of());

        assertEquals("EXO-2026-0001", vlo_Respuesta.getReferenciaInterna());
        ArgumentCaptor<EntExoneracion> vlo_Captor = ArgumentCaptor.forClass(EntExoneracion.class);
        verify(this.vgo_DalExoneracion).SIMT_EXONERACION_InsertarRegistro(vlo_Captor.capture());
        assertEquals("", vlo_Captor.getValue().getOrdenCompraGeco());
    }

    @Test
    void InsertarRegistro_ConTipoDeExoneracionDeVehiculos_DebeRegistrarLaSolicitud() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setTipoExoneracion(Constantes.TIPO_EXONERACION_VEHICULOS);

        EntRespuestaExoneracion vlo_Respuesta =
                this.vgo_BllExoneracion.InsertarRegistro(vlo_Registro, Map.of());

        assertEquals(Constantes.TIPO_EXONERACION_VEHICULOS, vlo_Respuesta.getTipoExoneracion());
    }

    @Test
    void InsertarRegistro_ConTipoDeMovimientoDeTraspaso_DebeRegistrarLaSolicitud() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setTipoExoneracion(Constantes.TIPO_EXONERACION_MOVIMIENTO_TRASPASO);

        EntRespuestaExoneracion vlo_Respuesta =
                this.vgo_BllExoneracion.InsertarRegistro(vlo_Registro, Map.of());

        assertEquals(Constantes.TIPO_EXONERACION_MOVIMIENTO_TRASPASO, vlo_Respuesta.getTipoExoneracion());
    }

    @Test
    void InsertarRegistro_ConLosDosNumerosDeExonet_DebeGuardarlos() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setNumeroExonet("2026-118745");
        vlo_Registro.setNumeroAutorizacionExonet("AUT-2026-9981");

        this.vgo_BllExoneracion.InsertarRegistro(vlo_Registro, Map.of());

        ArgumentCaptor<EntExoneracion> vlo_Captor = ArgumentCaptor.forClass(EntExoneracion.class);
        verify(this.vgo_DalExoneracion).SIMT_EXONERACION_InsertarRegistro(vlo_Captor.capture());
        assertEquals("2026-118745", vlo_Captor.getValue().getNumeroExonet());
        assertEquals("AUT-2026-9981", vlo_Captor.getValue().getNumeroAutorizacionExonet());
    }

    @Test
    void InsertarRegistro_ConAutorizacionDeExonetDe46Caracteres_DebeLanzarExcepcion() {
        EntRegistroExoneracion vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setNumeroAutorizacionExonet("9".repeat(46));

        this.AfirmarExcepcion(vlo_Registro, Constantes.MENSAJE_AUTORIZACION_EXONET_INVALIDA);
    }

    @Test
    void InsertarRegistro_CuandoElCatalogoNoExigeSicop_DebeLanzarExcepcion() {
        EntTipoTramite vlo_EnTipo = this.CrearTipoTramite();
        vlo_EnTipo.setRequiereSicop(Boolean.FALSE);
        when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerPorCodigo(anyString())).thenReturn(vlo_EnTipo);

        this.AfirmarExcepcion(this.CrearRegistroValido(), Constantes.MENSAJE_TIPO_TRAMITE_EXONERACION_NO_CONFIGURADO);
    }

    @Test
    void ObtenerExpediente_DeUnTramiteQueNoEsExoneracion_DebeLanzarExcepcion() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna("DE-2026-0001"))
                .thenReturn(this.CrearTramiteRegistrado());
        when(this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(any())).thenReturn(null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllExoneracion.ObtenerExpediente("DE-2026-0001"));

        assertEquals(Constantes.MENSAJE_EXONERACION_NO_ENCONTRADA, vlo_Excepcion.getMessage());
    }

    // ------------------------------------- HU-018: correcciones sobre la solicitud registrada

    @Test
    void ModificarDatosContractuales_ConUnDatoNuevo_DebeGuardarloYAnotarloEnLaBitacora() {
        this.PrepararSolicitudRegistrada();
        EntCorreccionExoneracion vlo_Correccion = new EntCorreccionExoneracion();
        vlo_Correccion.setIdUsuarioResponsable(ID_USUARIO);
        vlo_Correccion.setContratoSicop("2026-000999");

        this.vgo_BllExoneracion.ModificarDatosContractuales("EXO-2026-0001", vlo_Correccion);

        verify(this.vgo_DalExoneracion).SIMT_EXONERACION_ModificarRegistro(any());
        verify(this.vgo_BllBitacora).RegistrarAccion(
                eq(ID_TRAMITE_GENERADO), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_DATOS_MODIFICADOS),
                eq(Constantes.ENTIDAD_BITACORA_EXONERACION),
                eq(ID_TRAMITE_GENERADO), anyString(), anyString());
    }

    @Test
    void ModificarDatosContractuales_SinIndicarCambios_DebeLanzarExcepcion() {
        this.PrepararSolicitudRegistrada();
        EntCorreccionExoneracion vlo_Correccion = new EntCorreccionExoneracion();
        vlo_Correccion.setIdUsuarioResponsable(ID_USUARIO);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllExoneracion.ModificarDatosContractuales("EXO-2026-0001", vlo_Correccion));

        assertEquals(Constantes.MENSAJE_DATOS_CONTRACTUALES_SIN_CAMBIO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalExoneracion, never()).SIMT_EXONERACION_ModificarRegistro(any());
    }

    @Test
    void EliminarDocumento_DeUnaSolicitudRegistrada_DebeRetirarloDelExpediente() {
        this.PrepararSolicitudRegistrada();

        this.vgo_BllExoneracion.EliminarDocumento("EXO-2026-0001", 77, ID_USUARIO);

        verify(this.vgo_BllDocumento).EliminarDocumento(ID_TRAMITE_GENERADO, 77, ID_USUARIO);
    }

    // ---------------------------------------------------------------- Preparación

    private void PrepararSolicitudRegistrada() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(anyString()))
                .thenReturn(this.CrearTramiteRegistrado());
        when(this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(any()))
                .thenReturn(this.CrearExoneracionRegistrada());
    }

    private void PrepararCatalogosValidos() {
        lenient().when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerPorCodigo(anyString())).thenReturn(this.CrearTipoTramite());
        lenient().when(this.vgo_DalTramite.SIMT_TRAMITE_ExistePorReferenciaInterna(anyString())).thenReturn(false);
        lenient().when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(any())).thenReturn(this.CrearUsuario());
        lenient().when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerEstadoInicial(any())).thenReturn(this.CrearEstadoInicial());
        lenient().when(this.vgo_DalTipoDocumento.SIMM_TIPO_DOCUMENTO_ObtenerPorCodigo(anyString())).thenAnswer(pvo_Invocacion -> {
            String vlc_Codigo = pvo_Invocacion.getArgument(0);
            EntTipoDocumento vlo_EnTipo = new EntTipoDocumento();
            vlo_EnTipo.setIdTipoDocumento(vlc_Codigo.hashCode());
            vlo_EnTipo.setCodigo(vlc_Codigo);
            vlo_EnTipo.setNombre(NOMBRES_DOCUMENTO.getOrDefault(vlc_Codigo, vlc_Codigo));
            return vlo_EnTipo;
        });
        lenient().when(this.vgo_AlmacenamientoDocumentos.GuardarArchivo(any(), anyString())).thenReturn("EXO-2026-0001/archivo");
        lenient().when(this.vgo_AlmacenamientoDocumentos.DepurarNombreArchivo(any())).thenAnswer(pvo_Invocacion -> pvo_Invocacion.getArgument(0));
        lenient().when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerVersionVigente(any(), any(), any(), anyString())).thenReturn(null);
        this.SimularDocumentosVigentes(List.of());

        lenient().doAnswer(pvo_Invocacion -> {
            EntTramite vlo_EnTramite = pvo_Invocacion.getArgument(0);
            vlo_EnTramite.setIdTramite(ID_TRAMITE_GENERADO);
            return null;
        }).when(this.vgo_DalTramite).SIMT_TRAMITE_InsertarRegistro(any());
    }

    private void SimularDocumentosVigentes(List<String> pvo_LsCodigos) {
        lenient().when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerCodigosTipoVigentes(any(), anyString()))
                .thenReturn(pvo_LsCodigos);
    }

    private void AfirmarExcepcion(EntRegistroExoneracion pvo_Registro, String pvc_MensajeEsperado) {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllExoneracion.InsertarRegistro(pvo_Registro, Map.of()));
        assertEquals(pvc_MensajeEsperado, vlo_Excepcion.getMessage());
    }

    private EntRegistroExoneracion CrearRegistroValido() {
        EntRegistroExoneracion vlo_Registro = new EntRegistroExoneracion();
        vlo_Registro.setReferenciaInterna("EXO-2026-0001");
        vlo_Registro.setTipoExoneracion(Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL);
        vlo_Registro.setNumeroContratacion("2026LA-000123");
        vlo_Registro.setOrdenCompraGeco("OC-45871");
        vlo_Registro.setContratoSicop("0432026000100045");
        vlo_Registro.setObservaciones("Equipo de laboratorio para la Escuela de Química.");
        vlo_Registro.setIdUsuarioResponsable(ID_USUARIO);
        return vlo_Registro;
    }

    private Map<String, List<MultipartFile>> CrearDocumentosObligatorios() {
        Map<String, List<MultipartFile>> vlo_MpDocumentos = new LinkedHashMap<>();
        for (String vlc_Codigo : BllExoneracion.DOCUMENTOS_OBLIGATORIOS) {
            vlo_MpDocumentos.put(vlc_Codigo, List.of(this.CrearArchivo(vlc_Codigo.toLowerCase() + ".pdf")));
        }
        return vlo_MpDocumentos;
    }

    private EntTipoTramite CrearTipoTramite() {
        EntTipoTramite vlo_EnTipo = new EntTipoTramite();
        vlo_EnTipo.setIdTipoTramite(ID_TIPO_TRAMITE);
        vlo_EnTipo.setCodigo(Constantes.CODIGO_TIPO_TRAMITE_EXONERACION);
        vlo_EnTipo.setPrefijoReferencia("EXO-");
        vlo_EnTipo.setRequiereSicop(Boolean.TRUE);
        vlo_EnTipo.setRequiereOrdenCompra(Boolean.TRUE);
        return vlo_EnTipo;
    }

    private EntEstadoTramite CrearEstadoInicial() {
        EntEstadoTramite vlo_EnEstado = new EntEstadoTramite();
        vlo_EnEstado.setIdEstadoTramite(ID_ESTADO_INICIAL);
        vlo_EnEstado.setNombre("Pendiente revisión documental");
        vlo_EnEstado.setEsInicial(Boolean.TRUE);
        return vlo_EnEstado;
    }

    private EntUsuario CrearUsuario() {
        EntUsuario vlo_EnUsuario = new EntUsuario();
        vlo_EnUsuario.setIdUsuario(ID_USUARIO);
        vlo_EnUsuario.setNombreCompleto("Técnica de Exoneraciones");
        return vlo_EnUsuario;
    }

    private EntTramite CrearTramiteRegistrado() {
        EntTramite vlo_EnTramite = new EntTramite();
        vlo_EnTramite.setIdTramite(ID_TRAMITE_GENERADO);
        vlo_EnTramite.setReferenciaInterna("EXO-2026-0001");
        vlo_EnTramite.setIdEstadoTramite(ID_ESTADO_INICIAL);
        vlo_EnTramite.setIdUsuarioResponsable(ID_USUARIO);
        return vlo_EnTramite;
    }

    private EntExoneracion CrearExoneracionRegistrada() {
        EntExoneracion vlo_EnExoneracion = new EntExoneracion();
        vlo_EnExoneracion.setIdTramite(ID_TRAMITE_GENERADO);
        vlo_EnExoneracion.setTipoExoneracion(Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL);
        return vlo_EnExoneracion;
    }

    private MultipartFile CrearArchivo(String pvc_Nombre) {
        return new MockMultipartFile("archivo", pvc_Nombre, "application/pdf", "contenido".getBytes());
    }
}
