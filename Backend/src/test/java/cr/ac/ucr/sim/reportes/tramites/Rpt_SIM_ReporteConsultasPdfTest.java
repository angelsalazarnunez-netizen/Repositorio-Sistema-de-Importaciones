package cr.ac.ucr.sim.reportes.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntBloqueReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntLineaReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntReporteConsultas;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramiteReporte;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 12/10/2026
 * Descripción: Pruebas del reporte de consultas internas en PDF (HU-036). Abren el documento
 *   generado y revisan que traiga el logo y la información de la Oficina de Suministros, el pie
 *   institucional con el número de página y las mismas tablas que muestra la pantalla para la
 *   misma consulta.
 */
class Rpt_SIM_ReporteConsultasPdfTest {

    private Rpt_SIM_ReporteConsultasPdf vgo_Reporte;

    @BeforeEach
    void PrepararPrueba() {
        this.vgo_Reporte = new Rpt_SIM_ReporteConsultasPdf();
    }

    @Test
    void getFormato_DebeIdentificarElFormatoPdf() {
        assertEquals(Constantes.FORMATO_EXPORTACION_PDF, this.vgo_Reporte.getFormato());
        assertEquals("pdf", this.vgo_Reporte.getExtension());
        assertEquals("application/pdf", this.vgo_Reporte.getTipoContenido());
    }

    @Test
    void Generar_ConUnDesglose_DebeDevolverUnDocumentoPdfValido() throws IOException {
        byte[] vlo_Archivo = this.vgo_Reporte.Generar(this.CrearReporteDeUnaMoneda());

        assertTrue(vlo_Archivo.length > 0);
        assertEquals("%PDF", new String(vlo_Archivo, 0, 4, StandardCharsets.US_ASCII));
        PdfReader vlo_Lector = new PdfReader(vlo_Archivo);
        assertEquals(1, vlo_Lector.getNumberOfPages());
        vlo_Lector.close();
    }

    @Test
    void Generar_ConUnDesglose_DebeIncluirLaInformacionDeLaOficinaDeSuministros() throws IOException {
        String vlc_Texto = this.LeerTexto(this.CrearReporteDeUnaMoneda());

        assertTrue(vlc_Texto.contains(Constantes.ENCABEZADO_UNIVERSIDAD));
        assertTrue(vlc_Texto.contains(Constantes.ENCABEZADO_OFICINA));
        assertTrue(vlc_Texto.contains(Constantes.ENCABEZADO_UNIDAD));
        assertTrue(vlc_Texto.contains("Reporte de consultas internas"));
    }

    @Test
    void Generar_ConUnDesglose_DebeIncluirElLogoInstitucional() throws IOException {
        PdfReader vlo_Lector = new PdfReader(this.vgo_Reporte.Generar(this.CrearReporteDeUnaMoneda()));

        PdfDictionary vlo_Recursos = vlo_Lector.getPageN(1).getAsDict(PdfName.RESOURCES);
        PdfDictionary vlo_Imagenes = vlo_Recursos == null
                ? null : vlo_Recursos.getAsDict(PdfName.XOBJECT);

        assertNotNull(vlo_Imagenes, "La página del reporte no incorpora ninguna imagen.");
        assertFalse(vlo_Imagenes.getKeys().isEmpty(), "El reporte no incorpora el logo de la UCR.");
        vlo_Lector.close();
    }

    @Test
    void Generar_ConUnDesglose_DebeRepetirElPieConLaOficinaYElNumeroDePagina() throws IOException {
        String vlc_Texto = this.LeerTexto(this.CrearReporteDeUnaMoneda());

        assertTrue(vlc_Texto.contains("Página 1"));
        assertTrue(vlc_Texto.contains(Constantes.ENCABEZADO_UNIVERSIDAD + " · "
                + Constantes.ENCABEZADO_OFICINA + " · " + Constantes.ENCABEZADO_UNIDAD));
    }

    @Test
    void Generar_ConUnDesglose_DebeEscribirLaFechaDeGeneracionYLosConteosDelReporte()
            throws IOException {
        String vlc_Texto = this.LeerTexto(this.CrearReporteDeUnaMoneda());

        assertTrue(vlc_Texto.contains("Generado: 12/10/2026 09:30"));
        assertTrue(vlc_Texto.contains("Trámites: 3"));
        assertTrue(vlc_Texto.contains("Permisos regulatorios: 2"));
    }

