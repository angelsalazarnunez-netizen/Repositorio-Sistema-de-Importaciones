package cr.ac.ucr.sim.entidadnegocio.tramites;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 22/09/2026
 * Descripción: Corrección de los datos de una solicitud de exoneración (HU-018). No representa
 *   una tabla: transporta los datos que la persona analista corrige en el expediente, después de
 *   que los compañeros agentes revisan la documentación. Los datos que no se indican se conservan
 *   como estaban.
 */
public class EntCorreccionExoneracion {

    private String vgc_TipoExoneracion;
    private String vgc_NumeroContratacion;
    private String vgc_OrdenCompraGeco;
    private String vgc_ContratoSicop;
    private String vgc_NumeroExonet;
    private String vgc_NumeroAutorizacionExonet;
    private String vgc_Observaciones;
    private Integer vgn_IdUsuarioResponsable;

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

    public String getNumeroExonet() {
        return this.vgc_NumeroExonet;
    }

    public void setNumeroExonet(String pvc_NumeroExonet) {
        this.vgc_NumeroExonet = pvc_NumeroExonet;
    }

    public String getNumeroAutorizacionExonet() {
        return this.vgc_NumeroAutorizacionExonet;
    }

    public void setNumeroAutorizacionExonet(String pvc_NumeroAutorizacionExonet) {
        this.vgc_NumeroAutorizacionExonet = pvc_NumeroAutorizacionExonet;
    }

    public String getTipoExoneracion() {
        return this.vgc_TipoExoneracion;
    }

    public void setTipoExoneracion(String pvc_TipoExoneracion) {
        this.vgc_TipoExoneracion = pvc_TipoExoneracion;
    }
}
