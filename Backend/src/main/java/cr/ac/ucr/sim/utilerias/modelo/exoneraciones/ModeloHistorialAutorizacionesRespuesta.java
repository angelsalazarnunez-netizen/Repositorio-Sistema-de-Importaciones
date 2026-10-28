package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Respuesta de la consulta de autorizaciones EXONET de un expediente (HU-023): la
 *   vigente, el historial completo y, solo mientras no exista historial, la precarga con los
 *   números registrados en la HU-018.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
public class ModeloHistorialAutorizacionesRespuesta {

    private Integer vgn_IdTramite;
    private ModeloAutorizacionExonetRespuesta vgo_Vigente;
    private List<ModeloAutorizacionExonetRespuesta> vgo_LsHistorial = new ArrayList<>();
    private ModeloPrecargaAutorizacionRespuesta vgo_Precarga;

    public ModeloHistorialAutorizacionesRespuesta() {
    }

    /**
     * Expediente de exoneración consultado.
     */
    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    /**
     * Autorización vigente, o null si el expediente no tiene ninguna (nunca registrada o la
     *   última fue anulada). Es la misma información que su fila del historial.
     */
    public ModeloAutorizacionExonetRespuesta getVigente() {
        return this.vgo_Vigente;
    }

    public void setVigente(ModeloAutorizacionExonetRespuesta pvo_Vigente) {
        this.vgo_Vigente = pvo_Vigente;
    }

    /**
     * Todas las autorizaciones del expediente, vigente, anuladas y sustituidas, de la más antigua
     *   a la más reciente según su fecha de registro.
     */
    public List<ModeloAutorizacionExonetRespuesta> getHistorial() {
        return this.vgo_LsHistorial;
    }

    public void setHistorial(List<ModeloAutorizacionExonetRespuesta> pvo_LsHistorial) {
        this.vgo_LsHistorial = pvo_LsHistorial;
    }

    /**
     * Números EXONET de la HU-018 para precargar el primer registro, o null si el expediente ya
     *   tiene historial o no tiene números antiguos.
     */
    public ModeloPrecargaAutorizacionRespuesta getPrecarga() {
        return this.vgo_Precarga;
    }

    public void setPrecarga(ModeloPrecargaAutorizacionRespuesta pvo_Precarga) {
        this.vgo_Precarga = pvo_Precarga;
    }
}
