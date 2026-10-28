package cr.ac.ucr.sim.reportes.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntColumnaBusqueda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntListadoTramites;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResultadoBusquedaTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Reporte del listado de trámites en Word (.docx) (HU-034). Usa página horizontal
 *   tamaño carta y repite el encabezado de la tabla en cada página.
 */
@Component
public class Rpt_SIM_ListadoTramitesWord extends Rpt_SIM_ListadoTramites {

    // Medidas de Word en veinteavos de punto: carta horizontal de 11 x 8,5 pulgadas y márgenes de 0,5.
    private static final BigInteger ANCHO_PAGINA = BigInteger.valueOf(15840);
    private static final BigInteger ALTO_PAGINA = BigInteger.valueOf(12240);
    private static final BigInteger MARGEN = BigInteger.valueOf(720);
    private static final String COLOR_ENCABEZADO = "12395F";
    private static final String COLOR_NO_REGISTRADO = "6C757D";
    private static final int TAMANO_FUENTE_TABLA = 7;

    @Override
    public String getFormato() {
        return Constantes.FORMATO_EXPORTACION_WORD;
    }

    @Override
    public String getNombreFormato() {
        return "Word";
    }

    @Override
    public String getExtension() {
        return "docx";
    }

    @Override
    public String getTipoContenido() {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Genera el documento con el título, los criterios aplicados y la tabla de trámites.
     *
     * @param pvo_Listado criterios, columnas y resultados de la búsqueda
     * @return contenido del archivo .docx
     */
    @Override
    public byte[] Generar(EntListadoTramites pvo_Listado) {
        try (XWPFDocument vlo_Documento = new XWPFDocument();
             ByteArrayOutputStream vlo_Salida = new ByteArrayOutputStream()) {
            this.ConfigurarPagina(vlo_Documento);

            XWPFRun vlo_Titulo = vlo_Documento.createParagraph().createRun();
            vlo_Titulo.setText(pvo_Listado.getTitulo());
            vlo_Titulo.setBold(true);
            vlo_Titulo.setFontSize(14);
            this.AgregarDetalle(vlo_Documento, this.FormatearFechaGeneracion(pvo_Listado));
            for (String vlc_Criterio : pvo_Listado.getCriteriosAplicados()) {
                this.AgregarDetalle(vlo_Documento, vlc_Criterio);
            }

            List<EntColumnaBusqueda> vlo_LsColumnas = pvo_Listado.getColumnas();
            int vln_Filas = 1 + Math.max(1, pvo_Listado.getResultados().size());
            XWPFTable vlo_Tabla = vlo_Documento.createTable(vln_Filas, vlo_LsColumnas.size());
            vlo_Tabla.setWidth("100%");

            XWPFTableRow vlo_Encabezado = vlo_Tabla.getRow(0);
            vlo_Encabezado.setRepeatHeader(true);
            for (int vln_Columna = 0; vln_Columna < vlo_LsColumnas.size(); vln_Columna++) {
                XWPFTableCell vlo_Celda = vlo_Encabezado.getCell(vln_Columna);
                vlo_Celda.setColor(COLOR_ENCABEZADO);
                XWPFRun vlo_Texto = this.EscribirCelda(vlo_Celda, vlo_LsColumnas.get(vln_Columna).getTitulo());
                vlo_Texto.setBold(true);
                vlo_Texto.setColor("FFFFFF");
            }

            List<EntResultadoBusquedaTramite> vlo_LsResultados = pvo_Listado.getResultados();
            if (vlo_LsResultados.isEmpty()) {
                XWPFTableRow vlo_FilaVacia = vlo_Tabla.getRow(1);
                this.EscribirCelda(vlo_FilaVacia.getCell(0),
                        "No hay trámites que coincidan con los criterios aplicados.");
            }
            for (int vln_Fila = 0; vln_Fila < vlo_LsResultados.size(); vln_Fila++) {
                XWPFTableRow vlo_FilaDatos = vlo_Tabla.getRow(vln_Fila + 1);
                for (int vln_Columna = 0; vln_Columna < vlo_LsColumnas.size(); vln_Columna++) {
                    EntColumnaBusqueda vlo_Columna = vlo_LsColumnas.get(vln_Columna);
                    XWPFTableCell vlo_Celda = vlo_FilaDatos.getCell(vln_Columna);
                    XWPFRun vlo_Texto = this.EscribirCelda(
                            vlo_Celda, this.FormatearValor(vlo_LsResultados.get(vln_Fila), vlo_Columna));
                    if (!vlo_Columna.isDisponible()) {
                        vlo_Texto.setItalic(true);
                        vlo_Texto.setColor(COLOR_NO_REGISTRADO);
                    } else if (EntColumnaBusqueda.TIPO_DATO_MONTO.equals(vlo_Columna.getTipoDato())) {
                        vlo_Celda.getParagraphs().get(0).setAlignment(ParagraphAlignment.RIGHT);
                    }
                }
            }

            vlo_Documento.write(vlo_Salida);
            return vlo_Salida.toByteArray();
        } catch (IOException vlo_Excepcion) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_LISTADO_NO_GENERADO);
        }
    }

    private void ConfigurarPagina(XWPFDocument pvo_Documento) {
        CTSectPr vlo_Seccion = pvo_Documento.getDocument().getBody().addNewSectPr();
        CTPageSz vlo_Tamano = vlo_Seccion.addNewPgSz();
        vlo_Tamano.setOrient(STPageOrientation.LANDSCAPE);
        vlo_Tamano.setW(ANCHO_PAGINA);
        vlo_Tamano.setH(ALTO_PAGINA);
        CTPageMar vlo_Margenes = vlo_Seccion.addNewPgMar();
        vlo_Margenes.setTop(MARGEN);
        vlo_Margenes.setBottom(MARGEN);
        vlo_Margenes.setLeft(MARGEN);
        vlo_Margenes.setRight(MARGEN);
    }

    private void AgregarDetalle(XWPFDocument pvo_Documento, String pvc_Texto) {
        XWPFParagraph vlo_Parrafo = pvo_Documento.createParagraph();
        vlo_Parrafo.setSpacingAfter(0);
        XWPFRun vlo_Texto = vlo_Parrafo.createRun();
        vlo_Texto.setText(pvc_Texto);
        vlo_Texto.setFontSize(9);
    }

    private XWPFRun EscribirCelda(XWPFTableCell pvo_Celda, String pvc_Texto) {
        XWPFParagraph vlo_Parrafo = pvo_Celda.getParagraphs().get(0);
        vlo_Parrafo.setSpacingAfter(0);
        XWPFRun vlo_Texto = vlo_Parrafo.createRun();
        vlo_Texto.setText(pvc_Texto);
        vlo_Texto.setFontSize(TAMANO_FUENTE_TABLA);
        return vlo_Texto;
    }
}
