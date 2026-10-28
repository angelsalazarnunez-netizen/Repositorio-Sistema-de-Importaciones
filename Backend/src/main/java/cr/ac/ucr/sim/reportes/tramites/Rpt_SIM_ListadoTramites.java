package cr.ac.ucr.sim.reportes.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntColumnaBusqueda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntListadoTramites;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResultadoBusquedaTramite;
import cr.ac.ucr.sim.utilerias.Constantes;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Base de los reportes del listado de trámites (HU-034). Define una sola vez las
 *   columnas del listado y el valor de cada columna, de modo que la pantalla y los formatos Excel,
 *   PDF y Word presenten exactamente la misma información. Cada formato solo decide cómo dibujarla.
 *   Para integrar un dato de un módulo futuro (DUA, gastos, despacho, envío de documentos), se marca
 *   su columna como disponible y se agrega su valor en ObtenerValor.
 */
public abstract class Rpt_SIM_ListadoTramites {

    public static final String COLUMNA_REFERENCIA_INTERNA = "referenciaInterna";
    public static final String COLUMNA_FECHA_REGISTRO = "fechaRegistro";
    public static final String COLUMNA_FECHA_ENVIO = "fechaEnvio";
    public static final String COLUMNA_FECHA_DESALMACENAJE = "fechaDesalmacenaje";
    public static final String COLUMNA_TIPO_TRAMITE = "tipoTramite";
    public static final String COLUMNA_REGIMEN_ADUANERO = "regimenAduanero";
    public static final String COLUMNA_USUARIO = "usuario";
    public static final String COLUMNA_ESTADO = "estado";
    public static final String COLUMNA_VALOR_MERCANCIA = "valorMercancia";
    public static final String COLUMNA_IMPUESTOS_PAGADOS = "impuestosPagados";
    public static final String COLUMNA_IMPUESTOS_EXONERADOS = "impuestosExonerados";
    public static final String COLUMNA_TIPO_EXONERACION = "tipoExoneracion";
    public static final String COLUMNA_COSTOS_PERMISOS = "costosPermisos";
    public static final String COLUMNA_MONEDA = "moneda";
    public static final String COLUMNA_TOTAL_CIF = "totalCif";

