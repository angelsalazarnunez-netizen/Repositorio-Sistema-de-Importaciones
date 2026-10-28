package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 04/10/2026
 * Descripcion: representa un registro historico
 * de revision documental de una exoneracion.
 */
public class ModeloHistorialRevisionRespuesta {

    private Integer vgn_IdRevisionDocumento;
    private Integer vgn_IdDocumento;
    private String vgc_EstadoRevision;
    private String vgc_Observacion;
    private LocalDateTime vgd_FechaRevision;
    private String vgc_EstadoRegistro;
    private Long vgn_IdUsuarioRevisor;
    private String vgc_NombreUsuarioRevisor;

    public ModeloHistorialRevisionRespuesta() {
    }

    public Integer getIdRevisionDocumento() {
        return vgn_IdRevisionDocumento;
    }

    public void setIdRevisionDocumento(
            Integer vln_IdRevisionDocumento
    ) {
        this.vgn_IdRevisionDocumento =
                vln_IdRevisionDocumento;
    }

    public Integer getIdDocumento() {
        return vgn_IdDocumento;
    }

    public void setIdDocumento(
            Integer vln_IdDocumento
    ) {
        this.vgn_IdDocumento =
                vln_IdDocumento;
    }

    public String getEstadoRevision() {
        return vgc_EstadoRevision;
    }

    public void setEstadoRevision(
            String vlc_EstadoRevision
    ) {
        this.vgc_EstadoRevision =
                vlc_EstadoRevision;
    }

    public String getObservacion() {
        return vgc_Observacion;
    }

    public void setObservacion(
            String vlc_Observacion
    ) {
        this.vgc_Observacion =
                vlc_Observacion;
    }

    public LocalDateTime getFechaRevision() {
        return vgd_FechaRevision;
    }

    public void setFechaRevision(
            LocalDateTime vld_FechaRevision
    ) {
        this.vgd_FechaRevision =
                vld_FechaRevision;
    }

    public String getEstadoRegistro() {
        return vgc_EstadoRegistro;
    }

    public void setEstadoRegistro(
            String vlc_EstadoRegistro
    ) {
        this.vgc_EstadoRegistro =
                vlc_EstadoRegistro;
    }

    public Long getIdUsuarioRevisor() {
        return vgn_IdUsuarioRevisor;
    }

    public void setIdUsuarioRevisor(
            Long vln_IdUsuarioRevisor
    ) {
        this.vgn_IdUsuarioRevisor =
                vln_IdUsuarioRevisor;
    }

    public String getNombreUsuarioRevisor() {
        return vgc_NombreUsuarioRevisor;
    }

    public void setNombreUsuarioRevisor(
            String vlc_NombreUsuarioRevisor
    ) {
        this.vgc_NombreUsuarioRevisor =
                vlc_NombreUsuarioRevisor;
    }
}