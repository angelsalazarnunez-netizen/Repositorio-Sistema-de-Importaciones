package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 25/09/2026
 * Descripción: Reporte de consultas internas (HU-036). Reúne la cantidad de trámites, los totales
 *   por moneda y los desgloses por tipo de trámite y por régimen aduanero, junto con la lista de
 *   trámites cuando se pide enumerarlos. Las notas advierten qué montos todavía no registra el
 *   sistema, para que el reporte no se lea como si valieran cero.
 */
public class EntReporteConsultas {

    private LocalDateTime vgd_FechaGeneracion;
    private List<EntBloqueReporte> vgo_LsBloques = new ArrayList<>();
    private long vgn_CantidadTramites;
    private boolean vgb_IncluirTotalCif = true;
    private boolean vgb_IncluirTotalBodegaje = true;
    private long vgn_CantidadPermisos;
    private List<EntTramiteReporte> vgo_LsTramites = new ArrayList<>();
    private List<String> vgo_LsNotas = new ArrayList<>();

    @JsonProperty("fechaGeneracion")
    public LocalDateTime getFechaGeneracion() {
        return this.vgd_FechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime pvd_FechaGeneracion) {
        this.vgd_FechaGeneracion = pvd_FechaGeneracion;
    }

    @JsonProperty("bloques")
    public List<EntBloqueReporte> getBloques() {
        return this.vgo_LsBloques;
    }

    public void setBloques(List<EntBloqueReporte> pvo_LsBloques) {
        this.vgo_LsBloques = pvo_LsBloques;
    }

    @JsonProperty("cantidadTramites")
    public long getCantidadTramites() {
        return this.vgn_CantidadTramites;
    }

    public void setCantidadTramites(long pvn_CantidadTramites) {
        this.vgn_CantidadTramites = pvn_CantidadTramites;
    }

    @JsonProperty("cantidadPermisos")
    public long getCantidadPermisos() {
        return this.vgn_CantidadPermisos;
    }

    public void setCantidadPermisos(long pvn_CantidadPermisos) {
        this.vgn_CantidadPermisos = pvn_CantidadPermisos;
    }



    @JsonProperty("tramites")
    public List<EntTramiteReporte> getTramites() {
        return this.vgo_LsTramites;
    }

    public void setTramites(List<EntTramiteReporte> pvo_LsTramites) {
        this.vgo_LsTramites = pvo_LsTramites;
    }

    @JsonProperty("notas")
    public List<String> getNotas() {
        return this.vgo_LsNotas;
    }

    public void setNotas(List<String> pvo_LsNotas) {
        this.vgo_LsNotas = pvo_LsNotas;
    }

    @JsonProperty("incluirTotalCif")
    public boolean isIncluirTotalCif() {
        return this.vgb_IncluirTotalCif;
    }

    public void setIncluirTotalCif(boolean pvb_IncluirTotalCif) {
        this.vgb_IncluirTotalCif = pvb_IncluirTotalCif;
    }

    @JsonProperty("incluirTotalBodegaje")
    public boolean isIncluirTotalBodegaje() {
        return this.vgb_IncluirTotalBodegaje;
    }

    public void setIncluirTotalBodegaje(boolean pvb_IncluirTotalBodegaje) {
        this.vgb_IncluirTotalBodegaje = pvb_IncluirTotalBodegaje;
    }
}