    private static final List<EntColumnaBusqueda> COLUMNAS = List.of(
            new EntColumnaBusqueda(COLUMNA_REFERENCIA_INTERNA, "Número de trámite", EntColumnaBusqueda.TIPO_DATO_TEXTO, true),
            new EntColumnaBusqueda(COLUMNA_FECHA_REGISTRO, "Fecha de registro", EntColumnaBusqueda.TIPO_DATO_FECHA, true),
            // Envío de documentos: módulo sin implementar.
            new EntColumnaBusqueda(COLUMNA_FECHA_ENVIO, "Fecha de envío", EntColumnaBusqueda.TIPO_DATO_FECHA, false),
            // Despacho: módulo sin implementar.
            new EntColumnaBusqueda(COLUMNA_FECHA_DESALMACENAJE, "Fecha de desalmacenaje", EntColumnaBusqueda.TIPO_DATO_FECHA, false),
            new EntColumnaBusqueda(COLUMNA_TIPO_TRAMITE, "Tipo de trámite", EntColumnaBusqueda.TIPO_DATO_TEXTO, true),
            new EntColumnaBusqueda(COLUMNA_REGIMEN_ADUANERO, "Régimen aduanero", EntColumnaBusqueda.TIPO_DATO_TEXTO, true),
            new EntColumnaBusqueda(COLUMNA_USUARIO, "Usuario", EntColumnaBusqueda.TIPO_DATO_TEXTO, true),
            new EntColumnaBusqueda(COLUMNA_ESTADO, "Estado", EntColumnaBusqueda.TIPO_DATO_TEXTO, true),
            new EntColumnaBusqueda(COLUMNA_VALOR_MERCANCIA, "Valor de mercancía", EntColumnaBusqueda.TIPO_DATO_MONTO, true),
            // DUA e impuestos: módulo sin implementar.
            new EntColumnaBusqueda(COLUMNA_IMPUESTOS_PAGADOS, "Impuestos pagados", EntColumnaBusqueda.TIPO_DATO_MONTO, false),
            new EntColumnaBusqueda(COLUMNA_IMPUESTOS_EXONERADOS, "Impuestos exonerados", EntColumnaBusqueda.TIPO_DATO_MONTO, false),
            new EntColumnaBusqueda(COLUMNA_TIPO_EXONERACION, "Tipo de exoneración", EntColumnaBusqueda.TIPO_DATO_TEXTO, true),
            // Gastos y permisos regulatorios: módulo sin implementar.
            new EntColumnaBusqueda(COLUMNA_COSTOS_PERMISOS, "Costos de permisos", EntColumnaBusqueda.TIPO_DATO_MONTO, false),
            new EntColumnaBusqueda(COLUMNA_MONEDA, "Moneda", EntColumnaBusqueda.TIPO_DATO_TEXTO, true),
            // DUA: módulo sin implementar.
            new EntColumnaBusqueda(COLUMNA_TOTAL_CIF, "Total CIF", EntColumnaBusqueda.TIPO_DATO_MONTO, false));

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Obtiene las columnas del listado de trámites en el orden en que se presentan.
     *
     * @return columnas del listado
     */
    public static List<EntColumnaBusqueda> ObtenerColumnas() {
        return COLUMNAS;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Código del formato que genera el reporte, por ejemplo EXCEL.
     *
     * @return código del formato
     */
    public abstract String getFormato();

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Nombre del formato para mostrarlo a la persona usuaria.
     *
     * @return nombre del formato
     */
    public abstract String getNombreFormato();

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Extensión del archivo generado, sin el punto.
     *
     * @return extensión del archivo
     */
    public abstract String getExtension();

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Tipo de contenido HTTP del archivo generado.
     *
     * @return tipo de contenido
     */
    public abstract String getTipoContenido();

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Genera el archivo del listado.
     *
     * @param pvo_Listado criterios, columnas y resultados de la búsqueda
     * @return contenido del archivo
     */
    public abstract byte[] Generar(EntListadoTramites pvo_Listado);

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Obtiene el valor de una columna para una fila. Devuelve null cuando el trámite no
     *   tiene el dato o cuando la columna depende de un módulo que todavía no lo registra.
     *
     * @param pvo_Resultado fila del listado
     * @param pvo_Columna columna del listado
     * @return valor de la celda: texto, fecha o monto
     */
    protected Object ObtenerValor(EntResultadoBusquedaTramite pvo_Resultado, EntColumnaBusqueda pvo_Columna) {
        if (!pvo_Columna.isDisponible()) {
            return null;
        }
        return switch (pvo_Columna.getCodigo()) {
            case COLUMNA_REFERENCIA_INTERNA -> pvo_Resultado.getReferenciaInterna();
            case COLUMNA_FECHA_REGISTRO -> pvo_Resultado.getFechaRegistro();
            case COLUMNA_TIPO_TRAMITE -> pvo_Resultado.getTipoTramite();
            case COLUMNA_REGIMEN_ADUANERO -> pvo_Resultado.getRegimenAduanero();
            case COLUMNA_USUARIO -> pvo_Resultado.getUsuario();
            case COLUMNA_ESTADO -> pvo_Resultado.getCategoriaEstadoNombre() + " (" + pvo_Resultado.getEstadoTramite() + ")";
            case COLUMNA_VALOR_MERCANCIA -> pvo_Resultado.getValorMercancia();
            case COLUMNA_TIPO_EXONERACION -> pvo_Resultado.getTipoExoneracionNombre();
            case COLUMNA_MONEDA -> pvo_Resultado.getMoneda();
            default -> null;
        };
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Obtiene el valor de una celda como texto, listo para dibujarse en el reporte.
     *
     * @param pvo_Resultado fila del listado
     * @param pvo_Columna columna del listado
     * @return texto de la celda
     */
    protected String FormatearValor(EntResultadoBusquedaTramite pvo_Resultado, EntColumnaBusqueda pvo_Columna) {
        if (!pvo_Columna.isDisponible()) {
            return Constantes.TEXTO_NO_REGISTRADO_AUN;
        }
        Object vlo_Valor = this.ObtenerValor(pvo_Resultado, pvo_Columna);
        if (vlo_Valor == null || (vlo_Valor instanceof String vlc_Texto && vlc_Texto.isBlank())) {
            return Constantes.TEXTO_SIN_DATO;
        }
        if (vlo_Valor instanceof LocalDateTime vld_Fecha) {
            return vld_Fecha.format(FORMATO_FECHA);
        }
        if (vlo_Valor instanceof BigDecimal vln_Monto) {
            return this.CrearFormatoMonto().format(vln_Monto);
        }
        return vlo_Valor.toString();
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Texto con la fecha y hora de generación del listado.
     *
     * @param pvo_Listado listado exportado
     * @return texto de la fecha de generación
     */
    protected String FormatearFechaGeneracion(EntListadoTramites pvo_Listado) {
        return "Generado el " + pvo_Listado.getFechaGeneracion().format(FORMATO_FECHA_HORA)
                + " · " + pvo_Listado.getResultados().size() + " trámite(s)";
    }

    private DecimalFormat CrearFormatoMonto() {
        DecimalFormatSymbols vlo_Simbolos = new DecimalFormatSymbols(Locale.ROOT);
        vlo_Simbolos.setGroupingSeparator(' ');
        vlo_Simbolos.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.00", vlo_Simbolos);
    }
}
