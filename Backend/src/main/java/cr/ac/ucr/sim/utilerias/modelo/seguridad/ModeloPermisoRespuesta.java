package cr.ac.ucr.sim.utilerias.modelo.seguridad;

/**
 * Descripcion: modelo de respuesta que representa
 * un permiso asociado a un rol.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: se agregan el código y el nombre del módulo de acceso del permiso, para agrupar
 *     los permisos en la pantalla de roles (HU-003).
 */
public class ModeloPermisoRespuesta {

    private Long vgn_IdPermiso;
    private String vgc_Codigo;
    private String vgc_Nombre;
    private String vgc_Descripcion;
    private String vgc_Modulo;
    private String vgc_NombreModulo;

    public ModeloPermisoRespuesta() {
    }

    public Long getIdPermiso() {
        return vgn_IdPermiso;
    }

    public void setIdPermiso(Long vln_IdPermiso) {
        this.vgn_IdPermiso = vln_IdPermiso;
    }

    public String getCodigo() {
        return vgc_Codigo;
    }

    public void setCodigo(String vlc_Codigo) {
        this.vgc_Codigo = vlc_Codigo;
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

    public String getModulo() {
        return vgc_Modulo;
    }

    public void setModulo(String vlc_Modulo) {
        this.vgc_Modulo = vlc_Modulo;
    }

    public String getNombreModulo() {
        return vgc_NombreModulo;
    }

    public void setNombreModulo(String vlc_NombreModulo) {
        this.vgc_NombreModulo = vlc_NombreModulo;
    }
}