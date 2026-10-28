package cr.ac.ucr.sim.utilerias.modelo.seguridad;

import java.util.List;

/**
 * Descripcion: modelo de entrada para registrar
 * o actualizar un rol del sistema.
 */
public class ModeloRolSolicitud {

    private String vgc_Nombre;
    private String vgc_Descripcion;
    private List<Long> vgl_IdPermisos;

    public ModeloRolSolicitud() {
    }

    public String getNombre() {
        return vgc_Nombre;
    }

    public void setNombre(String vlc_Nombre) {
        this.vgc_Nombre = vlc_Nombre;
    }

    public String getDescripcion() {
        return vgc_Descripcion;
    }

    public void setDescripcion(String vlc_Descripcion) {
        this.vgc_Descripcion = vlc_Descripcion;
    }

    public List<Long> getIdPermisos() {
        return vgl_IdPermisos;
    }

    public void setIdPermisos(List<Long> vll_IdPermisos) {
        this.vgl_IdPermisos = vll_IdPermisos;
    }
}