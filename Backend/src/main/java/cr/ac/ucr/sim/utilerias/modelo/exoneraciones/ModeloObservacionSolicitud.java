package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 04/10/2026
 * Descripción: Datos que envía la pantalla para crear o modificar una observación del
 *   expediente (HU-020). No incluye el usuario: el servicio lo toma de la sesión autenticada.
 *   La fecha, el estado y el responsable los asigna el servidor.
 *
 * Historial de cambios:
 *   04/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
public class ModeloObservacionSolicitud {

    private String vgc_Motivo;
    private String vgc_Observacion;
    private Boolean vgb_Confirmar;

    public ModeloObservacionSolicitud() {
    }

    /**
     * Motivo opcional de la observación, en texto libre.
     */
    public String getMotivo() {
        return this.vgc_Motivo;
    }

    public void setMotivo(String pvc_Motivo) {
        this.vgc_Motivo = pvc_Motivo;
    }

    /**
     * Texto de la observación o aclaración.
     */
    public String getObservacion() {
        return this.vgc_Observacion;
    }

    public void setObservacion(String pvc_Observacion) {
        this.vgc_Observacion = pvc_Observacion;
    }

    /**
     * Solo al crear: true registra la observación directamente como confirmada. Si se omite o es
     *   false queda como borrador. Al modificar un borrador se ignora: confirmar tiene su propio
     *   servicio.
     */
    public Boolean getConfirmar() {
        return this.vgb_Confirmar;
    }

    public void setConfirmar(Boolean pvb_Confirmar) {
        this.vgb_Confirmar = pvb_Confirmar;
    }
}
