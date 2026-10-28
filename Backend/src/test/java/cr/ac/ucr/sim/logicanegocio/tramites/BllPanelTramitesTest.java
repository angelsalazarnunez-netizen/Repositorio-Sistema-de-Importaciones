package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.tramites.DalPanelTramites;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntFilaPanelTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.modelo.tramites.ModeloPanelTramitesRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.tramites.ModeloTramiteActivoRespuesta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Pruebas de las reglas del panel de trámites activos (HU-030).
 */
@ExtendWith(MockitoExtension.class)
class BllPanelTramitesTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 5);

    @Mock
    private DalPanelTramites vgo_DalPanelTramites;

    @Mock
    private BllAlertaTemporal vgo_BllAlertaTemporal;

    @InjectMocks
    private BllPanelTramites vgo_BllPanelTramites;

    @BeforeEach
    void Preparar() {
        lenient().when(this.vgo_BllAlertaTemporal.Hoy()).thenReturn(HOY);
    }

    @Test
    void ObtenerPanel_CalculaIndicadoresYOrdenaPorUrgencia() {
        when(this.vgo_DalPanelTramites.SIMT_TRAMITE_ObtenerListaActivos()).thenReturn(List.of(
                this.Fila(1, "PEDIDO_PROV", "Pendiente", "PEN", null, null, null, null),
                this.Fila(2, "IMPORT_TEMPORAL", "Activo en el país", "ACT", "EN_PROCESO", HOY.plusDays(181), null, null),
                this.Fila(3, "IMPORT_TEMPORAL", "Activo en el país", "ACT", "EN_PROCESO", HOY.plusDays(6), null, null),
                this.Fila(4, "DONACION", "Pendiente de permisos", "ACT", "PERMISOS", null, null, null),
                this.Fila(5, "EXONERACION", "Pendiente de aprobación", "ACT", "APROBACION", null, null, null),
                this.Fila(6, "DONACION", "Pendiente de exoneración", "ACT", "EXONERACION", null, null, null),
                this.Fila(7, "EXPORT_TEMPORAL", "Vencido", "ACT", "EN_PROCESO", HOY.minusDays(2), null, null)));

        ModeloPanelTramitesRespuesta vlo_Panel = this.vgo_BllPanelTramites.ObtenerPanel(null);

        assertEquals(Constantes.ALCANCE_PANEL_ACTIVOS, vlo_Panel.getAlcance());
        assertEquals(7, vlo_Panel.getTotalActivos());
        assertEquals(1, vlo_Panel.getTotalPorLlegar());
        assertEquals(6, vlo_Panel.getTotalEnProceso());
        assertEquals(1, vlo_Panel.getTotalPermisos());
        assertEquals(1, vlo_Panel.getTotalAprobacion());
        assertEquals(1, vlo_Panel.getTotalExoneracion());
        assertEquals(2, vlo_Panel.getTotalProximosVencer());
        // Primero el vencido, luego el próximo a vencer; el resto por fecha de registro.
        assertEquals(List.of(7, 3, 1, 2, 4, 5, 6),
                vlo_Panel.getTramites().stream().map(ModeloTramiteActivoRespuesta::getIdTramite).toList());
        verify(this.vgo_DalPanelTramites, never()).SIMT_TRAMITE_ObtenerListaHistorico(anyInt());
    }

    @Test
    void ConvertirFila_TemporalCalculaDiasRestantesConLaReglaDeLaHU027() {
        ModeloTramiteActivoRespuesta vlo_Proximo = this.vgo_BllPanelTramites.ConvertirFila(
                this.Fila(1, "IMPORT_TEMPORAL", "Activo en el país", "ACT", "EN_PROCESO", HOY.plusDays(45), null, null), HOY);
        ModeloTramiteActivoRespuesta vlo_EnPlazo = this.vgo_BllPanelTramites.ConvertirFila(
                this.Fila(2, "IMPORT_TEMPORAL", "Activo en el país", "ACT", "EN_PROCESO", HOY.plusDays(46), null, null), HOY);
        ModeloTramiteActivoRespuesta vlo_SinFecha = this.vgo_BllPanelTramites.ConvertirFila(
                this.Fila(3, "IMPORT_TEMPORAL", "Por llegar", "PEN", "POR_LLEGAR", null, null, null), HOY);

        assertEquals(45L, vlo_Proximo.getDiasRestantes());
        assertEquals(Constantes.VENCIMIENTO_PROXIMO, vlo_Proximo.getNivelVencimiento());
        assertEquals(Constantes.VENCIMIENTO_EN_PLAZO, vlo_EnPlazo.getNivelVencimiento());
        assertEquals(Constantes.VENCIMIENTO_SIN_FECHA, vlo_SinFecha.getNivelVencimiento());
        assertNull(vlo_SinFecha.getDiasRestantes());
    }

    @Test
    void ConvertirFila_NoTemporalNoTieneVencimientoNiDua() {
        ModeloTramiteActivoRespuesta vlo_Tramite = this.vgo_BllPanelTramites.ConvertirFila(
                this.Fila(1, "DONACION", "En aduana", "ACT", "EN_PROCESO", null, null, null), HOY);

        assertNull(vlo_Tramite.getDiasRestantes());
        assertNull(vlo_Tramite.getNivelVencimiento());
        assertNull(vlo_Tramite.getNumeroDua());
        assertEquals("Donación Fundación UCR", vlo_Tramite.getMotivo());
    }

    @Test
    void ResolverGrupo_EstadoSinClasificarSeUbicaPorSuCategoria() {
        assertEquals(Constantes.GRUPO_PANEL_POR_LLEGAR, this.vgo_BllPanelTramites.ResolverGrupo(
                this.Fila(1, "PEDIDO_PROV", "Pendiente", "PEN", null, null, null, null)));
        assertEquals(Constantes.GRUPO_PANEL_EN_PROCESO, this.vgo_BllPanelTramites.ResolverGrupo(
                this.Fila(2, "DESALM_ESPECIAL", "En aduana", "ACT", null, null, null, null)));
    }

    @Test
    void ResolverGrupo_PedidoQueIngresoAlPaisDejaDeEstarPorLlegarYAlDesalmacenarseSale() {
        assertEquals(Constantes.GRUPO_PANEL_EN_PROCESO, this.vgo_BllPanelTramites.ResolverGrupo(
                this.Fila(1, "PEDIDO_PROV", "Pendiente", "PEN", "POR_LLEGAR", null, HOY.minusDays(3), null)));
        assertEquals(Constantes.GRUPO_PANEL_FUERA, this.vgo_BllPanelTramites.ResolverGrupo(
                this.Fila(2, "PEDIDO_PROV", "Pendiente", "PEN", "POR_LLEGAR", null, HOY.minusDays(3), HOY)));
    }

    @Test
    void ResolverGrupo_EstadoFinalODesalmacenadoQuedaFuera() {
        assertEquals(Constantes.GRUPO_PANEL_FUERA, this.vgo_BllPanelTramites.ResolverGrupo(
                this.Fila(1, "DONACION", "Desalmacenaje", "ACT", "FUERA", null, null, null)));
        assertEquals(Constantes.GRUPO_PANEL_FUERA, this.vgo_BllPanelTramites.ResolverGrupo(
                this.Fila(2, "DESALM_ESPECIAL", "Finalizada", "FIN", null, null, null, null)));
    }

    @Test
    void ObtenerPanel_HistoricoDevuelveLosQueSalieronSinPerderLosIndicadores() {
        when(this.vgo_DalPanelTramites.SIMT_TRAMITE_ObtenerListaActivos()).thenReturn(List.of(
                this.Fila(1, "DONACION", "Pendiente de permisos", "ACT", "PERMISOS", null, null, null)));
        when(this.vgo_DalPanelTramites.SIMT_TRAMITE_ObtenerListaHistorico(Constantes.LIMITE_HISTORICO_PANEL))
                .thenReturn(List.of(this.Fila(9, "DONACION", "Finalizada", "FIN", "FUERA", null, null, null)));

        ModeloPanelTramitesRespuesta vlo_Panel = this.vgo_BllPanelTramites.ObtenerPanel("historico");

        assertEquals(Constantes.ALCANCE_PANEL_HISTORICO, vlo_Panel.getAlcance());
        assertEquals(1, vlo_Panel.getTotalPermisos());
        assertEquals(1, vlo_Panel.getTramites().size());
        assertEquals(Constantes.GRUPO_PANEL_FUERA, vlo_Panel.getTramites().get(0).getGrupoPanel());
    }

    @Test
    void ObtenerPanel_AlcanceInvalido_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllPanelTramites.ObtenerPanel("TODOS"));

        assertEquals(Constantes.MENSAJE_ALCANCE_PANEL_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void ResolverMotivo_UsaLosDatosPropiosDeCadaTipo() {
        assertEquals("Microscopio para exhibición", this.vgo_BllPanelTramites.ResolverMotivo(
                this.Fila(1, "IMPORT_TEMPORAL", "Activo en el país", "ACT", null, null, null, null)));
        assertEquals("Exoneración · Compra institucional", this.vgo_BllPanelTramites.ResolverMotivo(
                this.Fila(2, "EXONERACION", "Pendiente de aprobación", "ACT", null, null, null, null)));
        assertEquals("Desalmacenaje especial · Escuela de Química", this.vgo_BllPanelTramites.ResolverMotivo(
                this.Fila(3, "DESALM_ESPECIAL", "En aduana", "ACT", null, null, null, null)));
    }

    private EntFilaPanelTramite Fila(
            int pvn_Id,
            String pvc_Codigo,
            String pvc_Estado,
            String pvc_Categoria,
            String pvc_Grupo,
            LocalDate pvd_Vencimiento,
            LocalDate pvd_IngresoPais,
            LocalDate pvd_Desalmacenaje) {
        return new EntFilaPanelTramite(
                pvn_Id, "REF-" + pvn_Id, pvc_Codigo, pvc_Codigo, pvc_Estado, pvc_Categoria, pvc_Grupo,
                "FIN".equals(pvc_Categoria), "Microscopio para exhibición", new BigDecimal("1000.00"), "USD",
                "005-2026-" + pvn_Id, pvd_Vencimiento, "FUNDACION_UCR", "COMPRA_INSTITUCIONAL",
                "Escuela de Química", null, null, null, pvd_IngresoPais, pvd_Desalmacenaje,
                LocalDateTime.of(2026, 9, pvn_Id, 8, 0), "Analista");
    }
}
