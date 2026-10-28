package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalMoneda;
import cr.ac.ucr.sim.accesodatos.tramites.DalPedidoImportacion;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntNotificacionPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTransferenciaPedidoImportacion;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BllPedidoImportacionTest {

    @Mock
    private DalTramite vgo_DalTramite;

    @Mock
    private DalPedidoImportacion vgo_DalPedidoImportacion;

    @Mock
    private DalTipoTramite vgo_DalTipoTramite;

    @Mock
    private DalEstadoTramite vgo_DalEstadoTramite;

    @Mock
    private DalHistorialEstado vgo_DalHistorialEstado;

    @Mock
    private DalMoneda vgo_DalMoneda;

    @InjectMocks
    private BllPedidoImportacion vgo_BllPedidoImportacion;

    private EntTipoTramite vgo_EnTipo;
    private EntEstadoTramite vgo_EnEstado;

    @BeforeEach
    void PrepararCatalogos() {
        this.vgo_EnTipo = new EntTipoTramite();
        this.vgo_EnTipo.setIdTipoTramite(7);
        this.vgo_EnTipo.setCodigo("PEDIDO_PROV");
        this.vgo_EnEstado = new EntEstadoTramite();
        this.vgo_EnEstado.setIdEstadoTramite(13);
        this.vgo_EnEstado.setNombre("En trámite");

        lenient().when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerPorCodigo("PEDIDO_PROV"))
                .thenReturn(this.vgo_EnTipo);
        lenient().when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(7, "En trámite", "ACT"))
                .thenReturn(this.vgo_EnEstado);
        lenient().doAnswer(invocacion -> {
            invocacion.<cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite>getArgument(0).setIdTramite(99);
            return null;
        }).when(this.vgo_DalTramite).SIMT_TRAMITE_InsertarRegistro(any());
    }

    @Test
    void InsertarRegistro_ConDatosValidos_CreaExpedienteYDetalle() {
        int ano = Year.now().getValue();
        EntRespuestaPedidoImportacion respuesta = this.vgo_BllPedidoImportacion.InsertarRegistro(
                this.CrearRegistro(ano, ano + "-001", 5),
                23,
                "Importador de prueba");

        assertEquals(99, respuesta.idTramite());
        assertEquals(ano + "-001", respuesta.referenciaInterna());
        assertEquals("En trámite", respuesta.estado());
        assertEquals("Importador de prueba", respuesta.usuarioResponsable());
        verify(this.vgo_DalPedidoImportacion).SIMT_PEDIDO_PROV_InsertarRegistro(any());
        verify(this.vgo_DalHistorialEstado).SIMH_ESTADO_TRAMITE_InsertarRegistro(any());
        verify(this.vgo_DalPedidoImportacion).SIMM_OPCION_IMPORTACION_GuardarSiNoExiste("UNIDAD", "Unidad A");
    }

    @Test
    void InsertarRegistro_ConReferenciaDeOtroAno_RechazaLaSolicitud() {
        int ano = Year.now().getValue();
        ExcepcionNegocio problema = assertThrows(
                ExcepcionNegocio.class,
                () -> this.vgo_BllPedidoImportacion.InsertarRegistro(
                        this.CrearRegistro(ano, (ano - 1) + "-001", 5),
                        23,
                        "Importador de prueba"));

        assertEquals("El número de solicitud debe usar el año actual y el formato AAAA-NNN.", problema.getMessage());
        verify(this.vgo_DalTramite, never()).SIMT_TRAMITE_InsertarRegistro(any());
    }

    @Test
    void InsertarRegistro_ConPartidaNoAdmitida_RechazaLaSolicitud() {
        int ano = Year.now().getValue();
        assertThrows(
                ExcepcionNegocio.class,
                () -> this.vgo_BllPedidoImportacion.InsertarRegistro(
                        this.CrearRegistro(ano, ano + "-001", 3),
                        23,
                        "Importador de prueba"));
        verify(this.vgo_DalTipoTramite, never()).SIMM_TIPO_TRAMITE_ObtenerPorCodigo("PEDIDO_PROV");
    }

    @Test
    void ModificarRegistro_ConCambioDeEstado_ActualizaDetalleYGuardaHistorial() {
        int ano = Year.now().getValue();
        String referencia = ano + "-001";
        EntRegistroPedidoImportacion registro = this.CrearRegistro(ano, referencia, 5, "En aduana");
        EntTramite tramite = new EntTramite();
        tramite.setIdTramite(99);
        tramite.setReferenciaInterna(referencia);
        tramite.setIdTipoTramite(7);
        tramite.setIdEstadoTramite(13);
        tramite.setIdUsuarioResponsable(23);
        EntEstadoTramite estadoNuevo = new EntEstadoTramite();
        estadoNuevo.setIdEstadoTramite(14);
        estadoNuevo.setNombre("En aduana");

        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(referencia)).thenReturn(tramite);
        when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerRegistro(7)).thenReturn(this.vgo_EnTipo);
        when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(13)).thenReturn(this.vgo_EnEstado);
        when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(7, "En aduana", "ACT"))
                .thenReturn(estadoNuevo);
        when(this.vgo_DalPedidoImportacion.SIMT_PEDIDO_PROV_ObtenerPorReferenciaYUsuario(referencia, 23))
                .thenReturn(new EntPedidoImportacion(99, registro));

        EntRespuestaPedidoImportacion respuesta = this.vgo_BllPedidoImportacion.ModificarRegistro(
                referencia,
                registro,
                23,
                "Importador de prueba");

        assertEquals("En aduana", respuesta.estado());
        verify(this.vgo_DalTramite).SIMT_TRAMITE_ModificarRegistro(tramite);
        verify(this.vgo_DalPedidoImportacion).SIMT_PEDIDO_PROV_ModificarRegistro(any());
        verify(this.vgo_DalHistorialEstado).SIMH_ESTADO_TRAMITE_InsertarRegistro(any());
    }

    @Test
    void ObtenerNotificaciones_AlCumplirTresDias_DevuelveAlertaDePago() {
        LocalDate transferencia = LocalDate.now().minusDays(3);
        when(this.vgo_DalPedidoImportacion.SIMT_PEDIDO_PROV_ObtenerTransferenciasAnterioresA(
                23,
                LocalDate.now().minusDays(3)))
                .thenReturn(List.of(new EntTransferenciaPedidoImportacion("2026-001", transferencia)));

        List<EntNotificacionPedidoImportacion> notificaciones =
                this.vgo_BllPedidoImportacion.ObtenerNotificaciones(23);

        assertEquals(1, notificaciones.size());
        assertEquals(3, notificaciones.get(0).diasCalendarioPago());
        assertEquals("2026-001", notificaciones.get(0).referenciaInterna());
    }

    private EntRegistroPedidoImportacion CrearRegistro(int pvn_Ano, String pvc_Referencia, Integer pvn_Partida) {
        return this.CrearRegistro(pvn_Ano, pvc_Referencia, pvn_Partida, "En trámite");
    }

    private EntRegistroPedidoImportacion CrearRegistro(
            int pvn_Ano,
            String pvc_Referencia,
            Integer pvn_Partida,
            String pvc_Estado) {
        return new EntRegistroPedidoImportacion(
                pvc_Referencia,
                new BigDecimal("12.5"),
                LocalDate.now(),
                "Unidad A",
                "Descripción de prueba.",
                pvn_Ano + "-CFT-000002 UADQ-INT",
                "001-" + pvn_Ano,
                "Compañía A",
                pvn_Ano + "-001",
                "FOB",
                new BigDecimal("50000000.00"),
                new BigDecimal("1200.00"),
                "CRC",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "F-001",
                1,
                pvn_Partida,
                null,
                null,
                null,
                pvc_Estado,
                null);
    }
}
