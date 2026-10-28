package cr.ac.ucr.sim.reportes.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntBloqueReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntLineaReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntReporteConsultas;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 05/10/2026
 * Descripción: Base de los archivos descargables del reporte de consultas internas (HU-036).
 *   Arma las tablas igual que la pantalla: las mismas columnas de agrupación, una por criterio
 *   cuando se piden combinados, la moneda solo cuando hay más de una, las columnas de totales
 *   que la persona usuaria pidió incluir y la fila de totales por moneda al cierre de cada
 *   tabla. Así el archivo descargado dice exactamente lo mismo que se consultó.
 *
 * Historial de cambios:
 *   05/10/2026 Katherine Susana Valverde Fallas: creación de la clase.
 *   05/10/2026 Katherine Susana Valverde Fallas: las tablas se arman igual que en la pantalla.
 */
public abstract class Rpt_SIM_ReporteConsultas {

    protected static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Texto con el que se presenta la fila de totales de cada tabla. */
    protected static final String TITULO_TOTAL = "Total";

    /**
     * Columna de totales de una tabla: su título y de dónde sale su valor.
     */
    protected record ColumnaTotal(String titulo, Function<EntLineaReporte, Object> valor) {
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Genera el archivo del reporte en el formato de la clase.
     *
     * @param pvo_Reporte reporte ya calculado
     * @return contenido del archivo
     */
    public abstract byte[] Generar(EntReporteConsultas pvo_Reporte);

    /** Código con el que la persona usuaria pide este formato. */
    public abstract String getFormato();

    /** Extensión del archivo que se descarga. */
    public abstract String getExtension();

