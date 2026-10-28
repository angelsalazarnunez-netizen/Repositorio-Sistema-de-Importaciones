package cr.ac.ucr.sim.utilerias.modelo.tramites;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Panel de trámites activos (HU-030): los trámites de la vista solicitada (activos o histórico)
 *   y los indicadores de la vista operativa del día. Los indicadores siempre se calculan sobre los
 *   trámites activos, aunque se consulte el histórico.
 */
public class ModeloPanelTramitesRespuesta {

    private String vgc_Alcance;
    private LocalDateTime vgd_FechaConsulta;
    private Integer vgn_TotalActivos;
    private Integer vgn_TotalEnProceso;
    private Integer vgn_TotalPorLlegar;
    private Integer vgn_TotalPermisos;
    private Integer vgn_TotalAprobacion;
    private Integer vgn_TotalExoneracion;
    private Integer vgn_TotalProximosVencer;
    private List<ModeloTramiteActivoRespuesta> vgo_LsTramites = new ArrayList<>();

    public String getAlcance() {
        return this.vgc_Alcance;
    }

    public void setAlcance(String pvc_Alcance) {
        this.vgc_Alcance = pvc_Alcance;
    }

    public LocalDateTime getFechaConsulta() {
        return this.vgd_FechaConsulta;
    }

    public void setFechaConsulta(LocalDateTime pvd_FechaConsulta) {
        this.vgd_FechaConsulta = pvd_FechaConsulta;
    }

    public Integer getTotalActivos() {
        return this.vgn_TotalActivos;
    }

    public void setTotalActivos(Integer pvn_TotalActivos) {
        this.vgn_TotalActivos = pvn_TotalActivos;
    }

    public Integer getTotalEnProceso() {
        return this.vgn_TotalEnProceso;
    }

    public void setTotalEnProceso(Integer pvn_TotalEnProceso) {
        this.vgn_TotalEnProceso = pvn_TotalEnProceso;
    }

    public Integer getTotalPorLlegar() {
        return this.vgn_TotalPorLlegar;
    }

    public void setTotalPorLlegar(Integer pvn_TotalPorLlegar) {
        this.vgn_TotalPorLlegar = pvn_TotalPorLlegar;
    }

    public Integer getTotalPermisos() {
        return this.vgn_TotalPermisos;
    }

    public void setTotalPermisos(Integer pvn_TotalPermisos) {
        this.vgn_TotalPermisos = pvn_TotalPermisos;
    }

    public Integer getTotalAprobacion() {
        return this.vgn_TotalAprobacion;
    }

    public void setTotalAprobacion(Integer pvn_TotalAprobacion) {
        this.vgn_TotalAprobacion = pvn_TotalAprobacion;
    }

    public Integer getTotalExoneracion() {
        return this.vgn_TotalExoneracion;
    }

    public void setTotalExoneracion(Integer pvn_TotalExoneracion) {
        this.vgn_TotalExoneracion = pvn_TotalExoneracion;
    }

    public Integer getTotalProximosVencer() {
        return this.vgn_TotalProximosVencer;
    }

    public void setTotalProximosVencer(Integer pvn_TotalProximosVencer) {
        this.vgn_TotalProximosVencer = pvn_TotalProximosVencer;
    }

    public List<ModeloTramiteActivoRespuesta> getTramites() {
        return this.vgo_LsTramites;
    }

    public void setTramites(List<ModeloTramiteActivoRespuesta> pvo_LsTramites) {
        this.vgo_LsTramites = pvo_LsTramites;
    }
}
