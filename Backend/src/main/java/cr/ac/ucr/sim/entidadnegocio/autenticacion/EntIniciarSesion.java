package cr.ac.ucr.sim.entidadnegocio.autenticacion;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EntIniciarSesion {

    @NotBlank(message = "El correo institucional es obligatorio.")
    @Email(message = "Digite un correo institucional válido.")
    private String vgc_CorreoInstitucional;

    @NotBlank(message = "La contraseña es obligatoria.")
    private String vgc_Contrasena;

    @JsonProperty("correoInstitucional")
    public String getCorreoInstitucional() {
        return this.vgc_CorreoInstitucional;
    }

    public void setCorreoInstitucional(String pvc_CorreoInstitucional) {
        this.vgc_CorreoInstitucional = pvc_CorreoInstitucional;
    }

    @JsonProperty("contrasena")
    public String getContrasena() {
        return this.vgc_Contrasena;
    }

    public void setContrasena(String pvc_Contrasena) {
        this.vgc_Contrasena = pvc_Contrasena;
    }
}
