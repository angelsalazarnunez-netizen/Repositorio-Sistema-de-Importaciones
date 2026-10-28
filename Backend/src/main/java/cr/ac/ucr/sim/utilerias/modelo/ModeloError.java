package cr.ac.ucr.sim.utilerias.modelo;

import java.time.LocalDateTime;

/**
 * Descripcion: modelo utilizado para representar
 * respuestas de error de la API.
 */
public class ModeloError {

    private LocalDateTime vgd_FechaHora;
    private int vgn_EstadoHttp;
    private String vgc_Error;
    private String vgc_Mensaje;

    public ModeloError() {
    }

    public ModeloError(
            LocalDateTime vld_FechaHora,
            int vln_EstadoHttp,
            String vlc_Error,
            String vlc_Mensaje
    ) {
        this.vgd_FechaHora = vld_FechaHora;
        this.vgn_EstadoHttp = vln_EstadoHttp;
        this.vgc_Error = vlc_Error;
        this.vgc_Mensaje = vlc_Mensaje;
    }

    public LocalDateTime getFechaHora() {
        return vgd_FechaHora;
    }

    public void setFechaHora(LocalDateTime vld_FechaHora) {
        this.vgd_FechaHora = vld_FechaHora;
    }

    public int getEstadoHttp() {
        return vgn_EstadoHttp;
    }

    public void setEstadoHttp(int vln_EstadoHttp) {
        this.vgn_EstadoHttp = vln_EstadoHttp;
    }

    public String getError() {
        return vgc_Error;
    }

    public void setError(String vlc_Error) {
        this.vgc_Error = vlc_Error;
    }

    public String getMensaje() {
        return vgc_Mensaje;
    }

    public void setMensaje(String vlc_Mensaje) {
        this.vgc_Mensaje = vlc_Mensaje;
    }
}