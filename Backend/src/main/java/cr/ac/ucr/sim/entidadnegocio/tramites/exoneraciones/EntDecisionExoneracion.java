package cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Entidad de negocio de la tabla SIMT_DECISION_EXONERACION (HU-022). Conserva cada
 *   decisión de la jefatura sobre una solicitud de exoneración: aprobar (APR), rechazar (REC) o
 *   solicitar modificación (MOD), con su usuario, su fecha y hora y el estado del expediente antes
 *   y después. Una decisión registrada no se modifica: todas sus columnas son de solo inserción.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
@Entity
@Table(name = "SIMT_DECISION_EXONERACION")
public class EntDecisionExoneracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DECISION")
    private Integer vgn_IdDecision;

    @Column(name = "ID_TRAMITE", nullable = false, updatable = false)
    private Integer vgn_IdTramite;

    @Column(name = "ID_USUARIO_DECISOR", nullable = false, updatable = false)
    private Integer vgn_IdUsuarioDecisor;

    @Column(name = "DECISION", nullable = false, updatable = false, length = 3)
    private String vgc_Decision;

    @Column(name = "JUSTIFICACION", updatable = false, length = 1000)
    private String vgc_Justificacion;

    @Column(name = "ID_ESTADO_ANTERIOR", nullable = false, updatable = false)
    private Integer vgn_IdEstadoAnterior;

    @Column(name = "ID_ESTADO_NUEVO", nullable = false, updatable = false)
    private Integer vgn_IdEstadoNuevo;

    @Column(name = "FECHA_HORA", nullable = false, updatable = false)
    private LocalDateTime vgd_FechaHora;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado;

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Asigna la fecha y hora de la decisión y el estado de registro cuando no vienen
     *   indicados.
     */
    @PrePersist
    public void AsignarValoresPorDefecto() {
        if (this.vgd_FechaHora == null) {
            this.vgd_FechaHora = LocalDateTime.now();
        }
        if (this.vgc_Estado == null) {
            this.vgc_Estado = "ACT";
        }
    }

    public Integer getIdDecision() {
        return this.vgn_IdDecision;
    }

    public void setIdDecision(Integer pvn_IdDecision) {
        this.vgn_IdDecision = pvn_IdDecision;
    }

    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    public Integer getIdUsuarioDecisor() {
        return this.vgn_IdUsuarioDecisor;
    }

    public void setIdUsuarioDecisor(Integer pvn_IdUsuarioDecisor) {
        this.vgn_IdUsuarioDecisor = pvn_IdUsuarioDecisor;
    }

    public String getDecision() {
        return this.vgc_Decision;
    }

    public void setDecision(String pvc_Decision) {
        this.vgc_Decision = pvc_Decision;
    }

    public String getJustificacion() {
        return this.vgc_Justificacion;
    }

    public void setJustificacion(String pvc_Justificacion) {
        this.vgc_Justificacion = pvc_Justificacion;
    }

    public Integer getIdEstadoAnterior() {
        return this.vgn_IdEstadoAnterior;
    }

    public void setIdEstadoAnterior(Integer pvn_IdEstadoAnterior) {
        this.vgn_IdEstadoAnterior = pvn_IdEstadoAnterior;
    }

    public Integer getIdEstadoNuevo() {
        return this.vgn_IdEstadoNuevo;
    }

    public void setIdEstadoNuevo(Integer pvn_IdEstadoNuevo) {
        this.vgn_IdEstadoNuevo = pvn_IdEstadoNuevo;
    }

    public LocalDateTime getFechaHora() {
        return this.vgd_FechaHora;
    }

    public void setFechaHora(LocalDateTime pvd_FechaHora) {
        this.vgd_FechaHora = pvd_FechaHora;
    }

    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }
}