    @Test
    void Generar_ConUnaSolaMoneda_NoDebeEscribirLaColumnaDeMoneda() throws IOException {
        String vlc_Texto = this.LeerTexto(this.CrearReporteDeUnaMoneda());

        assertFalse(vlc_Texto.contains("Moneda"));
        assertTrue(vlc_Texto.contains("Tipo de trámite"));
    }

    @Test
    void Generar_ConVariasMonedas_DebeEscribirLaColumnaDeMoneda() throws IOException {
        String vlc_Texto = this.LeerTexto(this.CrearReporteDeDosMonedas());

        assertTrue(vlc_Texto.contains("Moneda"));
        assertTrue(vlc_Texto.contains("USD"));
        assertTrue(vlc_Texto.contains("CRC"));
    }

    @Test
    void Generar_SinLosTotalesOpcionales_NoDebeEscribirCifNiBodegajes() throws IOException {
        String vlc_Texto = this.LeerTexto(this.CrearReporteDeUnaMoneda());

        assertFalse(vlc_Texto.contains("Total CIF"));
        assertFalse(vlc_Texto.contains("Bodegajes"));
        assertTrue(vlc_Texto.contains("Impuestos pagados"));
        assertTrue(vlc_Texto.contains("Exonerados"));
    }

    @Test
    void Generar_PidiendoLosTotalesOpcionales_DebeEscribirCifYBodegajes() throws IOException {
        EntReporteConsultas vlo_Reporte = this.CrearReporteDeUnaMoneda();
        vlo_Reporte.setIncluirTotalCif(true);
        vlo_Reporte.setIncluirTotalBodegaje(true);

        String vlc_Texto = this.LeerTexto(vlo_Reporte);

        assertTrue(vlc_Texto.contains("Total CIF"));
        assertTrue(vlc_Texto.contains("Bodegajes"));
    }

    @Test
    void Generar_ConUnDesglose_DebeEscribirElTituloYLasLineasDeLaTabla() throws IOException {
        String vlc_Texto = this.LeerTexto(this.CrearReporteDeUnaMoneda());

        assertTrue(vlc_Texto.contains("Desglose por tipo de trámite"));
        assertTrue(vlc_Texto.contains("Desalmacenaje especial"));
        assertTrue(vlc_Texto.contains("Solicitud de exoneración"));
    }

    @Test
    void Generar_ConUnDesglose_DebeCerrarLaTablaConLaFilaDeTotales() throws IOException {
        String vlc_Texto = this.LeerTexto(this.CrearReporteDeUnaMoneda());

        assertTrue(vlc_Texto.contains("Total"));
    }

    @Test
    void Generar_ConCriteriosCombinados_DebeEscribirUnaColumnaPorCriterio() throws IOException {
        String vlc_Texto = this.LeerTexto(this.CrearReporteCombinado());

        assertTrue(vlc_Texto.contains("Desglose combinado"));
        assertTrue(vlc_Texto.contains("Tipo de trámite"));
        assertTrue(vlc_Texto.contains("Régimen aduanero"));
        assertTrue(vlc_Texto.contains("Importación definitiva"));
    }

    @Test
    void Generar_ConVariosDesgloses_DebeEscribirUnaTablaPorCadaUno() throws IOException {
        String vlc_Texto = this.LeerTexto(this.CrearReporteDeDosDesgloses());

        assertTrue(vlc_Texto.contains("Desglose por tipo de trámite"));
        assertTrue(vlc_Texto.contains("Desglose por régimen aduanero"));
    }

    @Test
    void Generar_SinTramitesQueCumplanLosCriterios_DebeAvisarloEnLaTabla() throws IOException {
        EntReporteConsultas vlo_Reporte = this.CrearReporte(List.of(
                this.CrearBloque("Desglose por tipo de trámite", List.of("Tipo de trámite"),
                        List.of())));

        String vlc_Texto = this.LeerTexto(vlo_Reporte);

        assertTrue(vlc_Texto.contains("Ningún trámite cumple los criterios indicados."));
    }

