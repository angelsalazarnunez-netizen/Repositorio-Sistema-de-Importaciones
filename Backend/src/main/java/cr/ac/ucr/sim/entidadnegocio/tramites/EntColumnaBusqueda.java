package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Columna del listado de la búsqueda de trámites (HU-034). No representa una tabla.
 *   La pantalla y los tres formatos de exportación dibujan las mismas columnas a partir de esta
 *   definición. Una columna no disponible es la que depende de un módulo que todavía no registra
 *   su dato: se muestra con el texto "No registrado aún".
 */
public class EntColumnaBusqueda {

    public static final String TIPO_DATO_TEXTO = "TEXTO";
    public static final String TIPO_DATO_FECHA = "FECHA";
    public static final String TIPO_DATO_MONTO = "MONTO";

    private final String vgc_Codigo;
    private final String vgc_Titulo;
    private final String vgc_TipoDato;
    private final boolean vgb_Disponible;

    public EntColumnaBusqueda(String pvc_Codigo, String pvc_Titulo, String pvc_TipoDato, boolean pvb_Disponible) {
        this.vgc_Codigo = pvc_Codigo;
        this.vgc_Titulo = pvc_Titulo;
        this.vgc_TipoDato = pvc_TipoDato;
        this.vgb_Disponible = pvb_Disponible;
    }

    @JsonProperty("codigo")
    public String getCodigo() {
        return this.vgc_Codigo;
    }

    @JsonProperty("titulo")
    public String getTitulo() {
        return this.vgc_Titulo;
    }

    @JsonProperty("tipoDato")
    public String getTipoDato() {
        return this.vgc_TipoDato;
    }

    @JsonProperty("disponible")
    public boolean isDisponible() {
        return this.vgb_Disponible;
    }
}
