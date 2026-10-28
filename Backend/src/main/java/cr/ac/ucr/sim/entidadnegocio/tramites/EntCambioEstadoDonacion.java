package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 15/09/2026
 * Descripción: Datos del cambio de estado de un expediente de donación (HU-044). No representa
 *   una tabla: transporta lo que envía la pantalla de gestión al actualizar el trámite.
 */
public class EntCambioEstadoDonacion {

    private String vgc_NuevoEstado;
    private String vgc_Justificacion;
    private String vgc_Observaciones;
    private Integer vgn_IdUsuarioResponsable;

    @JsonProperty("nuevoEstado")
    public String getNuevoEstado() {
        return this.vgc_NuevoEstado;
    }

    public void setNuevoEstado(String pvc_NuevoEstado) {
        this.vgc_NuevoEstado = pvc_NuevoEstado;
    }

    @JsonProperty("justificacion")
    public String getJustificacion() {
        return this.vgc_Justificacion;
    }

    public void setJustificacion(String pvc_Justificacion) {
        this.vgc_Justificacion = pvc_Justificacion;
    }

    @JsonProperty("observaciones")
    public String getObservaciones() {
        return this.vgc_Observaciones;
    }

    public void setObservaciones(String pvc_Observaciones) {
        this.vgc_Observaciones = pvc_Observaciones;
    }

    @JsonProperty("idUsuarioResponsable")
    public Integer getIdUsuarioResponsable() {
        return this.vgn_IdUsuarioResponsable;
    }

    public void setIdUsuarioResponsable(Integer pvn_IdUsuarioResponsable) {
        this.vgn_IdUsuarioResponsable = pvn_IdUsuarioResponsable;
    }
}
