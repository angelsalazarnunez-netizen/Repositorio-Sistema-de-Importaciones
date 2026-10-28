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
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoDocumental;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExpedienteDetalle;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMoneda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroDesalmacenaje;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaDesalmacenaje;
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

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Pruebas de las reglas de negocio del registro de desalmacenaje especial (HU-031).
 */
@ExtendWith(MockitoExtension.class)
class BllDesalmacenajeEspecialTest {

    private static final Integer ID_TIPO_TRAMITE = 1;
    private static final Integer ID_ESTADO_INICIAL = 10;
    private static final Integer ID_USUARIO = 1;
    private static final Integer ID_TRAMITE_GENERADO = 100;

    @Mock
    private DalTramite vgo_DalTramite;

    @Mock
    private DalDesalmacenajeEspecial vgo_DalDesalmacenajeEspecial;

    @Mock
    private DalDocumento vgo_DalDocumento;

    @Mock
    private DalHistorialEstado vgo_DalHistorialEstado;

    @Mock
    private DalTipoTramite vgo_DalTipoTramite;

    @Mock
    private DalEstadoTramite vgo_DalEstadoTramite;

    @Mock
    private DalMoneda vgo_DalMoneda;

    @Mock
    private DalPais vgo_DalPais;

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
    private BllDesalmacenajeEspecial vgo_BllDesalmacenajeEspecial;

    @BeforeEach
    void PrepararPrueba() {

        this.PrepararCatalogosValidos();
    }

    @Test
    void InsertarRegistro_ConDatosValidos_DebeCrearElExpedienteEnEstadoPendiente() {
        EntRespuestaDesalmacenaje vlo_Respuesta = this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                this.CrearRegistroValido(),
                List.of(this.CrearArchivo("acta-salida-2015.pdf")),
                null,
                null,
                null);

