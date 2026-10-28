package cr.ac.ucr.sim.reportes.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntBloqueReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntLineaReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntReporteConsultas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 12/10/2026
 * Descripción: Pruebas de la base de los archivos descargables del reporte de consultas internas
 *   (HU-036). Verifican que las tablas del documento se armen con las mismas columnas, los mismos
 *   valores y los mismos totales por moneda que muestra la pantalla, porque de esa base dependen
 *   por igual el Excel y el PDF.
 */
class Rpt_SIM_ReporteConsultasTest {

    private Rpt_SIM_ReporteConsultas vgo_Base;

    @BeforeEach
    void PrepararPrueba() {
        this.vgo_Base = new BaseDePrueba();
    }

    @Test
    void MuestraMoneda_ConUnaSolaMoneda_DebeOcultarLaColumna() {
        EntReporteConsultas vlo_Reporte = this.CrearReporte(
                this.CrearBloque("Desglose por tipo de trámite", List.of(
                        this.CrearLinea("Desalmacenaje especial", "CRC", 2),
                        this.CrearLinea("Solicitud de exoneración", "CRC", 1))));

        assertFalse(this.vgo_Base.MuestraMoneda(vlo_Reporte));
    }

    @Test
    void MuestraMoneda_ConMonedasDistintas_DebeMostrarLaColumna() {
        EntReporteConsultas vlo_Reporte = this.CrearReporte(
                this.CrearBloque("Desglose por tipo de trámite", List.of(
                        this.CrearLinea("Desalmacenaje especial", "CRC", 2),
                        this.CrearLinea("Desalmacenaje especial", "USD", 1))));

        assertTrue(this.vgo_Base.MuestraMoneda(vlo_Reporte));
    }

    @Test
    void MuestraMoneda_ConUnReporteSinLineas_DebeOcultarLaColumna() {
        EntReporteConsultas vlo_Reporte = this.CrearReporte(
                this.CrearBloque("Desglose por tipo de trámite", List.of()));

        assertFalse(this.vgo_Base.MuestraMoneda(vlo_Reporte));
    }

    @Test
    void ColumnasDeGrupo_ConUnSoloCriterio_DebeUsarElNombreDeEseCriterio() {
        EntBloqueReporte vlo_Bloque = this.CrearBloque("Desglose por tipo de trámite", List.of());
        vlo_Bloque.setEncabezados(List.of("Tipo de trámite"));

        assertEquals(List.of("Tipo de trámite"), this.vgo_Base.ColumnasDeGrupo(vlo_Bloque));
    }

    @Test
    void ColumnasDeGrupo_ConCriteriosCombinados_DebeUsarUnaColumnaPorCriterio() {
        EntBloqueReporte vlo_Bloque = this.CrearBloque("Desglose combinado", List.of());
        vlo_Bloque.setEncabezados(List.of("Tipo de trámite", "Régimen aduanero"));

        assertEquals(List.of("Tipo de trámite", "Régimen aduanero"),
                this.vgo_Base.ColumnasDeGrupo(vlo_Bloque));
    }

    @Test
    void ColumnasDeGrupo_SinEncabezados_DebeUsarLaColumnaGenerica() {
        EntBloqueReporte vlo_Bloque = this.CrearBloque("Desglose por tipo de trámite", List.of());

        assertEquals(List.of("Grupo"), this.vgo_Base.ColumnasDeGrupo(vlo_Bloque));
    }

    @Test
    void ColumnasDeTotales_SinLosTotalesOpcionales_DebeTraerSoloLasColumnasFijas() {
        EntReporteConsultas vlo_Reporte = this.CrearReporte(this.CrearBloque("Desglose", List.of()));
        vlo_Reporte.setIncluirTotalCif(false);
        vlo_Reporte.setIncluirTotalBodegaje(false);

        assertEquals(List.of("Trámites", "Impuestos pagados", "Exonerados", "Permisos"),
                this.TitulosDeTotales(vlo_Reporte));
    }

    @Test
    void ColumnasDeTotales_PidiendoElTotalCif_DebeIncluirloDespuesDeLosTramites() {
        EntReporteConsultas vlo_Reporte = this.CrearReporte(this.CrearBloque("Desglose", List.of()));
        vlo_Reporte.setIncluirTotalCif(true);
        vlo_Reporte.setIncluirTotalBodegaje(false);

        assertEquals(List.of("Trámites", "Total CIF", "Impuestos pagados", "Exonerados", "Permisos"),
                this.TitulosDeTotales(vlo_Reporte));
    }

