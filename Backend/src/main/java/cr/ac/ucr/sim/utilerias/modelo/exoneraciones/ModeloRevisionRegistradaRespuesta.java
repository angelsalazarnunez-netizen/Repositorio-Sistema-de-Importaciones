package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 03/10/2026
 * Descripcion: respuesta generada al registrar
 * una revision documental.
 */
public class ModeloRevisionRegistradaRespuesta {

    private Integer vgn_IdRevisionDocumento;
    private Integer vgn_IdDocumento;
    private String vgc_EstadoRevision;
    private String vgc_Observacion;
    private LocalDateTime vgd_FechaRevision;

    public ModeloRevisionRegistradaRespuesta() {
    }

    public Integer getIdRevisionDocumento() {
        return vgn_IdRevisionDocumento;
    }

    public void setIdRevisionDocumento(
            Integer vln_IdRevisionDocumento) {

        this.vgn_IdRevisionDocumento =
                vln_IdRevisionDocumento;
    }

    public Integer getIdDocumento() {
        return vgn_IdDocumento;
    }

    public void setIdDocumento(
            Integer vln_IdDocumento) {

        this.vgn_IdDocumento =
                vln_IdDocumento;
    }

    public String getEstadoRevision() {
        return vgc_EstadoRevision;
    }

    public void setEstadoRevision(
            String vlc_EstadoRevision) {

        this.vgc_EstadoRevision =
                vlc_EstadoRevision;
    }

    public String getObservacion() {
        return vgc_Observacion;
    }

    public void setObservacion(
            String vlc_Observacion) {

        this.vgc_Observacion =
                vlc_Observacion;
    }

    public LocalDateTime getFechaRevision() {
        return vgd_FechaRevision;
    }

    public void setFechaRevision(
            LocalDateTime vld_FechaRevision) {

        this.vgd_FechaRevision =
                vld_FechaRevision;
    }
}