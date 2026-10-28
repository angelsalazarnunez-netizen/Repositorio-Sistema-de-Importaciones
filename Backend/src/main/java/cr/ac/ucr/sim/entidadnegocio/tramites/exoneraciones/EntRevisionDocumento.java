package cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones;

import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 03/10/2026
 * Descripcion: representa una revision realizada sobre
 * un documento asociado a un expediente de tramite.
 */
@Entity
@Table(name = "SIMT_REVISION_DOCUMENTO")
public class EntRevisionDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_REVISION_DOCUMENTO")
    private Integer vgn_IdRevisionDocumento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_DOCUMENTO",
            nullable = false
    )
    private EntDocumento vgo_EnDocumento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_USUARIO_REVISOR",
            nullable = false
    )
    private EntUsuario vgo_EnUsuarioRevisor;

    @Column(
            name = "ESTADO_REVISION",
            nullable = false,
            length = 10
    )
    private String vgc_EstadoRevision;

    @Column(
            name = "OBSERVACION",
            length = 1000
    )
    private String vgc_Observacion;

    @Column(
            name = "FECHA_HORA",
            nullable = false
    )
    private LocalDateTime vgd_FechaHora;

    @Column(
            name = "ESTADO",
            nullable = false,
            length = 3
    )
    private String vgc_Estado;

    public EntRevisionDocumento() {
    }

    /**
     * Descripcion: asigna los valores por defecto
     * antes de guardar una revision.
     */
    @PrePersist
    protected void AntesDeGuardar() {

        if (vgd_FechaHora == null) {
            vgd_FechaHora =
                    LocalDateTime.now();
        }

        if (vgc_Estado == null
                || vgc_Estado.isBlank()) {

            vgc_Estado = "ACT";
        }
    }

    public Integer getIdRevisionDocumento() {
        return vgn_IdRevisionDocumento;
    }

    public void setIdRevisionDocumento(Integer vln_IdRevisionDocumento) {
        this.vgn_IdRevisionDocumento =
                vln_IdRevisionDocumento;
    }

    public EntDocumento getDocumento() {
        return vgo_EnDocumento;
    }

    public void setDocumento(EntDocumento vlo_EnDocumento) {

        this.vgo_EnDocumento =
                vlo_EnDocumento;
    }

    public EntUsuario getUsuarioRevisor() {
        return vgo_EnUsuarioRevisor;
    }

    public void setUsuarioRevisor(EntUsuario vlo_EnUsuarioRevisor) {
        this.vgo_EnUsuarioRevisor =
                vlo_EnUsuarioRevisor;
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

    public LocalDateTime getFechaHora() {
        return vgd_FechaHora;
    }

    public void setFechaHora(LocalDateTime vld_FechaHora) {
        this.vgd_FechaHora =
                vld_FechaHora;
    }

    public String getEstado() {
        return vgc_Estado;
    }

    public void setEstado(String vlc_Estado) {
        this.vgc_Estado =
                vlc_Estado;
    }
}