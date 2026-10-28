package cr.ac.ucr.sim.utilerias.modelo.seguridad;

/**
 * Descripcion: modelo de entrada para retirar
 * un rol asignado a un usuario.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main: el identificador de usuario es Integer. El
 *     servidor ya no usa idUsuarioFinalizador: toma el usuario de la sesión.
 */
public class ModeloRetiroRolSolicitud {

    private Integer vgn_IdUsuarioFinalizador;
    private String vgc_Motivo;

    public ModeloRetiroRolSolicitud() {
    }

    public Integer getIdUsuarioFinalizador() {
        return vgn_IdUsuarioFinalizador;
    }

    public void setIdUsuarioFinalizador(
            Integer vln_IdUsuarioFinalizador
    ) {
        this.vgn_IdUsuarioFinalizador =
                vln_IdUsuarioFinalizador;
    }

    public String getMotivo() {
        return vgc_Motivo;
    }

    public void setMotivo(String vlc_Motivo) {
        this.vgc_Motivo = vlc_Motivo;
    }
}