package cr.ac.ucr.sim.entidadnegocio.seguridad;

import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Descripcion: representa la asignacion de un rol a un usuario
 * del Sistema de Importaciones.
 */
@Entity
@Table(name = "SIMT_USUARIO_ROL")
public class EntUsuarioRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO_ROL")
    private Long vgn_IdUsuarioRol;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_USUARIO",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_SIMF_USUARIO_SIMT_USUARIO_ROL"
            )
    )
    private EntUsuario vgo_EnUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_ROL",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_SIMM_ROL_SIMT_USUARIO_ROL"
            )
    )
    private EntRol vgo_EnRol;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_USUARIO_ASIGNADOR",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_SIMF_USUARIO_SIMT_USUARIO_ROL_ASIG"
            )
    )
    private EntUsuario vgo_EnUsuarioAsignador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ID_USUARIO_FINALIZADOR",
            foreignKey = @ForeignKey(
                    name = "FK_SIMF_USUARIO_SIMT_USUARIO_ROL_FIN"
            )
    )
    private EntUsuario vgo_EnUsuarioFinalizador;


    @Column(name = "FECHA_ASIGNACION", nullable = false)
    private LocalDateTime vgd_FechaAsignacion;

    @Column(name = "FECHA_FINALIZACION")
    private LocalDateTime vgd_FechaFinalizacion;

    @Column(name = "MOTIVO", length = 255)
    private String vgc_Motivo;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado;

    public EntUsuarioRol() {
    }

    /**
     * Descripcion: asigna valores iniciales
     * antes de registrar una asignacion de rol a usuario.
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

    public Long getIdUsuarioRol() {
        return vgn_IdUsuarioRol;
    }

    public void setIdUsuarioRol(Long vln_IdUsuarioRol) {
        this.vgn_IdUsuarioRol = vln_IdUsuarioRol;
    }

    public EntUsuario getUsuario() {
        return vgo_EnUsuario;
    }

    public void setUsuario(EntUsuario vlo_EnUsuario) {
        this.vgo_EnUsuario = vlo_EnUsuario;
    }

    public EntRol getRol() {
        return vgo_EnRol;
    }

    public void setRol(EntRol vlo_EnRol) {
        this.vgo_EnRol = vlo_EnRol;
    }

    public EntUsuario getUsuarioAsignador() {
        return vgo_EnUsuarioAsignador;
    }

    public void setUsuarioAsignador(EntUsuario vlo_EnUsuarioAsignador) {
        this.vgo_EnUsuarioAsignador = vlo_EnUsuarioAsignador;
    }

    public EntUsuario getUsuarioFinalizador() {
        return vgo_EnUsuarioFinalizador;
    }

    public void setUsuarioFinalizador(EntUsuario vlo_EnUsuarioFinalizador) {
        this.vgo_EnUsuarioFinalizador = vlo_EnUsuarioFinalizador;
    }

    public LocalDateTime getFechaAsignacion() {
        return vgd_FechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime vld_FechaAsignacion) {
        this.vgd_FechaAsignacion = vld_FechaAsignacion;
    }

    public LocalDateTime getFechaFinalizacion() {
        return vgd_FechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime vld_FechaFinalizacion) {
        this.vgd_FechaFinalizacion = vld_FechaFinalizacion;
    }

    public String getMotivo() {
        return vgc_Motivo;
    }

    public void setMotivo(String vlc_Motivo) {
        this.vgc_Motivo = vlc_Motivo;
    }

    public String getEstado() {
        return vgc_Estado;
    }

    public void setEstado(String vlc_Estado) {
        this.vgc_Estado = vlc_Estado;
    }
}