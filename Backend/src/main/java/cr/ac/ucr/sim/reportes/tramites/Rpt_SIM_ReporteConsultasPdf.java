package cr.ac.ucr.sim.reportes.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntBloqueReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntLineaReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntReporteConsultas;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramiteReporte;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 05/10/2026
 * Descripción: Reporte de consultas internas en PDF (HU-036). Encabeza el documento con el logo
 *   de la Universidad y los datos de la oficina, y escribe las mismas tablas que muestra la
 *   pantalla: sus columnas de agrupación, su moneda cuando hay más de una, los totales que se
 *   pidieron incluir y la fila de totales por moneda.
 *
 * Historial de cambios:
 *   05/10/2026 Katherine Susana Valverde Fallas: creación de la clase.
 *   05/10/2026 Katherine Susana Valverde Fallas: encabezado institucional y tablas iguales a las
 *     de la pantalla.
 */
@Component
public class Rpt_SIM_ReporteConsultasPdf extends Rpt_SIM_ReporteConsultas {

    private static final Color COLOR_ENCABEZADO = new Color(0, 51, 102);
    private static final NumberFormat FORMATO_MONTO = NumberFormat.getNumberInstance(new Locale("es", "CR"));

    @Override
    public String getFormato() {
        return Constantes.FORMATO_EXPORTACION_PDF;
    }

    @Override
    public String getExtension() {
        return "pdf";
    }

    @Override
    public String getTipoContenido() {
        return "application/pdf";
    }

    @Override
    public byte[] Generar(EntReporteConsultas pvo_Reporte) {
        FORMATO_MONTO.setMinimumFractionDigits(2);
        FORMATO_MONTO.setMaximumFractionDigits(2);

        Document vlo_Documento = new Document(PageSize.LETTER.rotate(), 36, 36, 36, 48);
        try (ByteArrayOutputStream vlo_Salida = new ByteArrayOutputStream()) {
            PdfWriter vlo_Escritor = PdfWriter.getInstance(vlo_Documento, vlo_Salida);
            vlo_Escritor.setPageEvent(new Rpt_SIM_PieInstitucional());
            vlo_Documento.open();

            Font vlo_FuenteTitulo = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font vlo_FuenteSubtitulo = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font vlo_FuenteTexto = new Font(Font.HELVETICA, 9);

            this.AgregarEncabezadoInstitucional(vlo_Documento, vlo_FuenteTitulo, vlo_FuenteTexto);
            vlo_Documento.add(new Paragraph("Generado: "
                    + (pvo_Reporte.getFechaGeneracion() == null
                            ? "" : pvo_Reporte.getFechaGeneracion().format(FORMATO_FECHA_HORA)),
                    vlo_FuenteTexto));
            vlo_Documento.add(new Paragraph("Trámites: " + pvo_Reporte.getCantidadTramites()
                    + "   ·   Permisos regulatorios: " + pvo_Reporte.getCantidadPermisos(),
                    vlo_FuenteTexto));
            vlo_Documento.add(new Paragraph(" ", vlo_FuenteTexto));

            boolean vlb_MuestraMoneda = this.MuestraMoneda(pvo_Reporte);
            List<ColumnaTotal> vlo_LsColumnas = this.ColumnasDeTotales(pvo_Reporte);
            for (EntBloqueReporte vlo_Bloque : pvo_Reporte.getBloques()) {
                this.AgregarTabla(vlo_Documento, vlo_Bloque, vlo_LsColumnas, vlb_MuestraMoneda,
                        vlo_FuenteSubtitulo, vlo_FuenteTexto);
            }

            if (!pvo_Reporte.getTramites().isEmpty()) {
                this.AgregarTramites(vlo_Documento, pvo_Reporte.getTramites(),
                        vlo_FuenteSubtitulo, vlo_FuenteTexto);
            }

            for (String vlo_Nota : pvo_Reporte.getNotas()) {
                vlo_Documento.add(new Paragraph(vlo_Nota, vlo_FuenteTexto));
            }

            vlo_Documento.close();
            return vlo_Salida.toByteArray();
        } catch (DocumentException | IOException vlo_Excepcion) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REPORTE_NO_GENERADO);
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Escribe un desglose con el mismo encabezado, las mismas líneas y la misma fila
     *   de totales que muestra la pantalla.
     *
     * @param pvo_Documento documento en construcción
     * @param pvo_Bloque desglose que se escribe
     * @param pvo_LsColumnas columnas de totales que se incluyen
     * @param pvb_MuestraMoneda true si la tabla lleva la columna de moneda
     * @param pvo_FuenteSubtitulo fuente del título de la tabla
     * @param pvo_FuenteTexto fuente de las celdas
     * @throws DocumentException si el documento no admite la tabla
     */
    private void AgregarTabla(
            Document pvo_Documento,
            EntBloqueReporte pvo_Bloque,
            List<ColumnaTotal> pvo_LsColumnas,
            boolean pvb_MuestraMoneda,
            Font pvo_FuenteSubtitulo,
            Font pvo_FuenteTexto) throws DocumentException {

        List<String> vlo_LsGrupos = this.ColumnasDeGrupo(pvo_Bloque);
        List<String> vlo_LsEncabezados = new ArrayList<>(vlo_LsGrupos);
        if (pvb_MuestraMoneda) {
            vlo_LsEncabezados.add("Moneda");
        }
        pvo_LsColumnas.forEach(vlo_Columna -> vlo_LsEncabezados.add(vlo_Columna.titulo()));

        pvo_Documento.add(new Paragraph(pvo_Bloque.getTitulo(), pvo_FuenteSubtitulo));
        PdfPTable vlo_Tabla = new PdfPTable(vlo_LsEncabezados.size());
        vlo_Tabla.setWidthPercentage(100);
        vlo_Tabla.setSpacingBefore(4);
        vlo_Tabla.setSpacingAfter(10);

        for (String vlo_Encabezado : vlo_LsEncabezados) {
            vlo_Tabla.addCell(this.CrearEncabezado(vlo_Encabezado));
        }

        if (pvo_Bloque.getLineas().isEmpty()) {
            PdfPCell vlo_Celda = new PdfPCell(
                    new Phrase("Ningún trámite cumple los criterios indicados.", pvo_FuenteTexto));
            vlo_Celda.setColspan(vlo_LsEncabezados.size());
            vlo_Tabla.addCell(vlo_Celda);
        }

        Font vlo_FuenteTotal = new Font(Font.HELVETICA, 9, Font.BOLD);
        for (EntLineaReporte vlo_Linea : pvo_Bloque.getLineas()) {
            this.AgregarLinea(vlo_Tabla, vlo_Linea, vlo_LsGrupos.size(), pvo_LsColumnas,
                    pvb_MuestraMoneda, pvo_FuenteTexto);
        }
        for (EntLineaReporte vlo_Total : this.TotalizarPorMoneda(pvo_Bloque.getLineas())) {
            this.AgregarLinea(vlo_Tabla, vlo_Total, vlo_LsGrupos.size(), pvo_LsColumnas,
                    pvb_MuestraMoneda, vlo_FuenteTotal);
        }
        pvo_Documento.add(vlo_Tabla);
    }

