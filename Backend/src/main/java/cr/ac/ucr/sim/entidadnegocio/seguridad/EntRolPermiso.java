package cr.ac.ucr.sim.entidadnegocio.seguridad;

import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Descripcion: representa la asignacion de un permiso
 * a un rol del Sistema de Importaciones.
 */
@Entity
@Table(name = "SIMT_ROL_PERMISO")
public class EntRolPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ROL_PERMISO")
    private Long vgn_IdRolPermiso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_ROL",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_SIMM_ROL_SIMT_ROL_PERMISO"
            )
    )
    private EntRol vgo_EnRol;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_PERMISO",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_SIMM_PERMISO_SIMT_ROL_PERMISO"
            )
    )
    private EntPermiso vgo_EnPermiso;

    @Column(name = "FECHA_ASIGNACION", nullable = false)
    private LocalDateTime vgd_FechaAsignacion;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado;

    public EntRolPermiso() {
    }

    /**
     * Descripcion: asigna valores iniciales
     * antes de registrar una asignacion de permiso a rol.
     */
    @PrePersist
    protected void AntesDeGuardar() {
        if (vgd_FechaAsignacion == null) {
            vgd_FechaAsignacion = LocalDateTime.now();
        }

        if (vgc_Estado == null || vgc_Estado.isBlank()) {
            vgc_Estado = ConstantesSeguridad.ESTADO_ACTIVO;
        }
    }

    public Long getIdRolPermiso() {
        return vgn_IdRolPermiso;
    }

    public void setIdRolPermiso(Long vln_IdRolPermiso) {
        this.vgn_IdRolPermiso = vln_IdRolPermiso;
    }

    public EntRol getRol() {
        return vgo_EnRol;
    }

    public void setRol(EntRol vlo_EnRol) {
        this.vgo_EnRol = vlo_EnRol;
    }

    public EntPermiso getPermiso() {
        return vgo_EnPermiso;
    }

    public void setPermiso(EntPermiso vlo_EnPermiso) {
        this.vgo_EnPermiso = vlo_EnPermiso;
    }

    public LocalDateTime getFechaAsignacion() {
        return vgd_FechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime vld_FechaAsignacion) {
        this.vgd_FechaAsignacion = vld_FechaAsignacion;
    }

    public String getEstado() {
        return vgc_Estado;
    }

    public void setEstado(String vlc_Estado) {
        this.vgc_Estado = vlc_Estado;
    }
}