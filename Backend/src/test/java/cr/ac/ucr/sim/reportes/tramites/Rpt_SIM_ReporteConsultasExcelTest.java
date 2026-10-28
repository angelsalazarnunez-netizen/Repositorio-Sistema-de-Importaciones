package cr.ac.ucr.sim.reportes.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntBloqueReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntLineaReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntReporteConsultas;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramiteReporte;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 12/10/2026
 * Descripción: Pruebas del reporte de consultas internas en Excel (HU-036). Abren el archivo
 *   generado y revisan que la hoja traiga el encabezado institucional, una tabla por cada
 *   desglose con las mismas columnas que la pantalla, su fila de totales por moneda y los montos
 *   guardados como números, para que la persona agente pueda sumarlos en la hoja.
 */
class Rpt_SIM_ReporteConsultasExcelTest {

    private Rpt_SIM_ReporteConsultasExcel vgo_Reporte;

    @BeforeEach
    void PrepararPrueba() {
        this.vgo_Reporte = new Rpt_SIM_ReporteConsultasExcel();
    }

    @Test
    void getFormato_DebeIdentificarElFormatoExcel() {
        assertEquals(Constantes.FORMATO_EXPORTACION_EXCEL, this.vgo_Reporte.getFormato());
        assertEquals("xlsx", this.vgo_Reporte.getExtension());
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                this.vgo_Reporte.getTipoContenido());
    }

    @Test
    void Generar_ConUnDesglose_DebeEscribirElEncabezadoDeLaOficinaDeSuministros() throws IOException {
        List<String> vlo_LsTexto = this.LeerPrimeraColumna(this.CrearReporteDeUnaMoneda());

        assertTrue(vlo_LsTexto.contains(Constantes.ENCABEZADO_UNIVERSIDAD));
        assertTrue(vlo_LsTexto.contains(
                Constantes.ENCABEZADO_OFICINA + " · " + Constantes.ENCABEZADO_UNIDAD));
        assertTrue(vlo_LsTexto.contains("Reporte de consultas internas"));
    }

    @Test
    void Generar_ConUnDesglose_DebeEscribirLaFechaDeGeneracionYLosConteosDelReporte()
            throws IOException {
        List<String> vlo_LsTexto = this.LeerPrimeraColumna(this.CrearReporteDeUnaMoneda());

        assertTrue(vlo_LsTexto.contains("Generado: 12/10/2026 09:30"));
        assertTrue(vlo_LsTexto.contains("Trámites: 3   ·   Permisos regulatorios: 2"));
    }

    @Test
    void Generar_ConUnDesglose_DebeEscribirElTituloDeLaTabla() throws IOException {
        List<String> vlo_LsTexto = this.LeerPrimeraColumna(this.CrearReporteDeUnaMoneda());

        assertTrue(vlo_LsTexto.contains("Desglose por tipo de trámite"));
    }

    @Test
    void Generar_ConUnaSolaMoneda_NoDebeEscribirLaColumnaDeMoneda() throws IOException {
        List<String> vlo_LsEncabezado = this.BuscarEncabezado(this.CrearReporteDeUnaMoneda(),
                "Tipo de trámite");

        assertFalse(vlo_LsEncabezado.contains("Moneda"));
        assertEquals(List.of("Tipo de trámite", "Trámites", "Impuestos pagados", "Exonerados",
                "Permisos"), vlo_LsEncabezado);
    }

    @Test
    void Generar_ConVariasMonedas_DebeEscribirLaColumnaDeMoneda() throws IOException {
        List<String> vlo_LsEncabezado = this.BuscarEncabezado(this.CrearReporteDeDosMonedas(),
                "Tipo de trámite");

        assertEquals(List.of("Tipo de trámite", "Moneda", "Trámites", "Impuestos pagados",
                "Exonerados", "Permisos"), vlo_LsEncabezado);
    }

    @Test
    void Generar_PidiendoLosTotalesOpcionales_DebeEscribirCifYBodegajes() throws IOException {
        EntReporteConsultas vlo_Reporte = this.CrearReporteDeUnaMoneda();
        vlo_Reporte.setIncluirTotalCif(true);
        vlo_Reporte.setIncluirTotalBodegaje(true);

        List<String> vlo_LsEncabezado = this.BuscarEncabezado(vlo_Reporte, "Tipo de trámite");

        assertEquals(List.of("Tipo de trámite", "Trámites", "Total CIF", "Impuestos pagados",
                "Exonerados", "Bodegajes (₡)", "Permisos"), vlo_LsEncabezado);
    }

    @Test
    void Generar_ConUnDesglose_DebeEscribirUnaFilaPorLinea() throws IOException {
        List<List<String>> vlo_LsFilas = this.LeerFilas(this.CrearReporteDeUnaMoneda());
        int vln_Encabezado = this.BuscarFila(vlo_LsFilas, "Tipo de trámite");

        assertEquals("Desalmacenaje especial", vlo_LsFilas.get(vln_Encabezado + 1).get(0));
        assertEquals("Solicitud de exoneración", vlo_LsFilas.get(vln_Encabezado + 2).get(0));
    }

    @Test
    void Generar_ConUnDesglose_DebeCerrarLaTablaConLosTotalesPorMoneda() throws IOException {
        List<List<String>> vlo_LsFilas = this.LeerFilas(this.CrearReporteDeDosMonedas());
        int vln_Encabezado = this.BuscarFila(vlo_LsFilas, "Tipo de trámite");

        List<String> vlo_LsTotalColones = vlo_LsFilas.get(vln_Encabezado + 4);
        assertEquals("Total", vlo_LsTotalColones.get(0));
        assertEquals("CRC", vlo_LsTotalColones.get(1));
        assertEquals("3", vlo_LsTotalColones.get(2));
        assertEquals("Total", vlo_LsFilas.get(vln_Encabezado + 5).get(0));
        assertEquals("USD", vlo_LsFilas.get(vln_Encabezado + 5).get(1));
    }

    @Test
    void Generar_ConCriteriosCombinados_DebeEscribirUnaColumnaPorCriterio() throws IOException {
        List<List<String>> vlo_LsFilas = this.LeerFilas(this.CrearReporteCombinado());
        int vln_Encabezado = this.BuscarFila(vlo_LsFilas, "Tipo de trámite");

        assertEquals(List.of("Tipo de trámite", "Régimen aduanero", "Trámites", "Impuestos pagados",
                "Exonerados", "Permisos"), vlo_LsFilas.get(vln_Encabezado));
        assertEquals("Desalmacenaje especial", vlo_LsFilas.get(vln_Encabezado + 1).get(0));
        assertEquals("Importación definitiva", vlo_LsFilas.get(vln_Encabezado + 1).get(1));
    }

    @Test
    void Generar_ConVariosDesgloses_DebeEscribirUnaTablaPorCadaUno() throws IOException {
        List<String> vlo_LsTexto = this.LeerPrimeraColumna(this.CrearReporteDeDosDesgloses());

        assertTrue(vlo_LsTexto.contains("Desglose por tipo de trámite"));
        assertTrue(vlo_LsTexto.contains("Desglose por régimen aduanero"));
    }

    @Test
    void Generar_ConLosMontos_DebeGuardarlosComoNumerosYNoComoTexto() throws IOException {
        EntReporteConsultas vlo_Reporte = this.CrearReporteDeUnaMoneda();
        vlo_Reporte.setIncluirTotalCif(true);
        vlo_Reporte.setIncluirTotalBodegaje(false);

        try (XSSFWorkbook vlo_Libro = this.Abrir(vlo_Reporte)) {
            Sheet vlo_Hoja = vlo_Libro.getSheetAt(0);
            List<List<String>> vlo_LsFilas = this.LeerFilas(vlo_Reporte);
            int vln_Fila = this.BuscarFila(vlo_LsFilas, "Tipo de trámite") + 1;

            Cell vlo_Tramites = vlo_Hoja.getRow(vln_Fila).getCell(1);
            Cell vlo_Cif = vlo_Hoja.getRow(vln_Fila).getCell(2);
            assertEquals(CellType.NUMERIC, vlo_Tramites.getCellType());
            assertEquals(CellType.NUMERIC, vlo_Cif.getCellType());
            assertEquals(75000.00, vlo_Cif.getNumericCellValue(), 0.001);
        }
    }

    @Test
    void Generar_PidiendoEnumerarLosTramites_DebeEscribirLaTablaDeTramites() throws IOException {
        EntReporteConsultas vlo_Reporte = this.CrearReporteDeUnaMoneda();
        vlo_Reporte.setTramites(List.of(this.CrearTramite()));

        List<List<String>> vlo_LsFilas = this.LeerFilas(vlo_Reporte);
        int vln_Encabezado = this.BuscarFila(vlo_LsFilas, "Referencia");

        assertEquals(List.of("Referencia", "Tipo de trámite", "Régimen aduanero",
                "Partido responsable", "CIF", "Estado"), vlo_LsFilas.get(vln_Encabezado));
        assertEquals("DE-2026-0001", vlo_LsFilas.get(vln_Encabezado + 1).get(0));
        assertEquals("Unidad de Importaciones", vlo_LsFilas.get(vln_Encabezado + 1).get(3));
    }

    @Test
    void Generar_SinPedirLaEnumeracion_NoDebeEscribirLaTablaDeTramites() throws IOException {
        List<String> vlo_LsTexto = this.LeerPrimeraColumna(this.CrearReporteDeUnaMoneda());

        assertFalse(vlo_LsTexto.contains("Trámites incluidos"));
    }

    @Test
    void Generar_ConLasNotasDelReporte_DebeEscribirlasAlCierre() throws IOException {
        EntReporteConsultas vlo_Reporte = this.CrearReporteDeUnaMoneda();
        vlo_Reporte.setNotas(List.of("Las monedas no se suman entre sí."));

        List<String> vlo_LsTexto = this.LeerPrimeraColumna(vlo_Reporte);

        assertTrue(vlo_LsTexto.contains("Las monedas no se suman entre sí."));
    }

    @Test
    void Generar_SinTramitesQueCumplan_DebeEscribirLaTablaSoloConSusEncabezados() throws IOException {
        EntReporteConsultas vlo_Reporte = new EntReporteConsultas();
        vlo_Reporte.setFechaGeneracion(LocalDateTime.of(2026, 10, 12, 9, 30));
        vlo_Reporte.setIncluirTotalCif(false);
        vlo_Reporte.setIncluirTotalBodegaje(false);
        EntBloqueReporte vlo_Bloque =
                new EntBloqueReporte("TIPO_TRAMITE", "Desglose por tipo de trámite", List.of());
        vlo_Bloque.setEncabezados(List.of("Tipo de trámite"));
        vlo_Reporte.setBloques(List.of(vlo_Bloque));

        List<List<String>> vlo_LsFilas = this.LeerFilas(vlo_Reporte);
        int vln_Encabezado = this.BuscarFila(vlo_LsFilas, "Tipo de trámite");

        assertNotNull(vlo_LsFilas.get(vln_Encabezado));
        assertTrue(vln_Encabezado + 1 >= vlo_LsFilas.size()
                || vlo_LsFilas.get(vln_Encabezado + 1).isEmpty());
    }

    private XSSFWorkbook Abrir(EntReporteConsultas pvo_Reporte) throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(this.vgo_Reporte.Generar(pvo_Reporte)));
    }

    private List<List<String>> LeerFilas(EntReporteConsultas pvo_Reporte) throws IOException {
        List<List<String>> vlo_LsFilas = new ArrayList<>();
        try (XSSFWorkbook vlo_Libro = this.Abrir(pvo_Reporte)) {
            Sheet vlo_Hoja = vlo_Libro.getSheetAt(0);
            for (int vln_Fila = 0; vln_Fila <= vlo_Hoja.getLastRowNum(); vln_Fila++) {
                vlo_LsFilas.add(this.LeerFila(vlo_Hoja.getRow(vln_Fila)));
            }
        }
        return vlo_LsFilas;
    }

    private List<String> LeerFila(Row pvo_Fila) {
        List<String> vlo_LsValores = new ArrayList<>();
        if (pvo_Fila == null) {
            return vlo_LsValores;
        }
        for (int vln_Columna = 0; vln_Columna < pvo_Fila.getLastCellNum(); vln_Columna++) {
            Cell vlo_Celda = pvo_Fila.getCell(vln_Columna);
            if (vlo_Celda == null || vlo_Celda.getCellType() == CellType.BLANK) {
                vlo_LsValores.add("");
            } else if (vlo_Celda.getCellType() == CellType.NUMERIC) {
                vlo_LsValores.add(this.FormatearNumero(vlo_Celda.getNumericCellValue()));
            } else {
                vlo_LsValores.add(vlo_Celda.getStringCellValue());
            }
        }
        return vlo_LsValores;
    }

    private String FormatearNumero(double pvn_Valor) {
        return pvn_Valor == Math.rint(pvn_Valor)
                ? String.valueOf((long) pvn_Valor)
                : String.valueOf(pvn_Valor);
    }

    private List<String> LeerPrimeraColumna(EntReporteConsultas pvo_Reporte) throws IOException {
        return this.LeerFilas(pvo_Reporte).stream()
                .map(vlo_Fila -> vlo_Fila.isEmpty() ? "" : vlo_Fila.get(0))
                .toList();
    }

    private List<String> BuscarEncabezado(EntReporteConsultas pvo_Reporte, String pvc_Primera)
            throws IOException {
        List<List<String>> vlo_LsFilas = this.LeerFilas(pvo_Reporte);
        return vlo_LsFilas.get(this.BuscarFila(vlo_LsFilas, pvc_Primera));
    }

    private int BuscarFila(List<List<String>> pvo_LsFilas, String pvc_Primera) {
        for (int vln_Fila = 0; vln_Fila < pvo_LsFilas.size(); vln_Fila++) {
            List<String> vlo_Fila = pvo_LsFilas.get(vln_Fila);
            if (!vlo_Fila.isEmpty() && pvc_Primera.equals(vlo_Fila.get(0)) && vlo_Fila.size() > 1) {
                return vln_Fila;
            }
        }
        throw new AssertionError("La hoja no trae una fila que empiece con " + pvc_Primera);
    }

    private EntReporteConsultas CrearReporteDeUnaMoneda() {
        EntLineaReporte vlo_Desalmacenaje = this.CrearLinea("Desalmacenaje especial", "CRC", 2);
        vlo_Desalmacenaje.setTotalCif(new BigDecimal("75000.00"));
        vlo_Desalmacenaje.setTotalImpuestosPagados(new BigDecimal("9000.00"));
        EntLineaReporte vlo_Exoneracion = this.CrearLinea("Solicitud de exoneración", "CRC", 1);
        vlo_Exoneracion.setTotalCif(new BigDecimal("25000.00"));

        return this.CrearReporte(List.of(this.CrearBloque("Desglose por tipo de trámite",
                List.of("Tipo de trámite"), List.of(vlo_Desalmacenaje, vlo_Exoneracion))));
    }

    private EntReporteConsultas CrearReporteDeDosMonedas() {
        EntLineaReporte vlo_Colones = this.CrearLinea("Desalmacenaje especial", "CRC", 2);
        EntLineaReporte vlo_OtroColones = this.CrearLinea("Solicitud de exoneración", "CRC", 1);
        EntLineaReporte vlo_Dolares = this.CrearLinea("Desalmacenaje especial", "USD", 1);

        return this.CrearReporte(List.of(this.CrearBloque("Desglose por tipo de trámite",
                List.of("Tipo de trámite"), List.of(vlo_Colones, vlo_OtroColones, vlo_Dolares))));
    }

    private EntReporteConsultas CrearReporteCombinado() {
        EntLineaReporte vlo_Linea =
                this.CrearLinea("Desalmacenaje especial · Importación definitiva", "CRC", 2);
        vlo_Linea.setValores(List.of("Desalmacenaje especial", "Importación definitiva"));

        return this.CrearReporte(List.of(this.CrearBloque("Desglose combinado",
                List.of("Tipo de trámite", "Régimen aduanero"), List.of(vlo_Linea))));
    }

    private EntReporteConsultas CrearReporteDeDosDesgloses() {
        EntLineaReporte vlo_Tipo = this.CrearLinea("Desalmacenaje especial", "CRC", 2);
        EntLineaReporte vlo_Regimen = this.CrearLinea("Importación definitiva", "CRC", 2);

        return this.CrearReporte(List.of(
                this.CrearBloque("Desglose por tipo de trámite", List.of("Tipo de trámite"),
                        List.of(vlo_Tipo)),
                this.CrearBloque("Desglose por régimen aduanero", List.of("Régimen aduanero"),
                        List.of(vlo_Regimen))));
    }

    private EntReporteConsultas CrearReporte(List<EntBloqueReporte> pvo_LsBloques) {
        EntReporteConsultas vlo_Reporte = new EntReporteConsultas();
        vlo_Reporte.setFechaGeneracion(LocalDateTime.of(2026, 10, 12, 9, 30));
        vlo_Reporte.setCantidadTramites(3);
        vlo_Reporte.setCantidadPermisos(2);
        vlo_Reporte.setBloques(pvo_LsBloques);
        vlo_Reporte.setIncluirTotalCif(false);
        vlo_Reporte.setIncluirTotalBodegaje(false);
        return vlo_Reporte;
    }

    private EntBloqueReporte CrearBloque(
            String pvc_Titulo,
            List<String> pvo_LsEncabezados,
            List<EntLineaReporte> pvo_LsLineas) {

        EntBloqueReporte vlo_Bloque = new EntBloqueReporte("TIPO_TRAMITE", pvc_Titulo, pvo_LsLineas);
        vlo_Bloque.setEncabezados(pvo_LsEncabezados);
        return vlo_Bloque;
    }

    private EntLineaReporte CrearLinea(String pvc_Grupo, String pvc_Moneda, long pvn_Tramites) {
        EntLineaReporte vlo_Linea = new EntLineaReporte();
        vlo_Linea.setGrupo(pvc_Grupo);
        vlo_Linea.setMoneda(pvc_Moneda);
        vlo_Linea.setCantidadTramites(pvn_Tramites);
        return vlo_Linea;
    }

    private EntTramiteReporte CrearTramite() {
        return new EntTramiteReporte("DE-2026-0001", "Desalmacenaje especial",
                "Importación definitiva", "Pendiente", "CRC", new BigDecimal("75000.00"),
                "Escuela de Física", "Unidad de Importaciones", new BigDecimal("9000.00"),
                new BigDecimal("1500.00"), new BigDecimal("25000.00"), null,
                LocalDateTime.of(2026, 9, 10, 9, 0), 2L);
    }
}