    @Test
    void ColumnasDeTotales_PidiendoLosBodegajes_DebeIncluirlosAntesDeLosPermisos() {
        EntReporteConsultas vlo_Reporte = this.CrearReporte(this.CrearBloque("Desglose", List.of()));
        vlo_Reporte.setIncluirTotalCif(false);
        vlo_Reporte.setIncluirTotalBodegaje(true);

        assertEquals(List.of("Trámites", "Impuestos pagados", "Exonerados", "Bodegajes (₡)", "Permisos"),
                this.TitulosDeTotales(vlo_Reporte));
    }

    @Test
    void ColumnasDeTotales_PidiendoLosDosTotalesOpcionales_DebeConservarElOrdenDeLaPantalla() {
        EntReporteConsultas vlo_Reporte = this.CrearReporte(this.CrearBloque("Desglose", List.of()));
        vlo_Reporte.setIncluirTotalCif(true);
        vlo_Reporte.setIncluirTotalBodegaje(true);

        assertEquals(List.of("Trámites", "Total CIF", "Impuestos pagados", "Exonerados",
                "Bodegajes (₡)", "Permisos"), this.TitulosDeTotales(vlo_Reporte));
    }

    @Test
    void ValoresDeGrupo_DeUnaLineaCombinada_DebeDevolverUnValorPorColumna() {
        EntLineaReporte vlo_Linea = this.CrearLinea("Desalmacenaje especial · Tránsito aduanero", "CRC", 1);
        vlo_Linea.setValores(List.of("Desalmacenaje especial", "Tránsito aduanero"));

        assertEquals(List.of("Desalmacenaje especial", "Tránsito aduanero"),
                this.vgo_Base.ValoresDeGrupo(vlo_Linea, 2));
    }

    @Test
    void ValoresDeGrupo_DeUnaLineaDeUnSoloCriterio_DebeDevolverElGrupo() {
        EntLineaReporte vlo_Linea = this.CrearLinea("Desalmacenaje especial", "CRC", 1);

        assertEquals(List.of("Desalmacenaje especial"), this.vgo_Base.ValoresDeGrupo(vlo_Linea, 1));
    }

    @Test
    void ValoresDeGrupo_CuandoLaLineaTraeMenosValoresQueColumnas_DebeRellenarLasRestantes() {
        EntLineaReporte vlo_Linea = this.CrearLinea("Total", "CRC", 3);

        assertEquals(List.of("Total", "", ""), this.vgo_Base.ValoresDeGrupo(vlo_Linea, 3));
    }

    @Test
    void TotalizarPorMoneda_ConVariasLineas_DebeSumarCadaMonedaPorSeparado() {
        EntLineaReporte vlo_Colones = this.CrearLinea("Desalmacenaje especial", "CRC", 2);
        vlo_Colones.setTotalCif(new BigDecimal("75000.00"));
        vlo_Colones.setTotalImpuestosPagados(new BigDecimal("9000.00"));
        EntLineaReporte vlo_OtroColones = this.CrearLinea("Solicitud de exoneración", "CRC", 1);
        vlo_OtroColones.setTotalCif(new BigDecimal("25000.00"));
        vlo_OtroColones.setTotalImpuestosPagados(new BigDecimal("1000.00"));
        EntLineaReporte vlo_Dolares = this.CrearLinea("Desalmacenaje especial", "USD", 1);
        vlo_Dolares.setTotalCif(new BigDecimal("1200.00"));

        List<EntLineaReporte> vlo_LsTotales = this.vgo_Base.TotalizarPorMoneda(
                List.of(vlo_Colones, vlo_OtroColones, vlo_Dolares));

        assertEquals(2, vlo_LsTotales.size());
        assertEquals("CRC", vlo_LsTotales.get(0).getMoneda());
        assertEquals(3, vlo_LsTotales.get(0).getCantidadTramites());
        assertEquals(new BigDecimal("100000.00"), vlo_LsTotales.get(0).getTotalCif());
        assertEquals(new BigDecimal("10000.00"), vlo_LsTotales.get(0).getTotalImpuestosPagados());
        assertEquals("USD", vlo_LsTotales.get(1).getMoneda());
        assertEquals(new BigDecimal("1200.00"), vlo_LsTotales.get(1).getTotalCif());
    }

