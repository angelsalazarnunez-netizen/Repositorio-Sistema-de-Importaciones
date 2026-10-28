package cr.ac.ucr.sim.reportes.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntBloqueReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntLineaReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntReporteConsultas;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramiteReporte;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 05/10/2026
 * Descripción: Reporte de consultas internas en Excel (.xlsx) (HU-036). Escribe las mismas tablas
 *   que muestra la pantalla, con sus columnas de agrupación, sus totales por moneda y los
 *   trámites cuando se pidió enumerarlos. Los montos y los conteos se guardan como números, de
 *   modo que la persona agente pueda sumarlos y ordenarlos en la hoja.
 *
 * Historial de cambios:
 *   05/10/2026 Katherine Susana Valverde Fallas: creación de la clase.
 *   05/10/2026 Katherine Susana Valverde Fallas: las tablas se arman igual que en la pantalla.
 */
@Component
public class Rpt_SIM_ReporteConsultasExcel extends Rpt_SIM_ReporteConsultas {

    private static final String NOMBRE_HOJA = "Reporte";

    @Override
    public String getFormato() {
        return Constantes.FORMATO_EXPORTACION_EXCEL;
    }

    @Override
    public String getExtension() {
        return "xlsx";
    }

    @Override
    public String getTipoContenido() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public byte[] Generar(EntReporteConsultas pvo_Reporte) {
        try (XSSFWorkbook vlo_Libro = new XSSFWorkbook();
             ByteArrayOutputStream vlo_Salida = new ByteArrayOutputStream()) {

            Sheet vlo_Hoja = vlo_Libro.createSheet(NOMBRE_HOJA);
            CellStyle vlo_EstiloTitulo = this.CrearEstiloNegrita(vlo_Libro);
            CellStyle vlo_EstiloEncabezado = this.CrearEstiloEncabezado(vlo_Libro);
            boolean vlb_MuestraMoneda = this.MuestraMoneda(pvo_Reporte);
            List<ColumnaTotal> vlo_LsColumnas = this.ColumnasDeTotales(pvo_Reporte);
            int vln_Fila = 0;

            vln_Fila = this.EscribirTexto(vlo_Hoja, vln_Fila, Constantes.ENCABEZADO_UNIVERSIDAD,
                    vlo_EstiloTitulo);
            vln_Fila = this.EscribirTexto(vlo_Hoja, vln_Fila,
                    Constantes.ENCABEZADO_OFICINA + " · " + Constantes.ENCABEZADO_UNIDAD, null);
            vln_Fila = this.EscribirTexto(vlo_Hoja, vln_Fila, "Reporte de consultas internas",
                    vlo_EstiloTitulo);
            vln_Fila = this.EscribirTexto(vlo_Hoja, vln_Fila, "Generado: "
                    + (pvo_Reporte.getFechaGeneracion() == null
                            ? "" : pvo_Reporte.getFechaGeneracion().format(FORMATO_FECHA_HORA)), null);
            vln_Fila = this.EscribirTexto(vlo_Hoja, vln_Fila,
                    "Trámites: " + pvo_Reporte.getCantidadTramites()
                            + "   ·   Permisos regulatorios: " + pvo_Reporte.getCantidadPermisos(), null);
            vln_Fila++;

            for (EntBloqueReporte vlo_Bloque : pvo_Reporte.getBloques()) {
                vln_Fila = this.EscribirTabla(vlo_Hoja, vln_Fila, vlo_Bloque, vlo_LsColumnas,
                        vlb_MuestraMoneda, vlo_EstiloTitulo, vlo_EstiloEncabezado);
            }

            if (!pvo_Reporte.getTramites().isEmpty()) {
                vln_Fila = this.EscribirTramites(vlo_Hoja, vln_Fila, pvo_Reporte.getTramites(),
                        vlo_EstiloTitulo, vlo_EstiloEncabezado);
            }

            for (String vlo_Nota : pvo_Reporte.getNotas()) {
                vln_Fila = this.EscribirTexto(vlo_Hoja, vln_Fila, vlo_Nota, null);
            }

            for (int vln_Columna = 0; vln_Columna < 12; vln_Columna++) {
                vlo_Hoja.autoSizeColumn(vln_Columna);
            }

            vlo_Libro.write(vlo_Salida);
            return vlo_Salida.toByteArray();
        } catch (IOException vlo_Excepcion) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REPORTE_NO_GENERADO);
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Escribe un desglose con el mismo encabezado, las mismas líneas y la misma fila
     *   de totales que muestra la pantalla.
     *
     * @param pvo_Hoja hoja en construcción
     * @param pvn_Fila fila donde empieza la tabla
     * @param pvo_Bloque desglose que se escribe
     * @param pvo_LsColumnas columnas de totales que se incluyen
     * @param pvb_MuestraMoneda true si la tabla lleva la columna de moneda
     * @param pvo_EstiloTitulo estilo del título de la tabla
     * @param pvo_EstiloEncabezado estilo de la fila de encabezados
     * @return siguiente fila libre de la hoja
     */
    private int EscribirTabla(
            Sheet pvo_Hoja,
            int pvn_Fila,
            EntBloqueReporte pvo_Bloque,
            List<ColumnaTotal> pvo_LsColumnas,
            boolean pvb_MuestraMoneda,
            CellStyle pvo_EstiloTitulo,
            CellStyle pvo_EstiloEncabezado) {

        List<String> vlo_LsGrupos = this.ColumnasDeGrupo(pvo_Bloque);
        int vln_Fila = this.EscribirTexto(pvo_Hoja, pvn_Fila, pvo_Bloque.getTitulo(), pvo_EstiloTitulo);

        List<String> vlo_LsEncabezados = new ArrayList<>(vlo_LsGrupos);
        if (pvb_MuestraMoneda) {
            vlo_LsEncabezados.add("Moneda");
        }
        pvo_LsColumnas.forEach(vlo_Columna -> vlo_LsEncabezados.add(vlo_Columna.titulo()));

        Row vlo_Encabezado = pvo_Hoja.createRow(vln_Fila++);
        for (int vln_Columna = 0; vln_Columna < vlo_LsEncabezados.size(); vln_Columna++) {
            Cell vlo_Celda = vlo_Encabezado.createCell(vln_Columna);
            vlo_Celda.setCellValue(vlo_LsEncabezados.get(vln_Columna));
            vlo_Celda.setCellStyle(pvo_EstiloEncabezado);
        }

        for (EntLineaReporte vlo_Linea : pvo_Bloque.getLineas()) {
            this.EscribirLinea(pvo_Hoja.createRow(vln_Fila++), vlo_Linea, vlo_LsGrupos.size(),
                    pvo_LsColumnas, pvb_MuestraMoneda, null);
        }
        for (EntLineaReporte vlo_Total : this.TotalizarPorMoneda(pvo_Bloque.getLineas())) {
            this.EscribirLinea(pvo_Hoja.createRow(vln_Fila++), vlo_Total, vlo_LsGrupos.size(),
                    pvo_LsColumnas, pvb_MuestraMoneda, pvo_EstiloTitulo);
        }
        return vln_Fila + 1;
    }

