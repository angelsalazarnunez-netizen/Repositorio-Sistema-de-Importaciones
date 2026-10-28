package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Datos que envía la pantalla para anular la autorización EXONET vigente (HU-023).
 *   Viaja como JSON. No incluye el usuario ni la fecha: los asigna el servidor.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
public class ModeloAnulacionAutorizacionSolicitud {

    private String vgc_Motivo;

    public ModeloAnulacionAutorizacionSolicitud() {
    }

    /**
     * Motivo de la anulación. Es obligatorio; queda registrado en la autorización anulada.
     */
    public String getMotivo() {
        return this.vgc_Motivo;
    }

    public void setMotivo(String pvc_Motivo) {
        this.vgc_Motivo = pvc_Motivo;
    }
}
