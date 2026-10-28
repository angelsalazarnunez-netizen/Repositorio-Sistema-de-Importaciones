package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Decisión registrada sobre una solicitud de exoneración (HU-022), con su responsable, su
 *   fecha y hora, el cambio de estado que produjo y el resultado de la notificación.
 */
public class ModeloDecisionRespuesta {

    private Integer vgn_IdDecision;
    private String vgc_Decision;
    private String vgc_NombreDecision;
    private String vgc_Justificacion;
    private String vgc_Decisor;
    private String vgc_EstadoAnterior;
    private String vgc_EstadoNuevo;
    private LocalDateTime vgd_FechaHora;
    private String vgc_Notificado;
    private String vgc_EstadoNotificacion;

    public Integer getIdDecision() {
        return this.vgn_IdDecision;
    }

    public void setIdDecision(Integer pvn_IdDecision) {
        this.vgn_IdDecision = pvn_IdDecision;
    }

    public String getDecision() {
        return this.vgc_Decision;
    }

    public void setDecision(String pvc_Decision) {
        this.vgc_Decision = pvc_Decision;
    }

    public String getNombreDecision() {
        return this.vgc_NombreDecision;
    }

    public void setNombreDecision(String pvc_NombreDecision) {
        this.vgc_NombreDecision = pvc_NombreDecision;
    }

    public String getJustificacion() {
        return this.vgc_Justificacion;
    }

    public void setJustificacion(String pvc_Justificacion) {
        this.vgc_Justificacion = pvc_Justificacion;
    }

    public String getDecisor() {
        return this.vgc_Decisor;
    }

    public void setDecisor(String pvc_Decisor) {
        this.vgc_Decisor = pvc_Decisor;
    }

    public String getEstadoAnterior() {
        return this.vgc_EstadoAnterior;
    }

    public void setEstadoAnterior(String pvc_EstadoAnterior) {
        this.vgc_EstadoAnterior = pvc_EstadoAnterior;
    }

    public String getEstadoNuevo() {
        return this.vgc_EstadoNuevo;
    }

    public void setEstadoNuevo(String pvc_EstadoNuevo) {
        this.vgc_EstadoNuevo = pvc_EstadoNuevo;
    }

    public LocalDateTime getFechaHora() {
        return this.vgd_FechaHora;
    }

    public void setFechaHora(LocalDateTime pvd_FechaHora) {
        this.vgd_FechaHora = pvd_FechaHora;
    }

    public String getNotificado() {
        return this.vgc_Notificado;
    }

    public void setNotificado(String pvc_Notificado) {
        this.vgc_Notificado = pvc_Notificado;
    }

    public String getEstadoNotificacion() {
        return this.vgc_EstadoNotificacion;
    }

    public void setEstadoNotificacion(String pvc_EstadoNotificacion) {
        this.vgc_EstadoNotificacion = pvc_EstadoNotificacion;
    }
}
