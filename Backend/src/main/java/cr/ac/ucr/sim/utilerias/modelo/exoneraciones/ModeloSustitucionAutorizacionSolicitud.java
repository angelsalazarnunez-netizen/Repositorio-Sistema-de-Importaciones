package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Datos que envía la pantalla para sustituir la autorización EXONET vigente
 *   (HU-023): los datos completos de la nueva autorización más el motivo de la sustitución.
 *   Viaja como multipart/form-data junto con el archivo opcional de la nueva. La nueva no hereda
 *   datos ni documento de la anterior: la pantalla puede precargarlos, pero se envían completos.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
public class ModeloSustitucionAutorizacionSolicitud extends ModeloAutorizacionExonetSolicitud {

    private String vgc_Motivo;

    public ModeloSustitucionAutorizacionSolicitud() {
    }

    /**
     * Motivo de la sustitución. Es obligatorio; queda registrado en la autorización sustituida.
     */
    public String getMotivo() {
        return this.vgc_Motivo;
    }

    public void setMotivo(String pvc_Motivo) {
        this.vgc_Motivo = pvc_Motivo;
    }
}
