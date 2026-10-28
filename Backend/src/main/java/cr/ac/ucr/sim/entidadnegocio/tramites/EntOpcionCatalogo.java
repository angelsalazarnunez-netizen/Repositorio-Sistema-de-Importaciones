package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Opción de un filtro cuyos valores no provienen de una tabla de catálogo, como las
 *   categorías de estado o los tipos de exoneración (HU-034). No representa una tabla.
 */
public class EntOpcionCatalogo {

    private final String vgc_Codigo;
    private final String vgc_Nombre;

    public EntOpcionCatalogo(String pvc_Codigo, String pvc_Nombre) {
        this.vgc_Codigo = pvc_Codigo;
        this.vgc_Nombre = pvc_Nombre;
    }

    @JsonProperty("codigo")
    public String getCodigo() {
        return this.vgc_Codigo;
    }

    @JsonProperty("nombre")
    public String getNombre() {
        return this.vgc_Nombre;
    }
}
