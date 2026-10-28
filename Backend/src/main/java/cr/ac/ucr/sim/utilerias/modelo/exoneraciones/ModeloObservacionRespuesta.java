package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 04/10/2026
 * Descripción: Observación del expediente tal como la muestra la pantalla (HU-020), con el
 *   nombre del responsable ya resuelto. No expone el estado de registro (ACT/INA): los
 *   borradores eliminados nunca se devuelven.
 *
 * Historial de cambios:
 *   04/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
public class ModeloObservacionRespuesta {

    private Long vgn_IdObservacion;
    private Integer vgn_IdTramite;
    private String vgc_Motivo;
    private String vgc_Observacion;
    private String vgc_EstadoObservacion;
    private String vgc_Responsable;
    private LocalDateTime vgd_FechaCreacion;
    private LocalDateTime vgd_FechaModificacion;
    private LocalDateTime vgd_FechaConfirmacion;
    private Boolean vgb_Editable;

    public ModeloObservacionRespuesta() {
    }

    public Long getIdObservacion() {
        return this.vgn_IdObservacion;
    }

    public void setIdObservacion(Long pvn_IdObservacion) {
        this.vgn_IdObservacion = pvn_IdObservacion;
    }

    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    public String getMotivo() {
        return this.vgc_Motivo;
    }

    public void setMotivo(String pvc_Motivo) {
        this.vgc_Motivo = pvc_Motivo;
    }

    public String getObservacion() {
        return this.vgc_Observacion;
    }

    public void setObservacion(String pvc_Observacion) {
        this.vgc_Observacion = pvc_Observacion;
    }

    /**
     * BOR = borrador, CON = confirmada.
     */
    public String getEstadoObservacion() {
        return this.vgc_EstadoObservacion;
    }

    public void setEstadoObservacion(String pvc_EstadoObservacion) {
        this.vgc_EstadoObservacion = pvc_EstadoObservacion;
    }

    /**
     * Nombre completo del autor de la observación.
     */
    public String getResponsable() {
        return this.vgc_Responsable;
    }

    public void setResponsable(String pvc_Responsable) {
        this.vgc_Responsable = pvc_Responsable;
    }

    public LocalDateTime getFechaCreacion() {
        return this.vgd_FechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime pvd_FechaCreacion) {
        this.vgd_FechaCreacion = pvd_FechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return this.vgd_FechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime pvd_FechaModificacion) {
        this.vgd_FechaModificacion = pvd_FechaModificacion;
    }

    /**
     * Fecha y hora oficial del historial. Nula mientras la observación sea borrador.
     */
    public LocalDateTime getFechaConfirmacion() {
        return this.vgd_FechaConfirmacion;
    }

    public void setFechaConfirmacion(LocalDateTime pvd_FechaConfirmacion) {
        this.vgd_FechaConfirmacion = pvd_FechaConfirmacion;
    }

    /**
     * true cuando la pantalla debe ofrecer editar, eliminar y confirmar: solo un borrador lo es,
     *   y solo se devuelven los borradores del usuario autenticado.
     */
    public Boolean getEditable() {
        return this.vgb_Editable;
    }

    public void setEditable(Boolean pvb_Editable) {
        this.vgb_Editable = pvb_Editable;
    }
}
