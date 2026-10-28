package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Entidad de negocio de la tabla SIMH_ESTADO_TRAMITE. Registra cada cambio de
 *   estado del expediente; el primer movimiento se crea al registrar el trámite.
 */
@Entity
@Table(name = "SIMH_ESTADO_TRAMITE")
public class EntHistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HISTORIAL")
    private Integer vgn_IdHistorial;

    @Column(name = "ID_TRAMITE", nullable = false)
    private Integer vgn_IdTramite;

    @Column(name = "ID_ESTADO_ANTERIOR")
    private Integer vgn_IdEstadoAnterior;

    @Column(name = "ID_ESTADO_NUEVO", nullable = false)
    private Integer vgn_IdEstadoNuevo;

    @Column(name = "ID_USUARIO", nullable = false)
    private Integer vgn_IdUsuario;

    @Column(name = "JUSTIFICACION", length = 255)
    private String vgc_Justificacion;

    @Column(name = "FECHA_HORA", nullable = false)
    private LocalDateTime vgd_FechaHora;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Asigna la fecha del movimiento cuando no viene indicada.
     */
    @PrePersist
    public void AsignarFechaHora() {
        if (this.vgd_FechaHora == null) {
            this.vgd_FechaHora = LocalDateTime.now();
        }
    }

    @JsonProperty("idHistorial")
    public Integer getIdHistorial() {
        return this.vgn_IdHistorial;
    }

    public void setIdHistorial(Integer pvn_IdHistorial) {
        this.vgn_IdHistorial = pvn_IdHistorial;
    }

    @JsonProperty("idTramite")
    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    @JsonProperty("idEstadoAnterior")
    public Integer getIdEstadoAnterior() {
        return this.vgn_IdEstadoAnterior;
    }

    public void setIdEstadoAnterior(Integer pvn_IdEstadoAnterior) {
        this.vgn_IdEstadoAnterior = pvn_IdEstadoAnterior;
    }

    @JsonProperty("idEstadoNuevo")
    public Integer getIdEstadoNuevo() {
        return this.vgn_IdEstadoNuevo;
    }

    public void setIdEstadoNuevo(Integer pvn_IdEstadoNuevo) {
        this.vgn_IdEstadoNuevo = pvn_IdEstadoNuevo;
    }

    @JsonProperty("idUsuario")
    public Integer getIdUsuario() {
        return this.vgn_IdUsuario;
    }

    public void setIdUsuario(Integer pvn_IdUsuario) {
        this.vgn_IdUsuario = pvn_IdUsuario;
    }

    @JsonProperty("justificacion")
    public String getJustificacion() {
        return this.vgc_Justificacion;
    }

    public void setJustificacion(String pvc_Justificacion) {
        this.vgc_Justificacion = pvc_Justificacion;
    }

    @JsonProperty("fechaHora")
    public LocalDateTime getFechaHora() {
        return this.vgd_FechaHora;
    }

    public void setFechaHora(LocalDateTime pvd_FechaHora) {
        this.vgd_FechaHora = pvd_FechaHora;
    }
}
