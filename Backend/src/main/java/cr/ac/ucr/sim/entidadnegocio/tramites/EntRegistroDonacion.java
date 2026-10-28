package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.math.BigDecimal;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 10/09/2026
 * Descripción: Datos que envía el formulario de registro de la donación.
 *   No representa una tabla: transporta la entrada del caso de uso hacia la lógica de negocio.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: la referencia FUNDEVI pasa a llamarse referencia de la Fundación UCR.
 */
public class EntRegistroDonacion {

    private String vgc_ReferenciaInterna;
    private String vgc_TipoDonacion;
    private String vgc_NombreDonante;
    private String vgc_PaisOrigen;
    private String vgc_Moneda;
    private BigDecimal vgn_ValorEstimado;
    private String vgc_ReferenciaFundacionUcr;
    private String vgc_Observaciones;
    private Integer vgn_IdUsuarioResponsable;

    public String getReferenciaInterna() {
        return this.vgc_ReferenciaInterna;
    }

    public void setReferenciaInterna(String pvc_ReferenciaInterna) {
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
    }

    public String getTipoDonacion() {
        return this.vgc_TipoDonacion;
    }

    public void setTipoDonacion(String pvc_TipoDonacion) {
        this.vgc_TipoDonacion = pvc_TipoDonacion;
    }

    public String getNombreDonante() {
        return this.vgc_NombreDonante;
    }

    public void setNombreDonante(String pvc_NombreDonante) {
        this.vgc_NombreDonante = pvc_NombreDonante;
    }

    public String getPaisOrigen() {
        return this.vgc_PaisOrigen;
    }

    public void setPaisOrigen(String pvc_PaisOrigen) {
        this.vgc_PaisOrigen = pvc_PaisOrigen;
    }

    public String getMoneda() {
        return this.vgc_Moneda;
    }

    public void setMoneda(String pvc_Moneda) {
        this.vgc_Moneda = pvc_Moneda;
    }

    public BigDecimal getValorEstimado() {
        return this.vgn_ValorEstimado;
    }

    public void setValorEstimado(BigDecimal pvn_ValorEstimado) {
        this.vgn_ValorEstimado = pvn_ValorEstimado;
    }

    public String getReferenciaFundacionUcr() {
        return this.vgc_ReferenciaFundacionUcr;
    }

    public void setReferenciaFundacionUcr(String pvc_ReferenciaFundacionUcr) {
        this.vgc_ReferenciaFundacionUcr = pvc_ReferenciaFundacionUcr;
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
