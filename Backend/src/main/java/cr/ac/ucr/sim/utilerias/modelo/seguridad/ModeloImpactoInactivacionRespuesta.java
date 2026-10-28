package cr.ac.ucr.sim.utilerias.modelo.seguridad;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 12/10/2026
 * Descripción: modelo de respuesta con el efecto de inactivar un rol: cuántas personas lo tienen
 *   asignado y cuántas quedarían sin ningún rol activo (heurística H2, EH5-01). La pantalla lo
 *   usa para advertir antes de confirmar la inactivación.
 */
public class ModeloImpactoInactivacionRespuesta {

    private Long vgn_IdRol;
    private long vgn_UsuariosConRol;
    private long vgn_UsuariosSinRolesActivos;

    public ModeloImpactoInactivacionRespuesta() {
    }

    public Long getIdRol() {
        return vgn_IdRol;
    }

    public void setIdRol(Long vln_IdRol) {
        this.vgn_IdRol = vln_IdRol;
    }

    public long getUsuariosConRol() {
        return vgn_UsuariosConRol;
    }

    public void setUsuariosConRol(long vln_UsuariosConRol) {
        this.vgn_UsuariosConRol = vln_UsuariosConRol;
    }

    public long getUsuariosSinRolesActivos() {
        return vgn_UsuariosSinRolesActivos;
    }

    public void setUsuariosSinRolesActivos(long vln_UsuariosSinRolesActivos) {
        this.vgn_UsuariosSinRolesActivos = vln_UsuariosSinRolesActivos;
    }
}