        assertEquals("DE-2026-0001", vlo_Respuesta.getReferenciaInterna());
        assertEquals("Pendiente", vlo_Respuesta.getEstado());
        assertEquals(ID_TRAMITE_GENERADO, vlo_Respuesta.getIdTramite());
        assertEquals("Katherine Valverde", vlo_Respuesta.getUsuarioResponsable());
        assertEquals(1, vlo_Respuesta.getCantidadDocumentos());
        assertEquals(Constantes.MENSAJE_REGISTRO_DESALMACENAJE_EXITOSO, vlo_Respuesta.getMensaje());
    }

    @Test
    void InsertarRegistro_ConDatosValidos_DebeAsociarElExpedienteAlUsuarioResponsable() {
        this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                this.CrearRegistroValido(),
                List.of(this.CrearArchivo("justificacion.docx")),
                null,
                null,
                null);

        ArgumentCaptor<EntTramite> vlo_Captor = ArgumentCaptor.forClass(EntTramite.class);
        verify(this.vgo_DalTramite).SIMT_TRAMITE_InsertarRegistro(vlo_Captor.capture());
        EntTramite vlo_EnTramite = vlo_Captor.getValue();

        assertEquals(ID_USUARIO, vlo_EnTramite.getIdUsuarioResponsable());
        assertEquals(ID_ESTADO_INICIAL, vlo_EnTramite.getIdEstadoTramite());
        assertEquals(ID_TIPO_TRAMITE, vlo_EnTramite.getIdTipoTramite());
        assertEquals(new BigDecimal("1500.00"), vlo_EnTramite.getValorEstimado());
    }

    @Test
    void InsertarRegistro_SinPaisDeOrigen_DebeDejarElExpedienteSinPais() {
        EntRegistroDesalmacenaje vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setPaisOrigen("");

        this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                vlo_Registro,
                List.of(this.CrearArchivo("oficio.pdf")),
                null,
                null,
                null);

        ArgumentCaptor<EntTramite> vlo_Captor = ArgumentCaptor.forClass(EntTramite.class);
        verify(this.vgo_DalTramite).SIMT_TRAMITE_InsertarRegistro(vlo_Captor.capture());
        assertNull(vlo_Captor.getValue().getIdPaisOrigen());
    }

    @Test
    void InsertarRegistro_ConDocumentacionAtipica_DebeAsociarTodosLosAdjuntos() {
        EntRespuestaDesalmacenaje vlo_Respuesta = this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                this.CrearRegistroValido(),
                List.of(this.CrearArchivo("acta-1998.tiff"), this.CrearArchivo("oficio-manuscrito.jpg")),
                this.CrearArchivo("packing-list.xlsx"),
                this.CrearArchivo("factura.pdf"),
                this.CrearArchivo("guia-aerea.pdf"));

        assertEquals(5, vlo_Respuesta.getCantidadDocumentos());
        verify(this.vgo_DalDocumento, org.mockito.Mockito.times(5)).SIMF_DOCUMENTO_InsertarRegistro(any());
    }

    @Test
    void InsertarRegistro_DebeGuardarLaJustificacionEnElExpedienteEspecifico() {
        this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                this.CrearRegistroValido(),
                List.of(this.CrearArchivo("acta.pdf")),
                null,
                null,
                null);

        ArgumentCaptor<EntDesalmacenajeEspecial> vlo_Captor =
                ArgumentCaptor.forClass(EntDesalmacenajeEspecial.class);
        verify(this.vgo_DalDesalmacenajeEspecial)
                .SIMT_DESALMACENAJE_ESPEC_InsertarRegistro(vlo_Captor.capture());
        EntDesalmacenajeEspecial vlo_EnDesalmacenaje = vlo_Captor.getValue();

        assertEquals(ID_TRAMITE_GENERADO, vlo_EnDesalmacenaje.getIdTramite());
        assertEquals("Oficina de Suministros", vlo_EnDesalmacenaje.getOrigenDependencia());
    }

    @Test
    void InsertarRegistro_ConReferenciaDeOtroTipoDeTramite_DebeLanzarExcepcion() {
        EntRegistroDesalmacenaje vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setReferenciaInterna("DO-2026-0001");

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                        vlo_Registro,
                        List.of(this.CrearArchivo("acta.pdf")),
                        null,
                        null,
                        null));

        assertEquals(Constantes.MENSAJE_REFERENCIA_INVALIDA, vlo_Excepcion.getMessage());
        verify(this.vgo_DalTramite, never()).SIMT_TRAMITE_InsertarRegistro(any());
    }

    @Test
    void InsertarRegistro_ConReferenciaDuplicada_DebeLanzarExcepcion() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ExistePorReferenciaInterna(anyString())).thenReturn(true);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                        this.CrearRegistroValido(),
                        List.of(this.CrearArchivo("acta.pdf")),
                        null,
                        null,
                        null));

        assertEquals(Constantes.MENSAJE_REFERENCIA_DUPLICADA, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_SinDocumentoDeRespaldo_DebeCrearElExpedienteConLaDocumentacionPendiente() {
        EntRespuestaDesalmacenaje vlo_Respuesta = this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                this.CrearRegistroValido(),
                List.of(),
                null,
                null,
                null);

        assertEquals(0, vlo_Respuesta.getCantidadDocumentos());
        verify(this.vgo_DalTramite).SIMT_TRAMITE_InsertarRegistro(any());
        verify(this.vgo_DalDocumento, never()).SIMF_DOCUMENTO_InsertarRegistro(any());
    }

    @Test
    void EliminarDocumento_DeUnExpedienteRegistrado_DebeRetirarloYAnotarloEnLaBitacora() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(anyString()))
                .thenReturn(this.CrearTramiteRegistrado());

        this.vgo_BllDesalmacenajeEspecial.EliminarDocumento("DE-2026-0001", 55, ID_USUARIO);

        verify(this.vgo_BllDocumento).EliminarDocumento(ID_TRAMITE_GENERADO, 55, ID_USUARIO);
    }

    @Test
    void EliminarDocumento_DeUnExpedienteInexistente_DebeLanzarExcepcion() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(anyString())).thenReturn(null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.EliminarDocumento("DE-2026-9999", 55, ID_USUARIO));

        assertEquals(Constantes.MENSAJE_EXPEDIENTE_NO_ENCONTRADO, vlo_Excepcion.getMessage());
        verify(this.vgo_BllDocumento, never()).EliminarDocumento(any(), any(), any());
    }

    @Test
    void InsertarRegistro_ConJustificacionMuyCorta_DebeLanzarExcepcion() {
        EntRegistroDesalmacenaje vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setJustificacion("Mercancía vieja");

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                        vlo_Registro,
                        List.of(this.CrearArchivo("acta.pdf")),
                        null,
                        null,
                        null));

        assertEquals(Constantes.MENSAJE_JUSTIFICACION_INVALIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_ConValorEstimadoEnCero_DebeLanzarExcepcion() {
        EntRegistroDesalmacenaje vlo_Registro = this.CrearRegistroValido();
        vlo_Registro.setValorEstimado(BigDecimal.ZERO);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                        vlo_Registro,
                        List.of(this.CrearArchivo("acta.pdf")),
                        null,
                        null,
                        null));

        assertEquals(Constantes.MENSAJE_VALOR_ESTIMADO_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_ConUsuarioResponsableInexistente_DebeLanzarExcepcion() {
        when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(any())).thenReturn(null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                        this.CrearRegistroValido(),
                        List.of(this.CrearArchivo("acta.pdf")),
                        null,
                        null,
                        null));

        assertEquals(Constantes.MENSAJE_USUARIO_RESPONSABLE_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_CuandoElTipoDeTramiteExigeSicop_DebeLanzarExcepcion() {
        EntTipoTramite vlo_EnTipoTramite = this.CrearTipoTramite();
        vlo_EnTipoTramite.setRequiereSicop(Boolean.TRUE);
        when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerPorCodigo(anyString()))
                .thenReturn(vlo_EnTipoTramite);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                        this.CrearRegistroValido(),
                        List.of(this.CrearArchivo("acta.pdf")),
                        null,
                        null,
                        null));

        assertEquals(Constantes.MENSAJE_TIPO_TRAMITE_NO_CONFIGURADO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalTramite, never()).SIMT_TRAMITE_InsertarRegistro(any());
    }

    @Test
    void InsertarRegistro_ConDatosValidos_DebeAbrirElHistorialDeEstados() {
        this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                this.CrearRegistroValido(),
                List.of(this.CrearArchivo("acta.pdf")),
                null,
                null,
                null);

        ArgumentCaptor<EntHistorialEstado> vlo_Captor = ArgumentCaptor.forClass(EntHistorialEstado.class);
        verify(this.vgo_DalHistorialEstado).SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_Captor.capture());
        EntHistorialEstado vlo_EnHistorial = vlo_Captor.getValue();

        assertNull(vlo_EnHistorial.getIdEstadoAnterior());
        assertEquals(ID_ESTADO_INICIAL, vlo_EnHistorial.getIdEstadoNuevo());
        assertEquals(ID_TRAMITE_GENERADO, vlo_EnHistorial.getIdTramite());
        assertEquals(ID_USUARIO, vlo_EnHistorial.getIdUsuario());
    }

    @Test
    void InsertarDocumentos_SobreUnExpedienteExistente_DebeAgregarlosAlExpediente() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(anyString()))
                .thenReturn(this.CrearTramiteRegistrado());

        this.vgo_BllDesalmacenajeEspecial.InsertarDocumentos(
                "DE-2026-0001",
                ID_USUARIO,
                List.of(this.CrearArchivo("acta-tardia.pdf")),
                null,
                null,
                null);

        verify(this.vgo_DalDocumento).SIMF_DOCUMENTO_InsertarRegistro(any());
        verify(this.vgo_BllBitacora).RegistrarAccion(
                ID_TRAMITE_GENERADO,
                ID_USUARIO,
                Constantes.ACCION_BITACORA_DOCUMENTO_AGREGADO,
                Constantes.ENTIDAD_BITACORA_DOCUMENTO,
                null,
                null,
                "Documentos agregados al expediente: 1.");
    }

    @Test
    void InsertarDocumentos_ConUnArchivoQueYaExistia_DebeRegistrarLaVersionSiguiente() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(anyString()))
                .thenReturn(this.CrearTramiteRegistrado());
        EntDocumento vlo_EnVersionAnterior = new EntDocumento();
        vlo_EnVersionAnterior.setIdDocumento(55);
        vlo_EnVersionAnterior.setVersion((short) 1);
        vlo_EnVersionAnterior.setEstadoDocumento(Constantes.ESTADO_DOCUMENTO_ACTIVO);
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerVersionVigente(any(), any(), anyString(), anyString()))
                .thenReturn(vlo_EnVersionAnterior);

        this.vgo_BllDesalmacenajeEspecial.InsertarDocumentos(
                "DE-2026-0001",
                ID_USUARIO,
                List.of(this.CrearArchivo("archivo.pdf")),
                null,
                null,
                null);

        ArgumentCaptor<EntDocumento> vlo_Captor = ArgumentCaptor.forClass(EntDocumento.class);
        verify(this.vgo_DalDocumento).SIMF_DOCUMENTO_InsertarRegistro(vlo_Captor.capture());
        EntDocumento vlo_EnNueva = vlo_Captor.getValue();

        assertEquals((short) 2, vlo_EnNueva.getVersion());
        assertEquals(55, vlo_EnNueva.getIdDocumentoAnterior());
        assertEquals(Constantes.ESTADO_DOCUMENTO_INACTIVO, vlo_EnVersionAnterior.getEstadoDocumento());
        verify(this.vgo_DalDocumento).SIMF_DOCUMENTO_ModificarRegistro(vlo_EnVersionAnterior);
    }

    @Test
    void InsertarDocumentos_SinArchivosAdjuntos_DebeLanzarExcepcion() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(anyString()))
                .thenReturn(this.CrearTramiteRegistrado());

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.InsertarDocumentos(
                        "DE-2026-0001", ID_USUARIO, List.of(), null, null, null));

        assertEquals(Constantes.MENSAJE_SIN_DOCUMENTOS_ADJUNTOS, vlo_Excepcion.getMessage());
        verify(this.vgo_BllBitacora, never()).RegistrarAccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void ObtenerExpediente_ConReferenciaInexistente_DebeLanzarExcepcion() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(anyString())).thenReturn(null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.ObtenerExpediente("DE-2026-9999"));

        assertEquals(Constantes.MENSAJE_EXPEDIENTE_NO_ENCONTRADO, vlo_Excepcion.getMessage());
    }

    @Test
    void ObtenerExpediente_DeUnTramiteRegistrado_DebeIndicarQueNoRequiereSicopNiOrdenCompra() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(anyString()))
                .thenReturn(this.CrearTramiteRegistrado());
        when(this.vgo_DalDesalmacenajeEspecial.SIMT_DESALMACENAJE_ESPEC_ObtenerRegistro(any()))
                .thenReturn(this.CrearDesalmacenaje());
        when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(any()))
                .thenReturn(this.CrearEstadoInicial());

        EntExpedienteDetalle vlo_Detalle = this.vgo_BllDesalmacenajeEspecial.ObtenerExpediente("DE-2026-0001");

        assertEquals("DE-2026-0001", vlo_Detalle.getReferenciaInterna());
        assertEquals("Pendiente", vlo_Detalle.getEstado());
        assertEquals("Oficina de Suministros", vlo_Detalle.getOrigenDependencia());
        assertEquals(Boolean.FALSE, vlo_Detalle.getRequiereSicop());
        assertEquals(Boolean.FALSE, vlo_Detalle.getRequiereOrdenCompra());
    }

    // ------------------------------------------------ HU-032: seguimiento del expediente

    @Test
    void ObtenerEstadosDisponibles_DebeDevolverLosEstadosCatalogadosDelTipoDeTramite() {
        when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorTipoTramite(ID_TIPO_TRAMITE, "ACT"))
                .thenReturn(List.of(this.CrearEstadoInicial(), this.CrearEstado(11, "En trámite", false)));

        assertEquals(2, this.vgo_BllDesalmacenajeEspecial.ObtenerEstadosDisponibles().size());
    }

    @Test
    void ActualizarEstado_ConJustificacion_DebeCambiarElEstadoYAnotarElMovimiento() {
        this.PrepararExpedienteParaSeguimiento();

        this.vgo_BllDesalmacenajeEspecial.ActualizarEstado("DE-2026-0001",
                this.CrearCambio(11, "Documentación recibida y en revisión"));

        ArgumentCaptor<EntTramite> vlo_CaptorTramite = ArgumentCaptor.forClass(EntTramite.class);
        verify(this.vgo_DalTramite).SIMT_TRAMITE_ModificarRegistro(vlo_CaptorTramite.capture());
        assertEquals(11, vlo_CaptorTramite.getValue().getIdEstadoTramite());

        ArgumentCaptor<EntHistorialEstado> vlo_CaptorHistorial = ArgumentCaptor.forClass(EntHistorialEstado.class);
        verify(this.vgo_DalHistorialEstado).SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_CaptorHistorial.capture());
        EntHistorialEstado vlo_EnHistorial = vlo_CaptorHistorial.getValue();
        assertEquals(ID_ESTADO_INICIAL, vlo_EnHistorial.getIdEstadoAnterior());
        assertEquals(11, vlo_EnHistorial.getIdEstadoNuevo());
        assertEquals(ID_USUARIO, vlo_EnHistorial.getIdUsuario());
        assertEquals("Documentación recibida y en revisión", vlo_EnHistorial.getJustificacion());
    }

    @Test
    void ActualizarEstado_ConJustificacion_DebeRegistrarElCambioEnLaBitacora() {
        this.PrepararExpedienteParaSeguimiento();

        this.vgo_BllDesalmacenajeEspecial.ActualizarEstado("DE-2026-0001",
                this.CrearCambio(11, "Documentación recibida y en revisión"));

        verify(this.vgo_BllBitacora).RegistrarAccion(
                ID_TRAMITE_GENERADO,
                ID_USUARIO,
                Constantes.ACCION_BITACORA_CAMBIO_ESTADO,
                Constantes.ENTIDAD_BITACORA_TRAMITE,
                ID_TRAMITE_GENERADO,
                "Estado: Pendiente.",
                "Estado: En trámite. Justificación: Documentación recibida y en revisión");
    }

    @Test
    void ActualizarEstado_SinJustificacion_DebeLanzarExcepcionYNoCambiarNada() {
        this.PrepararExpedienteParaSeguimiento();

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.ActualizarEstado("DE-2026-0001", this.CrearCambio(11, "   ")));

        assertEquals(Constantes.MENSAJE_JUSTIFICACION_CAMBIO_REQUERIDA, vlo_Excepcion.getMessage());
        verify(this.vgo_DalTramite, never()).SIMT_TRAMITE_ModificarRegistro(any());
        verify(this.vgo_DalHistorialEstado, never()).SIMH_ESTADO_TRAMITE_InsertarRegistro(any());
        verify(this.vgo_BllBitacora, never()).RegistrarAccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void ActualizarEstado_ConJustificacionDeNueveCaracteres_DebeLanzarExcepcion() {
        this.PrepararExpedienteParaSeguimiento();

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.ActualizarEstado("DE-2026-0001", this.CrearCambio(11, "a".repeat(9))));

        assertEquals(Constantes.MENSAJE_JUSTIFICACION_CAMBIO_REQUERIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void ActualizarEstado_ConJustificacionDe256Caracteres_DebeLanzarExcepcion() {
        this.PrepararExpedienteParaSeguimiento();

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.ActualizarEstado("DE-2026-0001", this.CrearCambio(11, "a".repeat(256))));

        assertEquals(Constantes.MENSAJE_JUSTIFICACION_CAMBIO_REQUERIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void ActualizarEstado_AlMismoEstado_DebeLanzarExcepcion() {
        this.PrepararExpedienteParaSeguimiento();

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.ActualizarEstado("DE-2026-0001",
                        this.CrearCambio(ID_ESTADO_INICIAL, "El expediente sigue igual que ayer")));

        assertEquals(Constantes.MENSAJE_ESTADO_SIN_CAMBIO, vlo_Excepcion.getMessage());
    }

    @Test
    void ActualizarEstado_ConEstadoDeOtroTipoDeTramite_DebeLanzarExcepcion() {
        this.PrepararExpedienteParaSeguimiento();
        EntEstadoTramite vlo_EnEstadoAjeno = this.CrearEstado(99, "Pendiente de donación", false);
        vlo_EnEstadoAjeno.setIdTipoTramite(77);
        when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(99)).thenReturn(vlo_EnEstadoAjeno);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.ActualizarEstado("DE-2026-0001",
                        this.CrearCambio(99, "Cambio a un estado que no corresponde")));

        assertEquals(Constantes.MENSAJE_ESTADO_NUEVO_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void ActualizarEstado_ConEstadoInexistente_DebeLanzarExcepcion() {
        this.PrepararExpedienteParaSeguimiento();
        when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(404)).thenReturn(null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.ActualizarEstado("DE-2026-0001",
                        this.CrearCambio(404, "Cambio a un estado inexistente")));

        assertEquals(Constantes.MENSAJE_ESTADO_NUEVO_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void ActualizarEstado_DeUnExpedienteInexistente_DebeLanzarExcepcion() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(anyString())).thenReturn(null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.ActualizarEstado("DE-2026-9999",
                        this.CrearCambio(11, "Cambio sobre un expediente que no existe")));

        assertEquals(Constantes.MENSAJE_EXPEDIENTE_NO_ENCONTRADO, vlo_Excepcion.getMessage());
    }

    @Test
    void ActualizarEstado_IndicandoElEstadoPorNombre_DebeResolverElEstadoDelCatalogo() {
        this.PrepararExpedienteParaSeguimiento();
        EntCambioEstado vlo_Cambio = new EntCambioEstado();
        vlo_Cambio.setNuevoEstado("en trámite");
        vlo_Cambio.setJustificacion("La aduana confirmó el ingreso del trámite");
        vlo_Cambio.setIdUsuarioResponsable(ID_USUARIO);

        this.vgo_BllDesalmacenajeEspecial.ActualizarEstado("DE-2026-0001", vlo_Cambio);

        ArgumentCaptor<EntHistorialEstado> vlo_Captor = ArgumentCaptor.forClass(EntHistorialEstado.class);
        verify(this.vgo_DalHistorialEstado).SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_Captor.capture());
        assertEquals(11, vlo_Captor.getValue().getIdEstadoNuevo());
    }

    @Test
    void ActualizarEstado_ConUnNombreDeEstadoQueNoExiste_DebeLanzarExcepcion() {
        this.PrepararExpedienteParaSeguimiento();
        EntCambioEstado vlo_Cambio = new EntCambioEstado();
        vlo_Cambio.setNuevoEstado("Extraviado en la aduana");
        vlo_Cambio.setJustificacion("Estado inventado que no está catalogado");
        vlo_Cambio.setIdUsuarioResponsable(ID_USUARIO);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.ActualizarEstado("DE-2026-0001", vlo_Cambio));

        assertEquals(Constantes.MENSAJE_ESTADO_NUEVO_INVALIDO, vlo_Excepcion.getMessage());
    }

    // ---------------------------------------------------------------- Estado documental (HU-045)

    @Test
    void GuardarEstadoDocumental_ComoIncompleto_DebeGuardarloConSusObservaciones() {
        this.PrepararExpedienteParaSeguimiento();
        EntDesalmacenajeEspecial vlo_EnDesalmacenaje = this.CrearDesalmacenaje();
        when(this.vgo_DalDesalmacenajeEspecial.SIMT_DESALMACENAJE_ESPEC_ObtenerRegistro(any()))
                .thenReturn(vlo_EnDesalmacenaje);

        EntExpedienteDetalle vlo_Detalle = this.vgo_BllDesalmacenajeEspecial.GuardarEstadoDocumental(
                "DE-2026-0001",
                this.CrearEstadoDocumental(Constantes.ESTADO_DOCUMENTAL_INCOMPLETO,
                        "  Falta la factura del proveedor.  "));

        assertEquals(Constantes.ESTADO_DOCUMENTAL_INCOMPLETO, vlo_EnDesalmacenaje.getEstadoDocumental());
        assertEquals("Falta la factura del proveedor.",
                vlo_EnDesalmacenaje.getObservacionesDocumentales());
        assertEquals(Constantes.ESTADO_DOCUMENTAL_INCOMPLETO, vlo_Detalle.getEstadoDocumental());
        verify(this.vgo_DalDesalmacenajeEspecial)
                .SIMT_DESALMACENAJE_ESPEC_ModificarRegistro(vlo_EnDesalmacenaje);
    }

    @Test
    void GuardarEstadoDocumental_ConDatosValidos_DebeAnotarloEnLaBitacora() {
        this.PrepararExpedienteParaSeguimiento();

        this.vgo_BllDesalmacenajeEspecial.GuardarEstadoDocumental(
                "DE-2026-0001",
                this.CrearEstadoDocumental(Constantes.ESTADO_DOCUMENTAL_COMPLETO, null));

        verify(this.vgo_BllBitacora).RegistrarAccion(
                eq(ID_TRAMITE_GENERADO), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_ESTADO_DOCUMENTAL),
                eq(Constantes.ENTIDAD_BITACORA_TRAMITE),
                eq(ID_TRAMITE_GENERADO), anyString(), anyString());
    }

    @Test
    void GuardarEstadoDocumental_ConUnValorQueNoEsCompletoNiIncompleto_DebeLanzarExcepcion() {
        this.PrepararExpedienteParaSeguimiento();

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.GuardarEstadoDocumental(
                        "DE-2026-0001", this.CrearEstadoDocumental("MAS O MENOS", null)));

        assertEquals(Constantes.MENSAJE_ESTADO_DOCUMENTAL_INVALIDO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalDesalmacenajeEspecial, never())
                .SIMT_DESALMACENAJE_ESPEC_ModificarRegistro(any());
    }

    @Test
    void GuardarEstadoDocumental_ConObservacionesDe1001Caracteres_DebeLanzarExcepcion() {
        this.PrepararExpedienteParaSeguimiento();

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.GuardarEstadoDocumental(
                        "DE-2026-0001",
                        this.CrearEstadoDocumental(Constantes.ESTADO_DOCUMENTAL_COMPLETO,
                                "a".repeat(1001))));

        assertEquals(Constantes.MENSAJE_OBSERVACIONES_DOCUMENTALES_INVALIDAS, vlo_Excepcion.getMessage());
    }

    @Test
    void ClasificarDocumento_DeUnExpedienteRegistrado_DebeDelegarloYDevolverElExpediente() {
        this.PrepararExpedienteParaSeguimiento();

        EntExpedienteDetalle vlo_Detalle = this.vgo_BllDesalmacenajeEspecial.ClasificarDocumento(
                "DE-2026-0001", 55, "FACTURA", ID_USUARIO);

        verify(this.vgo_BllDocumento).ClasificarDocumento(ID_TRAMITE_GENERADO, 55, "FACTURA", ID_USUARIO);
        assertEquals("DE-2026-0001", vlo_Detalle.getReferenciaInterna());
    }

    private EntEstadoDocumental CrearEstadoDocumental(String pvc_Estado, String pvc_Observaciones) {
        EntEstadoDocumental vlo_Estado = new EntEstadoDocumental();
        vlo_Estado.setEstadoDocumental(pvc_Estado);
        vlo_Estado.setObservaciones(pvc_Observaciones);
        vlo_Estado.setIdUsuarioResponsable(ID_USUARIO);
        return vlo_Estado;
    }

    // ---------------------------------------------------------------- Correccion de datos (HU-032)

    @Test
    void ModificarDatos_ConUnaDependenciaDistinta_DebeGuardarlaYAnotarElValorAnterior() {
        this.PrepararExpedienteParaSeguimiento();
        EntDesalmacenajeEspecial vlo_EnDesalmacenaje = this.CrearDesalmacenaje();
        when(this.vgo_DalDesalmacenajeEspecial.SIMT_DESALMACENAJE_ESPEC_ObtenerRegistro(any()))
                .thenReturn(vlo_EnDesalmacenaje);

        EntCorreccionDesalmacenaje vlo_Correccion = this.CrearCorreccion();
        vlo_Correccion.setOrigenDependencia("Escuela de Física");

        this.vgo_BllDesalmacenajeEspecial.ModificarDatos("DE-2026-0001", vlo_Correccion);

        assertEquals("Escuela de Física", vlo_EnDesalmacenaje.getOrigenDependencia());
        verify(this.vgo_DalDesalmacenajeEspecial)
                .SIMT_DESALMACENAJE_ESPEC_ModificarRegistro(vlo_EnDesalmacenaje);
        verify(this.vgo_BllBitacora).RegistrarAccion(eq(ID_TRAMITE_GENERADO), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_DATOS_MODIFICADOS),
                eq(Constantes.ENTIDAD_BITACORA_TRAMITE), eq(ID_TRAMITE_GENERADO),
                anyString(), anyString());
    }

    @Test
    void ModificarDatos_ConUnRegimenAduanero_DebeGuardarloEnElTramite() {
        this.PrepararExpedienteParaSeguimiento();
        EntTramite vlo_EnTramite = this.CrearTramiteRegistrado();
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(anyString()))
                .thenReturn(vlo_EnTramite);

        EntCorreccionDesalmacenaje vlo_Correccion = this.CrearCorreccion();
        vlo_Correccion.setRegimenAduanero("Importación definitiva");

        this.vgo_BllDesalmacenajeEspecial.ModificarDatos("DE-2026-0001", vlo_Correccion);

        assertEquals("Importación definitiva", vlo_EnTramite.getRegimenAduanero());
        verify(this.vgo_DalTramite).SIMT_TRAMITE_ModificarRegistro(vlo_EnTramite);
    }

    @Test
    void ModificarDatos_ConUnValorEstimadoNegativo_DebeLanzarExcepcion() {
        this.PrepararExpedienteParaSeguimiento();
        EntCorreccionDesalmacenaje vlo_Correccion = this.CrearCorreccion();
        vlo_Correccion.setValorEstimado(new BigDecimal("-5.00"));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.ModificarDatos("DE-2026-0001", vlo_Correccion));

        assertEquals(Constantes.MENSAJE_VALOR_ESTIMADO_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void ModificarDatos_SinNingunCambio_DebeLanzarExcepcion() {
        this.PrepararExpedienteParaSeguimiento();

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDesalmacenajeEspecial.ModificarDatos(
                        "DE-2026-0001", this.CrearCorreccion()));

        assertEquals(Constantes.MENSAJE_DATOS_DESALMACENAJE_SIN_CAMBIO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalTramite, never()).SIMT_TRAMITE_ModificarRegistro(any());
    }

    private EntCorreccionDesalmacenaje CrearCorreccion() {
        EntCorreccionDesalmacenaje vlo_Correccion = new EntCorreccionDesalmacenaje();
        vlo_Correccion.setIdUsuarioResponsable(ID_USUARIO);
        return vlo_Correccion;
    }

    private void PrepararExpedienteParaSeguimiento() {
        lenient().when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(anyString()))
                .thenReturn(this.CrearTramiteRegistrado());
        lenient().when(this.vgo_DalDesalmacenajeEspecial.SIMT_DESALMACENAJE_ESPEC_ObtenerRegistro(any()))
                .thenReturn(this.CrearDesalmacenaje());
        lenient().when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(ID_ESTADO_INICIAL))
                .thenReturn(this.CrearEstadoInicial());
        lenient().when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(11))
                .thenReturn(this.CrearEstado(11, "En trámite", false));
        lenient().when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(any(), anyString(), anyString()))
                .thenReturn(null);
        lenient().when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(ID_TIPO_TRAMITE, "en trámite", "ACT"))
                .thenReturn(this.CrearEstado(11, "En trámite", false));
    }

    private EntCambioEstado CrearCambio(Integer pvn_IdEstadoNuevo, String pvc_Justificacion) {
        EntCambioEstado vlo_Cambio = new EntCambioEstado();
        vlo_Cambio.setIdEstadoNuevo(pvn_IdEstadoNuevo);
        vlo_Cambio.setJustificacion(pvc_Justificacion);
        vlo_Cambio.setIdUsuarioResponsable(ID_USUARIO);
        return vlo_Cambio;
    }

    private EntEstadoTramite CrearEstado(Integer pvn_Id, String pvc_Nombre, boolean pvb_EsFinal) {
        EntEstadoTramite vlo_EnEstado = new EntEstadoTramite();
        vlo_EnEstado.setIdEstadoTramite(pvn_Id);
        vlo_EnEstado.setIdTipoTramite(ID_TIPO_TRAMITE);
        vlo_EnEstado.setNombre(pvc_Nombre);
        vlo_EnEstado.setEsInicial(Boolean.FALSE);
        vlo_EnEstado.setEsFinal(pvb_EsFinal);
        vlo_EnEstado.setEstado("ACT");
        return vlo_EnEstado;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Deja los catálogos y el almacenamiento en su comportamiento correcto,
     *   de manera que cada prueba solo altere la condición que desea comprobar.
     */
    private void PrepararCatalogosValidos() {
        lenient().when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerPorCodigo(anyString()))
                .thenReturn(this.CrearTipoTramite());
        lenient().when(this.vgo_DalTramite.SIMT_TRAMITE_ExistePorReferenciaInterna(anyString())).thenReturn(false);
        lenient().when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(any())).thenReturn(this.CrearUsuario());
        lenient().when(this.vgo_DalMoneda.SIMM_MONEDA_ObtenerPorCodigoIso(anyString())).thenReturn(this.CrearMoneda());
        lenient().when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerEstadoInicial(any()))
                .thenReturn(this.CrearEstadoInicial());
        lenient().when(this.vgo_DalTipoDocumento.SIMM_TIPO_DOCUMENTO_ObtenerPorCodigo(anyString()))
                .thenReturn(this.CrearTipoDocumento());
        lenient().when(this.vgo_AlmacenamientoDocumentos.GuardarArchivo(any(), anyString()))
                .thenReturn("DE-2026-0001/archivo-almacenado");
        lenient().when(this.vgo_AlmacenamientoDocumentos.DepurarNombreArchivo(any())).thenReturn("archivo.pdf");
        lenient().when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerVersionVigente(any(), any(), anyString(), anyString()))
                .thenReturn(null);
        lenient().when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerListaExpediente(any())).thenReturn(List.of());
        lenient().when(this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_ObtenerPorTramite(any())).thenReturn(List.of());

        lenient().doAnswer(pvo_Invocacion -> {
            EntTramite vlo_EnTramite = pvo_Invocacion.getArgument(0);
            vlo_EnTramite.setIdTramite(ID_TRAMITE_GENERADO);
            return null;
        }).when(this.vgo_DalTramite).SIMT_TRAMITE_InsertarRegistro(any());
    }

    private EntRegistroDesalmacenaje CrearRegistroValido() {
        EntRegistroDesalmacenaje vlo_Registro = new EntRegistroDesalmacenaje();
        vlo_Registro.setReferenciaInterna("DE-2026-0001");
        vlo_Registro.setOrigenDependencia("Oficina de Suministros");
        vlo_Registro.setPaisOrigen(null);
        vlo_Registro.setMoneda("CRC");
        vlo_Registro.setValorEstimado(new BigDecimal("1500.00"));
        vlo_Registro.setJustificacion(
                "Mercancía propiedad de la institución que requiere ser desalmacenada.");
        vlo_Registro.setIdUsuarioResponsable(ID_USUARIO);
        return vlo_Registro;
    }

    private EntTramite CrearTramiteRegistrado() {
        EntTramite vlo_EnTramite = new EntTramite();
        vlo_EnTramite.setIdTramite(ID_TRAMITE_GENERADO);
        vlo_EnTramite.setReferenciaInterna("DE-2026-0001");
        vlo_EnTramite.setIdTipoTramite(ID_TIPO_TRAMITE);
        vlo_EnTramite.setIdEstadoTramite(ID_ESTADO_INICIAL);
        vlo_EnTramite.setIdUsuarioResponsable(ID_USUARIO);
        vlo_EnTramite.setIdMoneda(1);
        vlo_EnTramite.setValorEstimado(new BigDecimal("1500.00"));
        return vlo_EnTramite;
    }

    private EntDesalmacenajeEspecial CrearDesalmacenaje() {
        EntDesalmacenajeEspecial vlo_EnDesalmacenaje = new EntDesalmacenajeEspecial();
        vlo_EnDesalmacenaje.setIdTramite(ID_TRAMITE_GENERADO);
        vlo_EnDesalmacenaje.setOrigenDependencia("Oficina de Suministros");
        vlo_EnDesalmacenaje.setJustificacion("Mercancía propiedad de la institución.");
        return vlo_EnDesalmacenaje;
    }

    private EntTipoTramite CrearTipoTramite() {
        EntTipoTramite vlo_EnTipoTramite = new EntTipoTramite();
        vlo_EnTipoTramite.setIdTipoTramite(ID_TIPO_TRAMITE);
        vlo_EnTipoTramite.setCodigo(Constantes.CODIGO_TIPO_TRAMITE_DESALMACENAJE);
        vlo_EnTipoTramite.setPrefijoReferencia("DE-");
        vlo_EnTipoTramite.setRequiereSicop(Boolean.FALSE);
        vlo_EnTipoTramite.setRequiereOrdenCompra(Boolean.FALSE);
        return vlo_EnTipoTramite;
    }

    private EntEstadoTramite CrearEstadoInicial() {
        EntEstadoTramite vlo_EnEstado = new EntEstadoTramite();
        vlo_EnEstado.setIdEstadoTramite(ID_ESTADO_INICIAL);
        vlo_EnEstado.setIdTipoTramite(ID_TIPO_TRAMITE);
        vlo_EnEstado.setNombre("Pendiente");
        vlo_EnEstado.setEsInicial(Boolean.TRUE);
        vlo_EnEstado.setEstado("ACT");
        return vlo_EnEstado;
    }

    private EntUsuario CrearUsuario() {
        EntUsuario vlo_EnUsuario = new EntUsuario();
        vlo_EnUsuario.setIdUsuario(ID_USUARIO);
        vlo_EnUsuario.setNombreCompleto("Katherine Valverde");
        return vlo_EnUsuario;
    }

    private EntMoneda CrearMoneda() {
        EntMoneda vlo_EnMoneda = new EntMoneda();
        vlo_EnMoneda.setIdMoneda(1);
        vlo_EnMoneda.setCodigoIso("CRC");
        return vlo_EnMoneda;
    }

    private EntTipoDocumento CrearTipoDocumento() {
        EntTipoDocumento vlo_EnTipoDocumento = new EntTipoDocumento();
        vlo_EnTipoDocumento.setIdTipoDocumento(1);
        vlo_EnTipoDocumento.setCodigo(Constantes.CODIGO_TIPO_DOCUMENTO_RESPALDO);
        return vlo_EnTipoDocumento;
    }

    private MultipartFile CrearArchivo(String pvc_NombreArchivo) {
        return new MockMultipartFile(
                "documentosRespaldo",
                pvc_NombreArchivo,
                "application/octet-stream",
                "contenido de prueba".getBytes());
    }
}
