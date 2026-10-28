package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 25/09/2026
 * Descripcion: Criterios con los que se genera el reporte de consultas internas (HU-036). No
 *   representa una tabla: transporta el rango de fechas, los filtros y el criterio de
 *   agrupacion que elige la persona agente de aduanas.
 */
public class EntFiltroReporte {

    private LocalDate vgd_FechaDesde;
    private LocalDate vgd_FechaHasta;
    private String vgc_CodigoTipoTramite;
    private String vgc_RegimenAduanero;
    private String vgc_TipoExoneracion;
    private String vgc_OrigenDependencia;
    private List<String> vgo_LsAgrupaciones = new ArrayList<>();
    private boolean vgb_EnumerarTramites;
    private boolean vgb_CombinarCriterios;
    private boolean vgb_IncluirTotalCif = true;
    private boolean vgb_IncluirTotalBodegaje = true;

    public LocalDate getFechaDesde() {
        return this.vgd_FechaDesde;
    }

    public void setFechaDesde(LocalDate pvd_FechaDesde) {
        this.vgd_FechaDesde = pvd_FechaDesde;
    }

    public LocalDate getFechaHasta() {
        return this.vgd_FechaHasta;
    }

    public void setFechaHasta(LocalDate pvd_FechaHasta) {
        this.vgd_FechaHasta = pvd_FechaHasta;
    }

    public String getCodigoTipoTramite() {
        return this.vgc_CodigoTipoTramite;
    }

    public void setCodigoTipoTramite(String pvc_CodigoTipoTramite) {
        this.vgc_CodigoTipoTramite = pvc_CodigoTipoTramite;
    }

    public String getRegimenAduanero() {
        return this.vgc_RegimenAduanero;
    }

    public void setRegimenAduanero(String pvc_RegimenAduanero) {
        this.vgc_RegimenAduanero = pvc_RegimenAduanero;
    }

    public String getTipoExoneracion() {
        return this.vgc_TipoExoneracion;
    }

    public void setTipoExoneracion(String pvc_TipoExoneracion) {
        this.vgc_TipoExoneracion = pvc_TipoExoneracion;
    }

    public String getOrigenDependencia() {
        return this.vgc_OrigenDependencia;
    }

    public void setOrigenDependencia(String pvc_OrigenDependencia) {
        this.vgc_OrigenDependencia = pvc_OrigenDependencia;
    }

    public List<String> getAgrupaciones() {
        return this.vgo_LsAgrupaciones;
    }

    public void setAgrupaciones(List<String> pvo_LsAgrupaciones) {
        this.vgo_LsAgrupaciones = pvo_LsAgrupaciones == null ? new ArrayList<>() : pvo_LsAgrupaciones;
    }

    public boolean isEnumerarTramites() {
        return this.vgb_EnumerarTramites;
    }

    public void setEnumerarTramites(boolean pvb_EnumerarTramites) {
        this.vgb_EnumerarTramites = pvb_EnumerarTramites;
    }

    public boolean isCombinarCriterios() {
        return this.vgb_CombinarCriterios;
    }

    public void setCombinarCriterios(boolean pvb_CombinarCriterios) {
        this.vgb_CombinarCriterios = pvb_CombinarCriterios;
    }

    public boolean isIncluirTotalCif() {
        return this.vgb_IncluirTotalCif;
    }

    public void setIncluirTotalCif(boolean pvb_IncluirTotalCif) {
        this.vgb_IncluirTotalCif = pvb_IncluirTotalCif;
    }

    public boolean isIncluirTotalBodegaje() {
        return this.vgb_IncluirTotalBodegaje;
    }

    public void setIncluirTotalBodegaje(boolean pvb_IncluirTotalBodegaje) {
        this.vgb_IncluirTotalBodegaje = pvb_IncluirTotalBodegaje;
    }
}
