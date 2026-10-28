package cr.ac.ucr.sim.entidadnegocio.tramites;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 23/09/2026
 * Descripción: Cambio de la referencia interna de un expediente. No representa una tabla:
 *   transporta la referencia nueva y la justificación del cambio. Cuando la sigla de la
 *   referencia nueva pertenece a otro tipo de trámite, el expediente se reclasifica y se
 *   reciben además los datos propios que exige ese tipo.
 */
public class EntCambioReferencia {

    private String vgc_ReferenciaInterna;
    private String vgc_Justificacion;
    private Integer vgn_IdUsuarioResponsable;
    private String vgc_OrigenDependencia;
    private String vgc_TipoExoneracion;
    private String vgc_NumeroContratacion;
    private String vgc_OrdenCompraGeco;
    private String vgc_ContratoSicop;

    public String getReferenciaInterna() {
        return this.vgc_ReferenciaInterna;
    }

    public void setReferenciaInterna(String pvc_ReferenciaInterna) {
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
    }

    public String getJustificacion() {
        return this.vgc_Justificacion;
    }

    public void setJustificacion(String pvc_Justificacion) {
        this.vgc_Justificacion = pvc_Justificacion;
    }

    public Integer getIdUsuarioResponsable() {
        return this.vgn_IdUsuarioResponsable;
    }

    public void setIdUsuarioResponsable(Integer pvn_IdUsuarioResponsable) {
        this.vgn_IdUsuarioResponsable = pvn_IdUsuarioResponsable;
    }

    public String getOrigenDependencia() {
        return this.vgc_OrigenDependencia;
    }

    public void setOrigenDependencia(String pvc_OrigenDependencia) {
        this.vgc_OrigenDependencia = pvc_OrigenDependencia;
    }

    public String getTipoExoneracion() {
        return this.vgc_TipoExoneracion;
    }

    public void setTipoExoneracion(String pvc_TipoExoneracion) {
        this.vgc_TipoExoneracion = pvc_TipoExoneracion;
    }

    public String getNumeroContratacion() {
        return this.vgc_NumeroContratacion;
    }

    public void setNumeroContratacion(String pvc_NumeroContratacion) {
        this.vgc_NumeroContratacion = pvc_NumeroContratacion;
    }

    public String getOrdenCompraGeco() {
        return this.vgc_OrdenCompraGeco;
    }

    public void setOrdenCompraGeco(String pvc_OrdenCompraGeco) {
        this.vgc_OrdenCompraGeco = pvc_OrdenCompraGeco;
    }

    public String getContratoSicop() {
        return this.vgc_ContratoSicop;
    }

    public void setContratoSicop(String pvc_ContratoSicop) {
        this.vgc_ContratoSicop = pvc_ContratoSicop;
    }
}