    /** Tipo de contenido con el que se entrega el archivo. */
    public abstract String getTipoContenido();

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Indica si las tablas llevan la columna de moneda. Igual que en la pantalla,
     *   solo aparece cuando el reporte reúne más de una moneda.
     *
     * @param pvo_Reporte reporte ya calculado
     * @return true si hay más de una moneda
     */
    protected boolean MuestraMoneda(EntReporteConsultas pvo_Reporte) {
        return pvo_Reporte.getBloques().stream()
                .flatMap(vlo_Bloque -> vlo_Bloque.getLineas().stream())
                .map(EntLineaReporte::getMoneda)
                .distinct()
                .count() > 1;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Arma las columnas de agrupación de una tabla: una por criterio cuando los
     *   criterios se combinaron, o una sola cuando el desglose es de un criterio.
     *
     * @param pvo_Bloque desglose que se va a escribir
     * @return títulos de las columnas de agrupación
     */
    protected List<String> ColumnasDeGrupo(EntBloqueReporte pvo_Bloque) {
        return pvo_Bloque.getEncabezados().isEmpty() ? List.of("Grupo") : pvo_Bloque.getEncabezados();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Arma las columnas de totales que pidió la persona usuaria, en el mismo orden
     *   en el que se leen en la pantalla.
     *
     * @param pvo_Reporte reporte ya calculado
     * @return columnas de totales de la tabla
     */
    protected List<ColumnaTotal> ColumnasDeTotales(EntReporteConsultas pvo_Reporte) {
        List<ColumnaTotal> vlo_LsColumnas = new ArrayList<>();
        vlo_LsColumnas.add(new ColumnaTotal("Trámites", EntLineaReporte::getCantidadTramites));
        if (pvo_Reporte.isIncluirTotalCif()) {
            vlo_LsColumnas.add(new ColumnaTotal("Total CIF",
                    vlo_Linea -> this.Monto(vlo_Linea.getTotalCif())));
        }
        vlo_LsColumnas.add(new ColumnaTotal("Impuestos pagados",
                vlo_Linea -> this.Monto(vlo_Linea.getTotalImpuestosPagados())));
        vlo_LsColumnas.add(new ColumnaTotal("Exonerados",
                vlo_Linea -> this.Monto(vlo_Linea.getTotalImpuestosExonerados())));
        if (pvo_Reporte.isIncluirTotalBodegaje()) {
            vlo_LsColumnas.add(new ColumnaTotal("Bodegajes (₡)",
                    vlo_Linea -> this.Monto(vlo_Linea.getTotalBodegaje())));
        }
        vlo_LsColumnas.add(new ColumnaTotal("Permisos", EntLineaReporte::getCantidadPermisos));
        return vlo_LsColumnas;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Entrega los valores de las columnas de agrupación de una línea.
     *
     * @param pvo_Linea línea del desglose
     * @param pvn_Columnas cantidad de columnas de agrupación
     * @return valores de la línea, uno por columna
     */
    protected List<String> ValoresDeGrupo(EntLineaReporte pvo_Linea, int pvn_Columnas) {
        if (pvo_Linea.getValores().size() == pvn_Columnas) {
            return pvo_Linea.getValores();
        }
        List<String> vlo_LsValores = new ArrayList<>();
        vlo_LsValores.add(pvo_Linea.getGrupo());
        while (vlo_LsValores.size() < pvn_Columnas) {
            vlo_LsValores.add("");
        }
        return vlo_LsValores;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Suma las líneas de un desglose por moneda, igual que la fila de totales de la
     *   pantalla. Las monedas no se suman entre sí porque el sistema no registra tipo de cambio.
     *
     * @param pvo_LsLineas líneas del desglose
     * @return un total por cada moneda presente
     */
    protected List<EntLineaReporte> TotalizarPorMoneda(List<EntLineaReporte> pvo_LsLineas) {
        Map<String, EntLineaReporte> vlo_MpTotales = new LinkedHashMap<>();
        for (EntLineaReporte vlo_Linea : pvo_LsLineas) {
            EntLineaReporte vlo_Total = vlo_MpTotales.computeIfAbsent(vlo_Linea.getMoneda(), vlc_Moneda -> {
                EntLineaReporte vlo_Nuevo = new EntLineaReporte();
                vlo_Nuevo.setGrupo(TITULO_TOTAL);
                vlo_Nuevo.setMoneda(vlc_Moneda);
                vlo_Nuevo.setTotalCif(BigDecimal.ZERO);
                vlo_Nuevo.setTotalImpuestosPagados(BigDecimal.ZERO);
                vlo_Nuevo.setTotalImpuestosExonerados(BigDecimal.ZERO);
                vlo_Nuevo.setTotalBodegaje(BigDecimal.ZERO);
                return vlo_Nuevo;
            });
            vlo_Total.setCantidadTramites(vlo_Total.getCantidadTramites() + vlo_Linea.getCantidadTramites());
            vlo_Total.setCantidadPermisos(vlo_Total.getCantidadPermisos() + vlo_Linea.getCantidadPermisos());
            vlo_Total.setTotalCif(vlo_Total.getTotalCif().add(this.Monto(vlo_Linea.getTotalCif())));
            vlo_Total.setTotalImpuestosPagados(vlo_Total.getTotalImpuestosPagados()
                    .add(this.Monto(vlo_Linea.getTotalImpuestosPagados())));
            vlo_Total.setTotalImpuestosExonerados(vlo_Total.getTotalImpuestosExonerados()
                    .add(this.Monto(vlo_Linea.getTotalImpuestosExonerados())));
            vlo_Total.setTotalBodegaje(vlo_Total.getTotalBodegaje()
                    .add(this.Monto(vlo_Linea.getTotalBodegaje())));
        }
        return new ArrayList<>(vlo_MpTotales.values());
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Entrega el monto listo para escribirse, tomando el cero cuando el expediente
     *   todavía no declara ese dato.
     *
     * @param pvn_Monto monto de la línea
     * @return monto, nunca nulo
     */
    protected BigDecimal Monto(BigDecimal pvn_Monto) {
        return pvn_Monto == null ? BigDecimal.ZERO : pvn_Monto;
    }
}
