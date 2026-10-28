package cr.ac.ucr.sim.entidadnegocio.tramites;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 23/09/2026
 * Descripción: Respuesta del cambio de referencia de un expediente. Informa la referencia
 *   anterior y la nueva, el tipo de trámite al que quedó el expediente y si hubo
 *   reclasificación, para que la pantalla lleve a la persona usuaria al expediente correcto.
 */
public class EntRespuestaCambioReferencia {

    private String vgc_Mensaje;
    private String vgc_ReferenciaAnterior;
    private String vgc_ReferenciaInterna;
    private String vgc_TipoTramiteAnterior;
    private String vgc_TipoTramite;
    private String vgc_CodigoTipoTramite;
    private String vgc_Estado;
    private boolean vgb_Reclasificado;

    public String getMensaje() {
        return this.vgc_Mensaje;
    }

    public void setMensaje(String pvc_Mensaje) {
        this.vgc_Mensaje = pvc_Mensaje;
    }

    public String getReferenciaAnterior() {
        return this.vgc_ReferenciaAnterior;
    }

    public void setReferenciaAnterior(String pvc_ReferenciaAnterior) {
        this.vgc_ReferenciaAnterior = pvc_ReferenciaAnterior;
    }

    public String getReferenciaInterna() {
        return this.vgc_ReferenciaInterna;
    }

    public void setReferenciaInterna(String pvc_ReferenciaInterna) {
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
    }

    public String getTipoTramiteAnterior() {
        return this.vgc_TipoTramiteAnterior;
    }

    public void setTipoTramiteAnterior(String pvc_TipoTramiteAnterior) {
        this.vgc_TipoTramiteAnterior = pvc_TipoTramiteAnterior;
    }

    public String getTipoTramite() {
        return this.vgc_TipoTramite;
    }

    public void setTipoTramite(String pvc_TipoTramite) {
        this.vgc_TipoTramite = pvc_TipoTramite;
    }

    public String getCodigoTipoTramite() {
        return this.vgc_CodigoTipoTramite;
    }

    public void setCodigoTipoTramite(String pvc_CodigoTipoTramite) {
        this.vgc_CodigoTipoTramite = pvc_CodigoTipoTramite;
    }

    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }

    public boolean isReclasificado() {
        return this.vgb_Reclasificado;
    }

    public void setReclasificado(boolean pvb_Reclasificado) {
        this.vgb_Reclasificado = pvb_Reclasificado;
    }
}
