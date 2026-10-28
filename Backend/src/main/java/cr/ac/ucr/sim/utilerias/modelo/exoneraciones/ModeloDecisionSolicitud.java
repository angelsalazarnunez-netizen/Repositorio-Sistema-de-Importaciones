package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Datos que envía la jefatura al registrar su decisión sobre una solicitud de exoneración
 *   (HU-022). El usuario responsable no viaja en la solicitud: se toma de la sesión.
 *   decision: APROBAR, RECHAZAR o MODIFICAR.
 */
public class ModeloDecisionSolicitud {

    private String vgc_Decision;
    private String vgc_Justificacion;

    public String getDecision() {
        return this.vgc_Decision;
    }

    public void setDecision(String pvc_Decision) {
        this.vgc_Decision = pvc_Decision;
    }

    public String getJustificacion() {
        return this.vgc_Justificacion;
    }

    public void setJustificacion(String pvc_Justificacion) {
        this.vgc_Justificacion = pvc_Justificacion;
    }
}
