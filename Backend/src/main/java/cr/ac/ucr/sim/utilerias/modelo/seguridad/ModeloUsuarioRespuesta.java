package cr.ac.ucr.sim.utilerias.modelo.seguridad;

/**
 * Descripcion: modelo de respuesta para consultar un usuario desde el modulo
 * de seguridad.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main: el identificador de usuario es Integer.
 */
public class ModeloUsuarioRespuesta {

    private Integer vgn_IdUsuario;
    private String vgc_NumeroCedula;
    private String vgc_NombreCompleto;
    private String vgc_CorreoInstitucional;
    private String vgc_EstadoValidacion;
    private String vgc_Estado;

    public ModeloUsuarioRespuesta() {
    }

    public Integer getIdUsuario() {
        return vgn_IdUsuario;
    }

    public void setIdUsuario(Integer vln_IdUsuario) {
        this.vgn_IdUsuario = vln_IdUsuario;
    }

    public String getNumeroCedula() {
        return vgc_NumeroCedula;
    }

    public void setNumeroCedula(String vlc_NumeroCedula) {
        this.vgc_NumeroCedula = vlc_NumeroCedula;
    }

    public String getNombreCompleto() {
        return vgc_NombreCompleto;
    }

    public void setNombreCompleto(String vlc_NombreCompleto) {
        this.vgc_NombreCompleto = vlc_NombreCompleto;
    }

    public String getCorreoInstitucional() {
        return vgc_CorreoInstitucional;
    }

    public void setCorreoInstitucional(String vlc_CorreoInstitucional) {
        this.vgc_CorreoInstitucional = vlc_CorreoInstitucional;
    }

    public String getEstadoValidacion() {
        return vgc_EstadoValidacion;
    }

    public void setEstadoValidacion(String vlc_EstadoValidacion) {
        this.vgc_EstadoValidacion = vlc_EstadoValidacion;
    }

    public String getEstado() {
        return vgc_Estado;
    }

    public void setEstado(String vlc_Estado) {
        this.vgc_Estado = vlc_Estado;
    }
}