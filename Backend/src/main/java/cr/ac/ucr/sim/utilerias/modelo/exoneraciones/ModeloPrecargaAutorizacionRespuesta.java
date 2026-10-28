package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Números EXONET registrados en la solicitud de exoneración (HU-018) que la
 *   pantalla de la HU-023 puede ofrecer como precarga del primer registro (K-6). Solo se envía
 *   cuando el expediente todavía no tiene ninguna autorización registrada. Es informativa: no
 *   representa una autorización ni determina cuál está vigente.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
public class ModeloPrecargaAutorizacionRespuesta {

    private String vgc_NumeroSolicitudExonet;
    private String vgc_NumeroAutorizacion;

    public ModeloPrecargaAutorizacionRespuesta() {
    }

    /**
     * Número de solicitud EXONET registrado en la HU-018, o null.
     */
    public String getNumeroSolicitudExonet() {
        return this.vgc_NumeroSolicitudExonet;
    }

    public void setNumeroSolicitudExonet(String pvc_NumeroSolicitudExonet) {
        this.vgc_NumeroSolicitudExonet = pvc_NumeroSolicitudExonet;
    }

    /**
     * Número de autorización registrado en la HU-018, o null.
     */
    public String getNumeroAutorizacion() {
        return this.vgc_NumeroAutorizacion;
    }

    public void setNumeroAutorizacion(String pvc_NumeroAutorizacion) {
        this.vgc_NumeroAutorizacion = pvc_NumeroAutorizacion;
    }
}
