package cr.ac.ucr.sim.reportes.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntColumnaBusqueda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntListadoTramites;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResultadoBusquedaTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Reporte del listado de trámites en Excel (.xlsx) (HU-034). Las fechas y los montos
 *   se guardan como valores de fecha y número, de modo que la persona usuaria pueda ordenarlos,
 *   filtrarlos y sumarlos en la hoja.
 */
@Component
public class Rpt_SIM_ListadoTramitesExcel extends Rpt_SIM_ListadoTramites {

    private static final String NOMBRE_HOJA = "Trámites";

    @Override
    public String getFormato() {
        return Constantes.FORMATO_EXPORTACION_EXCEL;
    }

    @Override
    public String getNombreFormato() {
        return "Excel";
    }

    @Override
    public String getExtension() {
        return "xlsx";
    }

    @Override
    public String getTipoContenido() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Genera la hoja con el título, los criterios aplicados, el encabezado con
     *   autofiltro y una fila por trámite.
     *
     * @param pvo_Listado criterios, columnas y resultados de la búsqueda
     * @return contenido del archivo .xlsx
     */
    @Override
    public byte[] Generar(EntListadoTramites pvo_Listado) {
        try (XSSFWorkbook vlo_Libro = new XSSFWorkbook();
             ByteArrayOutputStream vlo_Salida = new ByteArrayOutputStream()) {
            Sheet vlo_Hoja = vlo_Libro.createSheet(NOMBRE_HOJA);
            List<EntColumnaBusqueda> vlo_LsColumnas = pvo_Listado.getColumnas();
            CellStyle vlo_EstiloTitulo = this.CrearEstiloTitulo(vlo_Libro);
            CellStyle vlo_EstiloEncabezado = this.CrearEstiloEncabezado(vlo_Libro);
            CellStyle vlo_EstiloFecha = vlo_Libro.createCellStyle();
            vlo_EstiloFecha.setDataFormat(vlo_Libro.createDataFormat().getFormat("dd/mm/yyyy"));
            CellStyle vlo_EstiloMonto = vlo_Libro.createCellStyle();
            vlo_EstiloMonto.setDataFormat(vlo_Libro.createDataFormat().getFormat("#,##0.00"));
            CellStyle vlo_EstiloNoRegistrado = this.CrearEstiloNoRegistrado(vlo_Libro);

            int vln_Fila = 0;
            Row vlo_FilaTitulo = vlo_Hoja.createRow(vln_Fila++);
            Cell vlo_CeldaTitulo = vlo_FilaTitulo.createCell(0);
            vlo_CeldaTitulo.setCellValue(pvo_Listado.getTitulo());
            vlo_CeldaTitulo.setCellStyle(vlo_EstiloTitulo);
            vlo_Hoja.createRow(vln_Fila++).createCell(0).setCellValue(this.FormatearFechaGeneracion(pvo_Listado));
            for (String vlc_Criterio : pvo_Listado.getCriteriosAplicados()) {
                vlo_Hoja.createRow(vln_Fila++).createCell(0).setCellValue(vlc_Criterio);
            }
            vln_Fila++;

            int vln_FilaEncabezado = vln_Fila;
            Row vlo_Encabezado = vlo_Hoja.createRow(vln_Fila++);
            for (int vln_Columna = 0; vln_Columna < vlo_LsColumnas.size(); vln_Columna++) {
                Cell vlo_Celda = vlo_Encabezado.createCell(vln_Columna);
                vlo_Celda.setCellValue(vlo_LsColumnas.get(vln_Columna).getTitulo());
                vlo_Celda.setCellStyle(vlo_EstiloEncabezado);
            }

            for (EntResultadoBusquedaTramite vlo_Resultado : pvo_Listado.getResultados()) {
                Row vlo_FilaDatos = vlo_Hoja.createRow(vln_Fila++);
                for (int vln_Columna = 0; vln_Columna < vlo_LsColumnas.size(); vln_Columna++) {
                    EntColumnaBusqueda vlo_Columna = vlo_LsColumnas.get(vln_Columna);
                    Cell vlo_Celda = vlo_FilaDatos.createCell(vln_Columna);
                    Object vlo_Valor = this.ObtenerValor(vlo_Resultado, vlo_Columna);
                    if (!vlo_Columna.isDisponible()) {
                        vlo_Celda.setCellValue(Constantes.TEXTO_NO_REGISTRADO_AUN);
                        vlo_Celda.setCellStyle(vlo_EstiloNoRegistrado);
                    } else if (vlo_Valor instanceof LocalDateTime vld_Fecha) {
                        vlo_Celda.setCellValue(vld_Fecha);
                        vlo_Celda.setCellStyle(vlo_EstiloFecha);
                    } else if (vlo_Valor instanceof BigDecimal vln_Monto) {
                        vlo_Celda.setCellValue(vln_Monto.doubleValue());
                        vlo_Celda.setCellStyle(vlo_EstiloMonto);
                    } else {
                        vlo_Celda.setCellValue(this.FormatearValor(vlo_Resultado, vlo_Columna));
                    }
                }
            }

            if (!pvo_Listado.getResultados().isEmpty()) {
                vlo_Hoja.setAutoFilter(new CellRangeAddress(
                        vln_FilaEncabezado, vln_Fila - 1, 0, vlo_LsColumnas.size() - 1));
            }
            vlo_Hoja.createFreezePane(0, vln_FilaEncabezado + 1);
            for (int vln_Columna = 0; vln_Columna < vlo_LsColumnas.size(); vln_Columna++) {
                vlo_Hoja.autoSizeColumn(vln_Columna);
            }

            vlo_Libro.write(vlo_Salida);
            return vlo_Salida.toByteArray();
        } catch (IOException vlo_Excepcion) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_LISTADO_NO_GENERADO);
        }
    }

    private CellStyle CrearEstiloTitulo(XSSFWorkbook pvo_Libro) {
        Font vlo_Fuente = pvo_Libro.createFont();
        vlo_Fuente.setBold(true);
        vlo_Fuente.setFontHeightInPoints((short) 14);
        CellStyle vlo_Estilo = pvo_Libro.createCellStyle();
        vlo_Estilo.setFont(vlo_Fuente);
        return vlo_Estilo;
    }

    private CellStyle CrearEstiloEncabezado(XSSFWorkbook pvo_Libro) {
        Font vlo_Fuente = pvo_Libro.createFont();
        vlo_Fuente.setBold(true);
        vlo_Fuente.setColor(IndexedColors.WHITE.getIndex());
        CellStyle vlo_Estilo = pvo_Libro.createCellStyle();
        vlo_Estilo.setFont(vlo_Fuente);
        vlo_Estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        vlo_Estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return vlo_Estilo;
    }

    private CellStyle CrearEstiloNoRegistrado(XSSFWorkbook pvo_Libro) {
        Font vlo_Fuente = pvo_Libro.createFont();
        vlo_Fuente.setItalic(true);
        vlo_Fuente.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        CellStyle vlo_Estilo = pvo_Libro.createCellStyle();
        vlo_Estilo.setFont(vlo_Fuente);
        return vlo_Estilo;
    }
}
