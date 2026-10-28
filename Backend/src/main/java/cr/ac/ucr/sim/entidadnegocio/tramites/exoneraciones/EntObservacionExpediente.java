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
 * Fecha: 04/10/2026
 * Descripción: Entidad de negocio de la tabla SIMT_OBSERVACION_EXPED (HU-020). Representa una
 *   observación o aclaración de un expediente. Nace como borrador, que solo su autor puede
 *   modificar o eliminar; al confirmarse pasa al historial compartido del expediente y deja de
 *   poder modificarse. Un borrador eliminado se marca inactivo, nunca se borra físicamente.
 *   No se devuelve directamente desde los servicios REST: se expone mediante modelos de respuesta.
 *
 * Historial de cambios:
 *   04/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
@Entity
@Table(name = "SIMT_OBSERVACION_EXPED")
public class EntObservacionExpediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_OBSERVACION")
    private Long vgn_IdObservacion;

    // El expediente, el autor y la fecha de creación no cambian después de registrar la
    // observación: Hibernate los excluye de toda actualización.
    @Column(name = "ID_TRAMITE", nullable = false, updatable = false)
    private Integer vgn_IdTramite;

    @Column(name = "ID_USUARIO_AUTOR", nullable = false, updatable = false)
    // La columna es BIGINT; se mapea como Integer igual que EntUsuario y el usuario autenticado.
    private Integer vgn_IdUsuarioAutor;

    @Column(name = "MOTIVO", length = 100)
    private String vgc_Motivo;

    @Column(name = "OBSERVACION", nullable = false, length = 1000)
    private String vgc_Observacion;

    @Column(name = "ESTADO_OBSERVACION", nullable = false, length = 3)
    private String vgc_EstadoObservacion;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime vgd_FechaCreacion;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime vgd_FechaModificacion;

    @Column(name = "FECHA_CONFIRMACION")
    private LocalDateTime vgd_FechaConfirmacion;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado;

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Asigna la fecha de creación cuando no viene indicada. Los estados los asigna
     *   la lógica de negocio, que es la dueña del ciclo borrador / confirmada.
     */
    @PrePersist
    public void AsignarFechaCreacion() {
        if (this.vgd_FechaCreacion == null) {
            this.vgd_FechaCreacion = LocalDateTime.now();
        }
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

    public Integer getIdUsuarioAutor() {
        return this.vgn_IdUsuarioAutor;
    }

    public void setIdUsuarioAutor(Integer pvn_IdUsuarioAutor) {
        this.vgn_IdUsuarioAutor = pvn_IdUsuarioAutor;
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

    public String getEstadoObservacion() {
        return this.vgc_EstadoObservacion;
    }

    public void setEstadoObservacion(String pvc_EstadoObservacion) {
        this.vgc_EstadoObservacion = pvc_EstadoObservacion;
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

    public LocalDateTime getFechaConfirmacion() {
        return this.vgd_FechaConfirmacion;
    }

    public void setFechaConfirmacion(LocalDateTime pvd_FechaConfirmacion) {
        this.vgd_FechaConfirmacion = pvd_FechaConfirmacion;
    }

    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }
}
