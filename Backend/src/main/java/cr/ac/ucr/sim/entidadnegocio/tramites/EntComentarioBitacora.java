package cr.ac.ucr.sim.entidadnegocio.tramites;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 15/09/2026
 * Descripción: Comentario que la persona usuaria agrega a la bitácora de un expediente.
 *   No representa una tabla: transporta la entrada del servicio hacia la lógica de negocio.
 */
public class EntComentarioBitacora {

    private String vgc_Comentario;
    private Integer vgn_IdUsuario;

    public String getComentario() {
        return this.vgc_Comentario;
    }

    public void setComentario(String pvc_Comentario) {
        this.vgc_Comentario = pvc_Comentario;
    }

    public Integer getIdUsuario() {
        return this.vgn_IdUsuario;
    }

    public void setIdUsuario(Integer pvn_IdUsuario) {
        this.vgn_IdUsuario = pvn_IdUsuario;
    }
}
