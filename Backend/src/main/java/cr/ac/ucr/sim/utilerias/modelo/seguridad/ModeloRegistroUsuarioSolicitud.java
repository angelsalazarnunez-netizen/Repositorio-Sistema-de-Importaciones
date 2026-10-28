package cr.ac.ucr.sim.utilerias.modelo.seguridad;

import java.time.LocalDate;

/**
 * Descripcion: modelo de solicitud utilizado para
 * registrar un usuario en el sistema.
 */
public class ModeloRegistroUsuarioSolicitud {

    private String vgc_Nombre;
    private String vgc_PrimerApellido;
    private String vgc_SegundoApellido;
    private String vgc_NumeroCedula;
    private LocalDate vgd_FechaNacimiento;
    private String vgc_CorreoInstitucional;
    private String vgc_Contrasena;

    public ModeloRegistroUsuarioSolicitud() {
    }

    public String getNombre() {
        return vgc_Nombre;
    }

    public void setNombre(String vlc_Nombre) {
        this.vgc_Nombre = vlc_Nombre;
    }

    public String getPrimerApellido() {
        return vgc_PrimerApellido;
    }

    public void setPrimerApellido(String vlc_PrimerApellido) {
        this.vgc_PrimerApellido = vlc_PrimerApellido;
    }

    public String getSegundoApellido() {
        return vgc_SegundoApellido;
    }

    public void setSegundoApellido(String vlc_SegundoApellido) {
        this.vgc_SegundoApellido = vlc_SegundoApellido;
    }

    public String getNumeroCedula() {
        return vgc_NumeroCedula;
    }

    public void setNumeroCedula(String vlc_NumeroCedula) {
        this.vgc_NumeroCedula = vlc_NumeroCedula;
    }

    public LocalDate getFechaNacimiento() {
        return vgd_FechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate vld_FechaNacimiento) {
        this.vgd_FechaNacimiento = vld_FechaNacimiento;
    }

    public String getCorreoInstitucional() {
        return vgc_CorreoInstitucional;
    }

    public void setCorreoInstitucional(String vlc_CorreoInstitucional) {
        this.vgc_CorreoInstitucional = vlc_CorreoInstitucional;
    }

    public String getContrasena() {
        return vgc_Contrasena;
    }

    public void setContrasena(String vlc_Contrasena) {
        this.vgc_Contrasena = vlc_Contrasena;
    }
}