    @Test
    void TotalizarPorMoneda_DebeIdentificarLaFilaComoTotal() {
        List<EntLineaReporte> vlo_LsTotales = this.vgo_Base.TotalizarPorMoneda(
                List.of(this.CrearLinea("Desalmacenaje especial", "CRC", 1)));

        assertEquals("Total", vlo_LsTotales.get(0).getGrupo());
    }

    @Test
    void TotalizarPorMoneda_ConMontosSinDeclarar_DebeTomarlosComoCero() {
        EntLineaReporte vlo_Linea = this.CrearLinea("Solicitud de exoneración", "Sin moneda", 1);

        List<EntLineaReporte> vlo_LsTotales = this.vgo_Base.TotalizarPorMoneda(List.of(vlo_Linea));

        assertEquals(BigDecimal.ZERO, vlo_LsTotales.get(0).getTotalCif());
        assertEquals(BigDecimal.ZERO, vlo_LsTotales.get(0).getTotalImpuestosPagados());
        assertEquals(BigDecimal.ZERO, vlo_LsTotales.get(0).getTotalImpuestosExonerados());
        assertEquals(BigDecimal.ZERO, vlo_LsTotales.get(0).getTotalBodegaje());
    }

    @Test
    void TotalizarPorMoneda_DebeSumarLosPermisosRegulatorios() {
        EntLineaReporte vlo_Primera = this.CrearLinea("Desalmacenaje especial", "CRC", 1);
        vlo_Primera.setCantidadPermisos(2);
        EntLineaReporte vlo_Segunda = this.CrearLinea("Solicitud de exoneración", "CRC", 1);
        vlo_Segunda.setCantidadPermisos(3);

        List<EntLineaReporte> vlo_LsTotales =
                this.vgo_Base.TotalizarPorMoneda(List.of(vlo_Primera, vlo_Segunda));

        assertEquals(5, vlo_LsTotales.get(0).getCantidadPermisos());
    }

    @Test
    void TotalizarPorMoneda_SinLineas_NoDebeAgregarFilaDeTotales() {
        assertTrue(this.vgo_Base.TotalizarPorMoneda(List.of()).isEmpty());
    }

    @Test
    void Monto_ConUnMontoSinDeclarar_DebeEntregarCero() {
        assertEquals(BigDecimal.ZERO, this.vgo_Base.Monto(null));
    }

    @Test
    void Monto_ConUnMontoDeclarado_DebeEntregarloTalCual() {
        assertEquals(new BigDecimal("75000.00"), this.vgo_Base.Monto(new BigDecimal("75000.00")));
    }

    private List<String> TitulosDeTotales(EntReporteConsultas pvo_Reporte) {
        return this.vgo_Base.ColumnasDeTotales(pvo_Reporte).stream()
                .map(Rpt_SIM_ReporteConsultas.ColumnaTotal::titulo)
                .toList();
    }

    private EntReporteConsultas CrearReporte(EntBloqueReporte pvo_Bloque) {
        EntReporteConsultas vlo_Reporte = new EntReporteConsultas();
        vlo_Reporte.setBloques(List.of(pvo_Bloque));
        return vlo_Reporte;
    }

    private EntBloqueReporte CrearBloque(String pvc_Titulo, List<EntLineaReporte> pvo_LsLineas) {
        return new EntBloqueReporte("TIPO_TRAMITE", pvc_Titulo, pvo_LsLineas);
    }

    private EntLineaReporte CrearLinea(String pvc_Grupo, String pvc_Moneda, long pvn_Tramites) {
        EntLineaReporte vlo_Linea = new EntLineaReporte();
        vlo_Linea.setGrupo(pvc_Grupo);
        vlo_Linea.setMoneda(pvc_Moneda);
        vlo_Linea.setCantidadTramites(pvn_Tramites);
        return vlo_Linea;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 12/10/2026
     * Descripción: Formato mínimo que expone la base para poder evaluar sus cálculos sin depender
     *   de la librería del Excel ni de la del PDF.
     */
    private static final class BaseDePrueba extends Rpt_SIM_ReporteConsultas {

        @Override
        public byte[] Generar(EntReporteConsultas pvo_Reporte) {
            return new byte[0];
        }

        @Override
        public String getFormato() {
            return "PRUEBA";
        }

        @Override
        public String getExtension() {
            return "txt";
        }

        @Override
        public String getTipoContenido() {
            return "text/plain";
        }
    }
}