    @Test
    void Generar_PidiendoEnumerarLosTramites_DebeEscribirLaTablaDeTramites() throws IOException {
        EntReporteConsultas vlo_Reporte = this.CrearReporteDeUnaMoneda();
        vlo_Reporte.setTramites(List.of(this.CrearTramite()));

        String vlc_Texto = this.LeerTexto(vlo_Reporte);

        assertTrue(vlc_Texto.contains("Trámites incluidos"));
        assertTrue(vlc_Texto.contains("DE-2026-0001"));
        assertTrue(vlc_Texto.contains("Unidad de Importaciones"));
    }

    @Test
    void Generar_SinPedirLaEnumeracion_NoDebeEscribirLaTablaDeTramites() throws IOException {
        String vlc_Texto = this.LeerTexto(this.CrearReporteDeUnaMoneda());

        assertFalse(vlc_Texto.contains("Trámites incluidos"));
    }

    @Test
    void Generar_ConLasNotasDelReporte_DebeEscribirlasAlCierre() throws IOException {
        EntReporteConsultas vlo_Reporte = this.CrearReporteDeUnaMoneda();
        vlo_Reporte.setNotas(List.of("Las monedas no se suman entre sí."));

        String vlc_Texto = this.LeerTexto(vlo_Reporte);

        assertTrue(vlc_Texto.contains("Las monedas no se suman entre sí."));
    }

    private String LeerTexto(EntReporteConsultas pvo_Reporte) throws IOException {
        PdfReader vlo_Lector = new PdfReader(this.vgo_Reporte.Generar(pvo_Reporte));
        try {
            StringBuilder vlo_Texto = new StringBuilder();
            PdfTextExtractor vlo_Extractor = new PdfTextExtractor(vlo_Lector);
            for (int vln_Pagina = 1; vln_Pagina <= vlo_Lector.getNumberOfPages(); vln_Pagina++) {
                vlo_Texto.append(vlo_Extractor.getTextFromPage(vln_Pagina)).append('\n');
            }
            return vlo_Texto.toString();
        } finally {
            vlo_Lector.close();
        }
    }

    private EntReporteConsultas CrearReporteDeUnaMoneda() {
        EntLineaReporte vlo_Desalmacenaje = this.CrearLinea("Desalmacenaje especial", "CRC", 2);
        vlo_Desalmacenaje.setTotalCif(new BigDecimal("75000.00"));
        vlo_Desalmacenaje.setTotalImpuestosPagados(new BigDecimal("9000.00"));
        EntLineaReporte vlo_Exoneracion = this.CrearLinea("Solicitud de exoneración", "CRC", 1);

        return this.CrearReporte(List.of(this.CrearBloque("Desglose por tipo de trámite",
                List.of("Tipo de trámite"), List.of(vlo_Desalmacenaje, vlo_Exoneracion))));
    }

    private EntReporteConsultas CrearReporteDeDosMonedas() {
        EntLineaReporte vlo_Colones = this.CrearLinea("Desalmacenaje especial", "CRC", 2);
        EntLineaReporte vlo_Dolares = this.CrearLinea("Desalmacenaje especial", "USD", 1);

        return this.CrearReporte(List.of(this.CrearBloque("Desglose por tipo de trámite",
                List.of("Tipo de trámite"), List.of(vlo_Colones, vlo_Dolares))));
    }

    private EntReporteConsultas CrearReporteCombinado() {
        EntLineaReporte vlo_Linea =
                this.CrearLinea("Desalmacenaje especial · Importación definitiva", "CRC", 2);
        vlo_Linea.setValores(List.of("Desalmacenaje especial", "Importación definitiva"));

        return this.CrearReporte(List.of(this.CrearBloque("Desglose combinado",
                List.of("Tipo de trámite", "Régimen aduanero"), List.of(vlo_Linea))));
    }

    private EntReporteConsultas CrearReporteDeDosDesgloses() {
        return this.CrearReporte(List.of(
                this.CrearBloque("Desglose por tipo de trámite", List.of("Tipo de trámite"),
                        List.of(this.CrearLinea("Desalmacenaje especial", "CRC", 2))),
                this.CrearBloque("Desglose por régimen aduanero", List.of("Régimen aduanero"),
                        List.of(this.CrearLinea("Importación definitiva", "CRC", 2)))));
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
