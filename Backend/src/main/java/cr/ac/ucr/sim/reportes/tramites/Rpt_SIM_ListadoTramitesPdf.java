package cr.ac.ucr.sim.reportes.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntColumnaBusqueda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntListadoTramites;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResultadoBusquedaTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Reporte del listado de trámites en PDF (HU-034). Usa página horizontal para que
 *   quepan todas las columnas y repite el encabezado de la tabla en cada página.
 */
@Component
public class Rpt_SIM_ListadoTramitesPdf extends Rpt_SIM_ListadoTramites {

    private static final Color COLOR_ENCABEZADO = new Color(0x12, 0x39, 0x5f);
    private static final Color COLOR_NO_REGISTRADO = new Color(0x6c, 0x75, 0x7d);

    @Override
    public String getFormato() {
        return Constantes.FORMATO_EXPORTACION_PDF;
    }

    @Override
    public String getNombreFormato() {
        return "PDF";
    }

    @Override
    public String getExtension() {
        return "pdf";
    }

    @Override
    public String getTipoContenido() {
        return "application/pdf";
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Genera el documento con el título, los criterios aplicados y la tabla de trámites.
     *
     * @param pvo_Listado criterios, columnas y resultados de la búsqueda
     * @return contenido del archivo .pdf
     */
    @Override
    public byte[] Generar(EntListadoTramites pvo_Listado) {
        ByteArrayOutputStream vlo_Salida = new ByteArrayOutputStream();
        Document vlo_Documento = new Document(PageSize.LETTER.rotate(), 24, 24, 28, 28);
        try {
            PdfWriter.getInstance(vlo_Documento, vlo_Salida);
            vlo_Documento.open();

            vlo_Documento.add(new Paragraph(pvo_Listado.getTitulo(), new Font(Font.HELVETICA, 14, Font.BOLD)));
            Font vlo_FuenteDetalle = new Font(Font.HELVETICA, 8);
            vlo_Documento.add(new Paragraph(this.FormatearFechaGeneracion(pvo_Listado), vlo_FuenteDetalle));
            for (String vlc_Criterio : pvo_Listado.getCriteriosAplicados()) {
                vlo_Documento.add(new Paragraph(vlc_Criterio, vlo_FuenteDetalle));
            }

            List<EntColumnaBusqueda> vlo_LsColumnas = pvo_Listado.getColumnas();
            PdfPTable vlo_Tabla = new PdfPTable(vlo_LsColumnas.size());
            vlo_Tabla.setWidthPercentage(100);
            vlo_Tabla.setWidths(this.CalcularAnchos(vlo_LsColumnas));
            vlo_Tabla.setSpacingBefore(8);
            vlo_Tabla.setHeaderRows(1);

            Font vlo_FuenteEncabezado = new Font(Font.HELVETICA, 6.5f, Font.BOLD, Color.WHITE);
            for (EntColumnaBusqueda vlo_Columna : vlo_LsColumnas) {
                PdfPCell vlo_Celda = new PdfPCell(new Phrase(vlo_Columna.getTitulo(), vlo_FuenteEncabezado));
                vlo_Celda.setBackgroundColor(COLOR_ENCABEZADO);
                vlo_Celda.setPadding(3);
                vlo_Tabla.addCell(vlo_Celda);
            }

            Font vlo_FuenteDato = new Font(Font.HELVETICA, 6.5f);
            Font vlo_FuenteNoRegistrado = new Font(Font.HELVETICA, 6f, Font.ITALIC, COLOR_NO_REGISTRADO);
            for (EntResultadoBusquedaTramite vlo_Resultado : pvo_Listado.getResultados()) {
                for (EntColumnaBusqueda vlo_Columna : vlo_LsColumnas) {
                    PdfPCell vlo_Celda = new PdfPCell(new Phrase(
                            this.FormatearValor(vlo_Resultado, vlo_Columna),
                            vlo_Columna.isDisponible() ? vlo_FuenteDato : vlo_FuenteNoRegistrado));
                    vlo_Celda.setPadding(3);
                    if (vlo_Columna.isDisponible()
                            && EntColumnaBusqueda.TIPO_DATO_MONTO.equals(vlo_Columna.getTipoDato())) {
                        vlo_Celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    }
                    vlo_Tabla.addCell(vlo_Celda);
                }
            }
            if (pvo_Listado.getResultados().isEmpty()) {
                PdfPCell vlo_Celda = new PdfPCell(new Phrase(
                        "No hay trámites que coincidan con los criterios aplicados.", vlo_FuenteDato));
                vlo_Celda.setColspan(vlo_LsColumnas.size());
                vlo_Celda.setPadding(6);
                vlo_Tabla.addCell(vlo_Celda);
            }

            vlo_Documento.add(vlo_Tabla);
            vlo_Documento.close();
            return vlo_Salida.toByteArray();
        } catch (DocumentException vlo_Excepcion) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_LISTADO_NO_GENERADO);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Calcula el ancho relativo de cada columna según el largo habitual de su contenido,
     *   para que los títulos y los nombres no se partan a mitad de palabra.
     *
     * @param pvo_LsColumnas columnas del listado
     * @return ancho relativo de cada columna
     */
    private float[] CalcularAnchos(List<EntColumnaBusqueda> pvo_LsColumnas) {
        Map<String, Float> vlo_MpAnchos = Map.of(
                COLUMNA_REFERENCIA_INTERNA, 1.25f,
                COLUMNA_FECHA_DESALMACENAJE, 1.4f,
                COLUMNA_TIPO_TRAMITE, 1.35f,
                COLUMNA_REGIMEN_ADUANERO, 1.25f,
                COLUMNA_USUARIO, 1.35f,
                COLUMNA_ESTADO, 1.5f,
                COLUMNA_IMPUESTOS_EXONERADOS, 1.2f,
                COLUMNA_TIPO_EXONERACION, 1.25f,
                COLUMNA_MONEDA, 0.8f);
        float[] vlo_Anchos = new float[pvo_LsColumnas.size()];
        for (int vln_Columna = 0; vln_Columna < pvo_LsColumnas.size(); vln_Columna++) {
            vlo_Anchos[vln_Columna] = vlo_MpAnchos.getOrDefault(pvo_LsColumnas.get(vln_Columna).getCodigo(), 1.05f);
        }
        return vlo_Anchos;
    }
}
