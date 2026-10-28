package cr.ac.ucr.sim.utilerias.modelo.seguridad;

import java.time.LocalDateTime;

/**
 * Descripcion: modelo de respuesta que representa
 * una asignacion de rol a un usuario.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main: los identificadores de usuario son Integer.
 */
public class ModeloUsuarioRolRespuesta {

    private Long vgn_IdUsuarioRol;
    private Integer vgn_IdUsuario;
    private Long vgn_IdRol;
    private String vgc_NombreRol;
    private Integer vgn_IdUsuarioAsignador;
    private LocalDateTime vgd_FechaAsignacion;
    private LocalDateTime vgd_FechaFinalizacion;
    private String vgc_Motivo;
    private String vgc_Estado;
    private Integer vgn_IdUsuarioFinalizador;

    public ModeloUsuarioRolRespuesta() {
    }

    public Long getIdUsuarioRol() {
        return vgn_IdUsuarioRol;
    }

    public void setIdUsuarioRol(Long vln_IdUsuarioRol) {
        this.vgn_IdUsuarioRol = vln_IdUsuarioRol;
    }

    public Integer getIdUsuario() {
        return vgn_IdUsuario;
    }

    public void setIdUsuario(Integer vln_IdUsuario) {
        this.vgn_IdUsuario = vln_IdUsuario;
    }

    public Long getIdRol() {
        return vgn_IdRol;
    }

    public void setIdRol(Long vln_IdRol) {
        this.vgn_IdRol = vln_IdRol;
    }

    public String getNombreRol() {
        return vgc_NombreRol;
    }

    public void setNombreRol(String vlc_NombreRol) {
        this.vgc_NombreRol = vlc_NombreRol;
    }

    public Integer getIdUsuarioAsignador() {
        return vgn_IdUsuarioAsignador;
    }

    public void setIdUsuarioAsignador(
            Integer vln_IdUsuarioAsignador
    ) {
        this.vgn_IdUsuarioAsignador =
                vln_IdUsuarioAsignador;
    }

    public LocalDateTime getFechaAsignacion() {
        return vgd_FechaAsignacion;
    }

    public void setFechaAsignacion(
            LocalDateTime vld_FechaAsignacion
    ) {
        this.vgd_FechaAsignacion =
                vld_FechaAsignacion;
    }

    public LocalDateTime getFechaFinalizacion() {
        return vgd_FechaFinalizacion;
    }

    public void setFechaFinalizacion(
            LocalDateTime vld_FechaFinalizacion
    ) {
        this.vgd_FechaFinalizacion =
                vld_FechaFinalizacion;
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

    public Integer getIdUsuarioFinalizador() {
        return vgn_IdUsuarioFinalizador;
    }

    public void setIdUsuarioFinalizador(Integer vln_IdUsuarioFinalizador) {
        this.vgn_IdUsuarioFinalizador =
                vln_IdUsuarioFinalizador;
    }

}