package cr.ac.ucr.sim.utilerias.modelo.seguridad;

import java.util.List;

/**
 * Descripcion: modelo de entrada para asignar
 * uno o varios roles a un usuario del sistema.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main: los identificadores de usuario son Integer. El
 *     servidor ya no usa idUsuarioAsignador: toma el usuario de la sesión.
 */
public class ModeloAsignacionRolSolicitud {

    private Integer vgn_IdUsuario;
    private Integer vgn_IdUsuarioAsignador;
    private List<Long> vgl_IdRoles;
    private String vgc_Motivo;

    public ModeloAsignacionRolSolicitud() {
    }

    public Integer getIdUsuario() {
        return vgn_IdUsuario;
    }

    public void setIdUsuario(Integer vln_IdUsuario) {
        this.vgn_IdUsuario = vln_IdUsuario;
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

    public List<Long> getIdRoles() {
        return vgl_IdRoles;
    }

    public void setIdRoles(
            List<Long> vll_IdRoles
    ) {
        this.vgl_IdRoles = vll_IdRoles;
    }

    public String getMotivo() {
        return vgc_Motivo;
    }

    public void setMotivo(String vlc_Motivo) {
        this.vgc_Motivo = vlc_Motivo;
    }
}