    private void EscribirLinea(
            Row pvo_Fila,
            EntLineaReporte pvo_Linea,
            int pvn_ColumnasDeGrupo,
            List<ColumnaTotal> pvo_LsColumnas,
            boolean pvb_MuestraMoneda,
            CellStyle pvo_Estilo) {

        int vln_Columna = 0;
        for (String vlo_Valor : this.ValoresDeGrupo(pvo_Linea, pvn_ColumnasDeGrupo)) {
            this.EscribirValor(pvo_Fila.createCell(vln_Columna++), vlo_Valor, pvo_Estilo);
        }
        if (pvb_MuestraMoneda) {
            this.EscribirValor(pvo_Fila.createCell(vln_Columna++), pvo_Linea.getMoneda(), pvo_Estilo);
        }
        for (ColumnaTotal vlo_Columna : pvo_LsColumnas) {
            this.EscribirValor(pvo_Fila.createCell(vln_Columna++), vlo_Columna.valor().apply(pvo_Linea),
                    pvo_Estilo);
        }
    }

    private int EscribirTramites(
            Sheet pvo_Hoja,
            int pvn_Fila,
            List<EntTramiteReporte> pvo_LsTramites,
            CellStyle pvo_EstiloTitulo,
            CellStyle pvo_EstiloEncabezado) {

        List<String> vlo_LsColumnas = List.of("Referencia", "Tipo de trámite", "Régimen aduanero",
                "Partido responsable", "CIF", "Estado");

        int vln_Fila = this.EscribirTexto(pvo_Hoja, pvn_Fila, "Trámites incluidos", pvo_EstiloTitulo);
        Row vlo_Encabezado = pvo_Hoja.createRow(vln_Fila++);
        for (int vln_Columna = 0; vln_Columna < vlo_LsColumnas.size(); vln_Columna++) {
            Cell vlo_Celda = vlo_Encabezado.createCell(vln_Columna);
            vlo_Celda.setCellValue(vlo_LsColumnas.get(vln_Columna));
            vlo_Celda.setCellStyle(pvo_EstiloEncabezado);
        }

        for (EntTramiteReporte vlo_Tramite : pvo_LsTramites) {
            Row vlo_FilaDatos = pvo_Hoja.createRow(vln_Fila++);
            this.EscribirValor(vlo_FilaDatos.createCell(0), vlo_Tramite.getReferenciaInterna(), null);
            this.EscribirValor(vlo_FilaDatos.createCell(1), vlo_Tramite.getTipoTramite(), null);
            this.EscribirValor(vlo_FilaDatos.createCell(2), vlo_Tramite.getRegimenAduanero(), null);
            this.EscribirValor(vlo_FilaDatos.createCell(3), vlo_Tramite.getPartidoResponsable(), null);
            this.EscribirValor(vlo_FilaDatos.createCell(4), vlo_Tramite.getValorCif(), null);
            this.EscribirValor(vlo_FilaDatos.createCell(5), vlo_Tramite.getEstado(), null);
        }
        return vln_Fila + 1;
    }

