package cr.ac.ucr.sim.reportes.tramites;

import cr.ac.ucr.sim.utilerias.Constantes;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 05/10/2026
 * Descripción: Pie de página de los reportes en PDF. Repite en cada página la oficina que emite
 *   el documento y el número de página, de modo que una hoja suelta del reporte siga diciendo de
 *   dónde salió.
 *
 * Historial de cambios:
 *   05/10/2026 Katherine Susana Valverde Fallas: creación de la clase.
 */
public class Rpt_SIM_PieInstitucional extends PdfPageEventHelper {

    private static final Font FUENTE_PIE = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.DARK_GRAY);

    @Override
    public void onEndPage(PdfWriter pvo_Escritor, Document pvo_Documento) {
        String vlc_Oficina = Constantes.ENCABEZADO_UNIVERSIDAD + " · " + Constantes.ENCABEZADO_OFICINA
                + " · " + Constantes.ENCABEZADO_UNIDAD;

        ColumnText.showTextAligned(pvo_Escritor.getDirectContent(), Element.ALIGN_LEFT,
                new Phrase(vlc_Oficina, FUENTE_PIE),
                pvo_Documento.left(), pvo_Documento.bottom() - 12, 0);
        ColumnText.showTextAligned(pvo_Escritor.getDirectContent(), Element.ALIGN_RIGHT,
                new Phrase("Página " + pvo_Escritor.getPageNumber(), FUENTE_PIE),
                pvo_Documento.right(), pvo_Documento.bottom() - 12, 0);
    }
}