    private void AgregarLinea(
            PdfPTable pvo_Tabla,
            EntLineaReporte pvo_Linea,
            int pvn_ColumnasDeGrupo,
            List<ColumnaTotal> pvo_LsColumnas,
            boolean pvb_MuestraMoneda,
            Font pvo_Fuente) {

        for (String vlo_Valor : this.ValoresDeGrupo(pvo_Linea, pvn_ColumnasDeGrupo)) {
            pvo_Tabla.addCell(this.CrearCelda(vlo_Valor, pvo_Fuente));
        }
        if (pvb_MuestraMoneda) {
            pvo_Tabla.addCell(this.CrearCelda(pvo_Linea.getMoneda(), pvo_Fuente));
        }
        for (ColumnaTotal vlo_Columna : pvo_LsColumnas) {
            pvo_Tabla.addCell(this.CrearCelda(vlo_Columna.valor().apply(pvo_Linea), pvo_Fuente));
        }
    }

    private void AgregarTramites(
            Document pvo_Documento,
            List<EntTramiteReporte> pvo_LsTramites,
            Font pvo_FuenteSubtitulo,
            Font pvo_FuenteTexto) throws DocumentException {

        List<String> vlo_LsColumnas = List.of("Referencia", "Tipo de trámite", "Régimen aduanero",
                "Partido responsable", "CIF", "Estado");

        pvo_Documento.add(new Paragraph("Trámites incluidos", pvo_FuenteSubtitulo));
        PdfPTable vlo_Tabla = new PdfPTable(vlo_LsColumnas.size());
        vlo_Tabla.setWidthPercentage(100);
        vlo_Tabla.setSpacingBefore(4);
        vlo_Tabla.setSpacingAfter(10);

        for (String vlo_Columna : vlo_LsColumnas) {
            vlo_Tabla.addCell(this.CrearEncabezado(vlo_Columna));
        }

        for (EntTramiteReporte vlo_Tramite : pvo_LsTramites) {
            vlo_Tabla.addCell(this.CrearCelda(vlo_Tramite.getReferenciaInterna(), pvo_FuenteTexto));
            vlo_Tabla.addCell(this.CrearCelda(vlo_Tramite.getTipoTramite(), pvo_FuenteTexto));
            vlo_Tabla.addCell(this.CrearCelda(vlo_Tramite.getRegimenAduanero(), pvo_FuenteTexto));
            vlo_Tabla.addCell(this.CrearCelda(vlo_Tramite.getPartidoResponsable(), pvo_FuenteTexto));
            vlo_Tabla.addCell(this.CrearCelda(vlo_Tramite.getValorCif(), pvo_FuenteTexto));
            vlo_Tabla.addCell(this.CrearCelda(vlo_Tramite.getEstado(), pvo_FuenteTexto));
        }
        pvo_Documento.add(vlo_Tabla);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Escribe el encabezado institucional del reporte: el logo de la Universidad
     *   junto al nombre de la oficina y de la unidad, y el título del reporte. Si el logo no
     *   estuviera disponible, el encabezado se escribe igual, solo con el texto.
     *
     * @param pvo_Documento documento en construcción
     * @param pvo_FuenteTitulo fuente del título del reporte
     * @param pvo_FuenteTexto fuente del texto del encabezado
     * @throws DocumentException si el documento no admite el encabezado
     */
    private void AgregarEncabezadoInstitucional(
            Document pvo_Documento,
            Font pvo_FuenteTitulo,
            Font pvo_FuenteTexto) throws DocumentException {

        PdfPTable vlo_Encabezado = new PdfPTable(2);
        vlo_Encabezado.setWidthPercentage(100);
        vlo_Encabezado.setWidths(new float[] {18, 82});
        vlo_Encabezado.setSpacingAfter(10);

        PdfPCell vlo_CeldaLogo = new PdfPCell();
        vlo_CeldaLogo.setBorder(0);
        vlo_CeldaLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Image vlo_Logo = this.LeerLogoInstitucional();
        if (vlo_Logo != null) {
            vlo_Logo.scaleToFit(90, 45);
            vlo_CeldaLogo.addElement(vlo_Logo);
        }
        vlo_Encabezado.addCell(vlo_CeldaLogo);

        Font vlo_FuenteOficina = new Font(Font.HELVETICA, 10, Font.BOLD);
        PdfPCell vlo_CeldaTexto = new PdfPCell();
        vlo_CeldaTexto.setBorder(0);
        vlo_CeldaTexto.setVerticalAlignment(Element.ALIGN_MIDDLE);
        vlo_CeldaTexto.addElement(new Paragraph(Constantes.ENCABEZADO_UNIVERSIDAD, vlo_FuenteOficina));
        vlo_CeldaTexto.addElement(new Paragraph(
                Constantes.ENCABEZADO_OFICINA + " · " + Constantes.ENCABEZADO_UNIDAD, pvo_FuenteTexto));
        vlo_CeldaTexto.addElement(new Paragraph("Reporte de consultas internas", pvo_FuenteTitulo));
        vlo_Encabezado.addCell(vlo_CeldaTexto);

        pvo_Documento.add(vlo_Encabezado);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Lee el logo institucional que acompaña a la aplicación. Si no se pudiera leer,
     *   el reporte se genera igual sin él, porque el archivo no debe dejar de entregarse por un
     *   asunto de presentación.
     *
     * @return logo listo para el documento o null si no se pudo leer
     */
    private Image LeerLogoInstitucional() {
        try (InputStream vlo_Recurso = this.getClass().getClassLoader()
                .getResourceAsStream(Constantes.RUTA_LOGO_INSTITUCIONAL)) {
            return vlo_Recurso == null ? null : Image.getInstance(vlo_Recurso.readAllBytes());
        } catch (Exception vlo_Excepcion) {
            return null;
        }
    }

    private PdfPCell CrearEncabezado(String pvc_Texto) {
        Font vlo_Fuente = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        PdfPCell vlo_Celda = new PdfPCell(new Phrase(pvc_Texto, vlo_Fuente));
        vlo_Celda.setBackgroundColor(COLOR_ENCABEZADO);
        vlo_Celda.setPadding(4);
        return vlo_Celda;
    }

    private PdfPCell CrearCelda(Object pvo_Valor, Font pvo_Fuente) {
        String vlc_Texto;
        if (pvo_Valor == null) {
            vlc_Texto = "—";
        } else if (pvo_Valor instanceof BigDecimal vlo_Monto) {
            vlc_Texto = FORMATO_MONTO.format(vlo_Monto);
        } else {
            vlc_Texto = pvo_Valor.toString();
        }
        PdfPCell vlo_Celda = new PdfPCell(new Phrase(vlc_Texto, pvo_Fuente));
        vlo_Celda.setPadding(3);
        if (pvo_Valor instanceof Number) {
            vlo_Celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
        return vlo_Celda;
    }
}