    private int EscribirTexto(Sheet pvo_Hoja, int pvn_Fila, String pvc_Texto, CellStyle pvo_Estilo) {
        Row vlo_Fila = pvo_Hoja.createRow(pvn_Fila);
        Cell vlo_Celda = vlo_Fila.createCell(0);
        vlo_Celda.setCellValue(pvc_Texto);
        if (pvo_Estilo != null) {
            vlo_Celda.setCellStyle(pvo_Estilo);
        }
        return pvn_Fila + 1;
    }

    private void EscribirValor(Cell pvo_Celda, Object pvo_Valor, CellStyle pvo_Estilo) {
        if (pvo_Valor == null) {
            pvo_Celda.setBlank();
        } else if (pvo_Valor instanceof BigDecimal vlo_Monto) {
            pvo_Celda.setCellValue(vlo_Monto.doubleValue());
        } else if (pvo_Valor instanceof Number vlo_Numero) {
            pvo_Celda.setCellValue(vlo_Numero.doubleValue());
        } else {
            pvo_Celda.setCellValue(pvo_Valor.toString());
        }
        if (pvo_Estilo != null) {
            pvo_Celda.setCellStyle(pvo_Estilo);
        }
    }

    private CellStyle CrearEstiloNegrita(XSSFWorkbook pvo_Libro) {
        CellStyle vlo_Estilo = pvo_Libro.createCellStyle();
        Font vlo_Fuente = pvo_Libro.createFont();
        vlo_Fuente.setBold(true);
        vlo_Estilo.setFont(vlo_Fuente);
        return vlo_Estilo;
    }

    private CellStyle CrearEstiloEncabezado(XSSFWorkbook pvo_Libro) {
        CellStyle vlo_Estilo = pvo_Libro.createCellStyle();
        Font vlo_Fuente = pvo_Libro.createFont();
        vlo_Fuente.setBold(true);
        vlo_Fuente.setColor(IndexedColors.WHITE.getIndex());
        vlo_Estilo.setFont(vlo_Fuente);
        vlo_Estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        vlo_Estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return vlo_Estilo;
    }
}
