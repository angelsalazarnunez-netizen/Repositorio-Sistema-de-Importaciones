package cr.ac.ucr.sim.utilerias.modelo.seguridad;

/**
 * Descripcion: modelo utilizado para solicitar
 * el cambio de estado de un registro.
 */
public class ModeloEstadoSolicitud {

    private String vgc_Estado;

    public ModeloEstadoSolicitud() {
    }

    public String getEstado() {
        return vgc_Estado;
    }

    public void setEstado(String vlc_Estado) {
        this.vgc_Estado = vlc_Estado;
    }
}