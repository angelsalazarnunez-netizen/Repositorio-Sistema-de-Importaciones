package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Contenido del listado de trámites que se exporta (HU-034). No representa una tabla.
 *   Reúne en un solo objeto los criterios aplicados, las columnas y las filas de la búsqueda, de
 *   modo que Excel, PDF y Word se generen exactamente con los mismos datos.
 */
public class EntListadoTramites {

    private final String vgc_Titulo;
    private final LocalDateTime vgd_FechaGeneracion;
    private final List<String> vgo_LsCriteriosAplicados;
    private final List<EntColumnaBusqueda> vgo_LsColumnas;
    private final List<EntResultadoBusquedaTramite> vgo_LsResultados;

    public EntListadoTramites(
            String pvc_Titulo,
            LocalDateTime pvd_FechaGeneracion,
            List<String> pvo_LsCriteriosAplicados,
            List<EntColumnaBusqueda> pvo_LsColumnas,
            List<EntResultadoBusquedaTramite> pvo_LsResultados) {
        this.vgc_Titulo = pvc_Titulo;
        this.vgd_FechaGeneracion = pvd_FechaGeneracion;
        this.vgo_LsCriteriosAplicados = pvo_LsCriteriosAplicados;
        this.vgo_LsColumnas = pvo_LsColumnas;
        this.vgo_LsResultados = pvo_LsResultados;
    }

    public String getTitulo() {
        return this.vgc_Titulo;
    }

    public LocalDateTime getFechaGeneracion() {
        return this.vgd_FechaGeneracion;
    }

    public List<String> getCriteriosAplicados() {
        return this.vgo_LsCriteriosAplicados;
    }

    public List<EntColumnaBusqueda> getColumnas() {
        return this.vgo_LsColumnas;
    }

    public List<EntResultadoBusquedaTramite> getResultados() {
        return this.vgo_LsResultados;
    }
}
