package cr.ac.ucr.sim.utilerias.modelo.seguridad;

import java.util.List;

/**
 * Descripcion: modelo de respuesta con la informacion
 * de un rol y sus permisos asociados.
 */
public class ModeloRolRespuesta {

    private Long vgn_IdRol;
    private String vgc_Nombre;
    private String vgc_Descripcion;
    private String vgc_Estado;
    private List<ModeloPermisoRespuesta> vgl_Permisos;

    public ModeloRolRespuesta() {
    }

    public Long getIdRol() {
        return vgn_IdRol;
    }

    public void setIdRol(Long vln_IdRol) {
        this.vgn_IdRol = vln_IdRol;
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

    public String getEstado() {
        return vgc_Estado;
    }

    public void setEstado(String vlc_Estado) {
        this.vgc_Estado = vlc_Estado;
    }

    public List<ModeloPermisoRespuesta> getPermisos() {
        return vgl_Permisos;
    }

    public void setPermisos(
            List<ModeloPermisoRespuesta> vll_Permisos
    ) {
        this.vgl_Permisos = vll_Permisos;
    }
}