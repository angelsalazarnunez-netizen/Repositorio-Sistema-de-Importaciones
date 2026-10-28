package cr.ac.ucr.sim.entidadnegocio.tramites;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 25/09/2026
 * Descripción: Gestión documental del expediente (HU-045). No representa una tabla: transporta
 *   la clasificación que se le da a un documento o el estado documental que la persona analista
 *   determina para el expediente, junto con sus observaciones y el usuario responsable.
 */
public class EntEstadoDocumental {

    private String vgc_EstadoDocumental;
    private String vgc_TipoDocumento;
    private String vgc_Observaciones;
    private Integer vgn_IdUsuarioResponsable;

    public String getEstadoDocumental() {
        return this.vgc_EstadoDocumental;
    }

    public void setEstadoDocumental(String pvc_EstadoDocumental) {
        this.vgc_EstadoDocumental = pvc_EstadoDocumental;
    }

    public String getTipoDocumento() {
        return this.vgc_TipoDocumento;
    }

    public void setTipoDocumento(String pvc_TipoDocumento) {
        this.vgc_TipoDocumento = pvc_TipoDocumento;
    }

    public String getObservaciones() {
        return this.vgc_Observaciones;
    }

    public void setObservaciones(String pvc_Observaciones) {
        this.vgc_Observaciones = pvc_Observaciones;
    }

    public Integer getIdUsuarioResponsable() {
        return this.vgn_IdUsuarioResponsable;
    }

    public void setIdUsuarioResponsable(Integer pvn_IdUsuarioResponsable) {
        this.vgn_IdUsuarioResponsable = pvn_IdUsuarioResponsable;
    }
}
