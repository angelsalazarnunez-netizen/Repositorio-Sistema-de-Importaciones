package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Movimiento del historial de estados tal como se muestra en el seguimiento.
 */
public class EntMovimientoEstado {

    private final String vgc_EstadoAnterior;
    private final String vgc_EstadoNuevo;
    private final String vgc_Usuario;
    private final String vgc_Justificacion;
    private final LocalDateTime vgd_FechaHora;

    public EntMovimientoEstado(
            String pvc_EstadoAnterior,
            String pvc_EstadoNuevo,
            String pvc_Usuario,
            String pvc_Justificacion,
            LocalDateTime pvd_FechaHora) {
        this.vgc_EstadoAnterior = pvc_EstadoAnterior;
        this.vgc_EstadoNuevo = pvc_EstadoNuevo;
        this.vgc_Usuario = pvc_Usuario;
        this.vgc_Justificacion = pvc_Justificacion;
        this.vgd_FechaHora = pvd_FechaHora;
    }

    @JsonProperty("estadoAnterior")
    public String getEstadoAnterior() {
        return this.vgc_EstadoAnterior;
    }

    @JsonProperty("estadoNuevo")
    public String getEstadoNuevo() {
        return this.vgc_EstadoNuevo;
    }

    @JsonProperty("usuario")
    public String getUsuario() {
        return this.vgc_Usuario;
    }

    @JsonProperty("justificacion")
    public String getJustificacion() {
        return this.vgc_Justificacion;
    }

    @JsonProperty("fechaHora")
    public LocalDateTime getFechaHora() {
        return this.vgd_FechaHora;
    }
}
