package cr.ac.ucr.sim.entidadnegocio.autenticacion;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class EntRespuestaSesion {

    private Integer vgn_IdUsuario;
    private String vgc_NombreCompleto;
    private String vgc_CorreoInstitucional;
    private String vgc_Token;
    private String vgc_Mensaje;
    private List<String> vgo_Roles = new ArrayList<>();
    private List<String> vgo_Permisos = new ArrayList<>();

    @JsonProperty("idUsuario")
    public Integer getIdUsuario() {
        return this.vgn_IdUsuario;
    }

    public void setIdUsuario(Integer pvn_IdUsuario) {
        this.vgn_IdUsuario = pvn_IdUsuario;
    }

    @JsonProperty("nombreCompleto")
    public String getNombreCompleto() {
        return this.vgc_NombreCompleto;
    }

    public void setNombreCompleto(String pvc_NombreCompleto) {
        this.vgc_NombreCompleto = pvc_NombreCompleto;
    }

    @JsonProperty("correoInstitucional")
    public String getCorreoInstitucional() {
        return this.vgc_CorreoInstitucional;
    }

    public void setCorreoInstitucional(String pvc_CorreoInstitucional) {
        this.vgc_CorreoInstitucional = pvc_CorreoInstitucional;
    }

    @JsonProperty("token")
    public String getToken() {
        return this.vgc_Token;
    }

    public void setToken(String pvc_Token) {
        this.vgc_Token = pvc_Token;
    }

    @JsonProperty("mensaje")
    public String getMensaje() {
        return this.vgc_Mensaje;
    }

    public void setMensaje(String pvc_Mensaje) {
        this.vgc_Mensaje = pvc_Mensaje;
    }

    @JsonProperty("roles")
    public List<String> getRoles() {
        return this.vgo_Roles;
    }

    public void setRoles(List<String> pvo_Roles) {
        this.vgo_Roles = pvo_Roles == null ? new ArrayList<>() : pvo_Roles;
    }

    @JsonProperty("permisos")
    public List<String> getPermisos() {
        return this.vgo_Permisos;
    }

    public void setPermisos(List<String> pvo_Permisos) {
        this.vgo_Permisos = pvo_Permisos == null ? new ArrayList<>() : pvo_Permisos;
    }

    @JsonProperty("accesoConcedido")
    public boolean isAccesoConcedido() {
        return this.vgc_Token != null && !this.vgc_Token.isBlank();
    }
}
