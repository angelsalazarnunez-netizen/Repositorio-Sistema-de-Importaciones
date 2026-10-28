package cr.ac.ucr.sim.utilerias.modelo.seguridad;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Descripcion: modelo de respuesta utilizado en el
 * registro y validacion de una cuenta de usuario.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main: el identificador de usuario es Integer.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModeloRegistroUsuarioRespuesta {

    private String vgc_Mensaje;
    private Integer vgn_IdUsuario;
    private String vgc_CorreoInstitucional;
    private String vgc_EstadoValidacion;
    private String vgc_EstadoAsignacionRol;
    private String vgc_EnlaceVerificacion;

    public ModeloRegistroUsuarioRespuesta() {
    }

    public String getMensaje() {
        return vgc_Mensaje;
    }

    public void setMensaje(String vlc_Mensaje) {
        this.vgc_Mensaje = vlc_Mensaje;
    }

    public Integer getIdUsuario() {
        return vgn_IdUsuario;
    }

    public void setIdUsuario(Integer vln_IdUsuario) {
        this.vgn_IdUsuario = vln_IdUsuario;
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

    public String getEstadoAsignacionRol() {
        return vgc_EstadoAsignacionRol;
    }

    public void setEstadoAsignacionRol(String vlc_EstadoAsignacionRol) {
        this.vgc_EstadoAsignacionRol = vlc_EstadoAsignacionRol;
    }

    public String getEnlaceVerificacion() {
        return vgc_EnlaceVerificacion;
    }

    public void setEnlaceVerificacion(String vlc_EnlaceVerificacion) {
        this.vgc_EnlaceVerificacion = vlc_EnlaceVerificacion;
    }
}