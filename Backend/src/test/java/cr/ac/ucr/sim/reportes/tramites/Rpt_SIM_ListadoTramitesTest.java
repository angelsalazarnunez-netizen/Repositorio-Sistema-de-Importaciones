package cr.ac.ucr.sim.reportes.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntColumnaBusqueda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntListadoTramites;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResultadoBusquedaTramite;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Pruebas de los reportes del listado de trámites (HU-034). Cada archivo generado se
 *   vuelve a abrir con su librería para comprobar que contiene las mismas columnas, filas y valores.
 */
class Rpt_SIM_ListadoTramitesTest {

    private static final List<EntColumnaBusqueda> COLUMNAS = Rpt_SIM_ListadoTramites.ObtenerColumnas();

    @Test
    void ObtenerColumnas_DefineLasColumnasDeLaHuEnOrden() {
        assertEquals(List.of("Número de trámite", "Fecha de registro", "Fecha de envío", "Fecha de desalmacenaje",
                        "Tipo de trámite", "Régimen aduanero", "Usuario", "Estado", "Valor de mercancía",
                        "Impuestos pagados", "Impuestos exonerados", "Tipo de exoneración", "Costos de permisos",
                        "Moneda", "Total CIF"),
                COLUMNAS.stream().map(EntColumnaBusqueda::getTitulo).toList());
    }

    @Test
    void Excel_GeneraEncabezadoFilasYValoresNumericos() throws Exception {
        byte[] vlo_Contenido = new Rpt_SIM_ListadoTramitesExcel().Generar(this.CrearListado());

        try (XSSFWorkbook vlo_Libro = new XSSFWorkbook(new ByteArrayInputStream(vlo_Contenido))) {
            Sheet vlo_Hoja = vlo_Libro.getSheetAt(0);
            assertEquals(Constantes.TITULO_LISTADO_TRAMITES, vlo_Hoja.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Estado: Trámite activo", vlo_Hoja.getRow(2).getCell(0).getStringCellValue());
            Row vlo_Encabezado = vlo_Hoja.getRow(4);
            assertEquals("Número de trámite", vlo_Encabezado.getCell(0).getStringCellValue());
            assertEquals("Total CIF", vlo_Encabezado.getCell(14).getStringCellValue());

            Row vlo_Primera = vlo_Hoja.getRow(5);
            assertEquals("DE-2026-0002", vlo_Primera.getCell(0).getStringCellValue());
            assertEquals(CellType.NUMERIC, vlo_Primera.getCell(1).getCellType());
            assertEquals(Constantes.TEXTO_NO_REGISTRADO_AUN, vlo_Primera.getCell(2).getStringCellValue());
            assertEquals("Trámite activo (En revisión)", vlo_Primera.getCell(7).getStringCellValue());
            assertEquals(425000.50, vlo_Primera.getCell(8).getNumericCellValue(), 0.001);
            assertEquals(Constantes.TEXTO_NO_REGISTRADO_AUN, vlo_Primera.getCell(14).getStringCellValue());

            Row vlo_Segunda = vlo_Hoja.getRow(6);
            assertEquals("EX-2026-0001", vlo_Segunda.getCell(0).getStringCellValue());
            assertEquals(Constantes.TEXTO_SIN_DATO, vlo_Segunda.getCell(8).getStringCellValue());
            assertEquals("Donación", vlo_Segunda.getCell(11).getStringCellValue());
            assertEquals(6, vlo_Hoja.getLastRowNum());
        }
    }

    @Test
    void Word_GeneraTablaConEncabezadoYUnaFilaPorTramite() throws Exception {
        byte[] vlo_Contenido = new Rpt_SIM_ListadoTramitesWord().Generar(this.CrearListado());

        try (XWPFDocument vlo_Documento = new XWPFDocument(new ByteArrayInputStream(vlo_Contenido))) {
            XWPFTable vlo_Tabla = vlo_Documento.getTables().get(0);
            assertEquals(3, vlo_Tabla.getNumberOfRows());
            assertEquals("Número de trámite", vlo_Tabla.getRow(0).getCell(0).getText());
            assertEquals("DE-2026-0002", vlo_Tabla.getRow(1).getCell(0).getText());
            assertEquals("425 000,50", vlo_Tabla.getRow(1).getCell(8).getText());
            assertEquals(Constantes.TEXTO_NO_REGISTRADO_AUN, vlo_Tabla.getRow(2).getCell(9).getText());
            assertEquals("Donación", vlo_Tabla.getRow(2).getCell(11).getText());
            assertTrue(vlo_Documento.getParagraphs().stream()
                    .anyMatch(vlo_Parrafo -> vlo_Parrafo.getText().equals("Estado: Trámite activo")));
        }
    }

    @Test
    void Pdf_GeneraDocumentoLegibleConLosTramitesYCriterios() throws Exception {
        byte[] vlo_Contenido = new Rpt_SIM_ListadoTramitesPdf().Generar(this.CrearListado());

        PdfReader vlo_Lector = new PdfReader(vlo_Contenido);
        // Las celdas angostas parten el texto en varias líneas: se unifican los espacios.
        String vlc_Texto = new PdfTextExtractor(vlo_Lector).getTextFromPage(1).replaceAll("\\s+", " ");
        vlo_Lector.close();
        assertTrue(new String(vlo_Contenido, 0, 4).startsWith("%PDF"));
        assertTrue(vlc_Texto.contains(Constantes.TITULO_LISTADO_TRAMITES));
        assertTrue(vlc_Texto.contains("Estado: Trámite activo"));
        assertTrue(vlc_Texto.contains("DE-2026-0002"));
        assertTrue(vlc_Texto.contains("EX-2026-0001"));
        assertTrue(vlc_Texto.contains("Fecha de desalmacenaje"));
        assertTrue(vlc_Texto.contains(Constantes.TEXTO_NO_REGISTRADO_AUN));
    }

    @Test
    void TodosLosFormatos_SinResultados_GeneranArchivoConMensaje() throws Exception {
        EntListadoTramites vlo_Listado = new EntListadoTramites(Constantes.TITULO_LISTADO_TRAMITES,
                LocalDateTime.now(), List.of("Criterios: todos los trámites"), COLUMNAS, List.of());

        byte[] vlo_Pdf = new Rpt_SIM_ListadoTramitesPdf().Generar(vlo_Listado);
        byte[] vlo_Word = new Rpt_SIM_ListadoTramitesWord().Generar(vlo_Listado);
        byte[] vlo_Excel = new Rpt_SIM_ListadoTramitesExcel().Generar(vlo_Listado);

        PdfReader vlo_Lector = new PdfReader(vlo_Pdf);
        assertTrue(new PdfTextExtractor(vlo_Lector).getTextFromPage(1).contains("No hay trámites"));
        vlo_Lector.close();
        try (XWPFDocument vlo_Documento = new XWPFDocument(new ByteArrayInputStream(vlo_Word))) {
            assertTrue(vlo_Documento.getTables().get(0).getRow(1).getCell(0).getText().startsWith("No hay trámites"));
        }
        try (XSSFWorkbook vlo_Libro = new XSSFWorkbook(new ByteArrayInputStream(vlo_Excel))) {
            assertEquals("Número de trámite", vlo_Libro.getSheetAt(0).getRow(4).getCell(0).getStringCellValue());
        }
    }

    private EntListadoTramites CrearListado() {
        EntResultadoBusquedaTramite vlo_Desalmacenaje = new EntResultadoBusquedaTramite(1, "DE-2026-0002",
                LocalDateTime.of(2026, 8, 14, 14, 30), "Desalmacenaje especial", "Importación definitiva",
                "Analista de Importaciones", "ACT", "En revisión", new BigDecimal("425000.50"), "CRC", null);
        vlo_Desalmacenaje.setCategoriaEstadoNombre("Trámite activo");
        EntResultadoBusquedaTramite vlo_Exoneracion = new EntResultadoBusquedaTramite(2, "EX-2026-0001",
                LocalDateTime.of(2026, 8, 20, 10, 45), "Solicitud de exoneración", null,
                "Analista de Importaciones", "ACT", "Pendiente revisión documental", null, null, "DONACION");
        vlo_Exoneracion.setCategoriaEstadoNombre("Trámite activo");
        vlo_Exoneracion.setTipoExoneracionNombre("Donación");
        return new EntListadoTramites(Constantes.TITULO_LISTADO_TRAMITES, LocalDateTime.of(2026, 9, 16, 10, 0),
                List.of("Estado: Trámite activo"), COLUMNAS, List.of(vlo_Desalmacenaje, vlo_Exoneracion));
    }
}
