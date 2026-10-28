package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.math.BigDecimal;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Datos que envía el formulario de registro del desalmacenaje especial.
 *   No representa una tabla: transporta la entrada del caso de uso hacia la lógica de negocio.
 */
public class EntRegistroDesalmacenaje {

    private String vgc_ReferenciaInterna;
    private String vgc_OrigenDependencia;
    private String vgc_PaisOrigen;
    private String vgc_Moneda;
    private BigDecimal vgn_ValorEstimado;
    private String vgc_Justificacion;
    private Integer vgn_IdUsuarioResponsable;

    public String getReferenciaInterna() {
        return this.vgc_ReferenciaInterna;
    }

    public void setReferenciaInterna(String pvc_ReferenciaInterna) {
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
    }

    public String getOrigenDependencia() {
        return this.vgc_OrigenDependencia;
    }

    public void setOrigenDependencia(String pvc_OrigenDependencia) {
        this.vgc_OrigenDependencia = pvc_OrigenDependencia;
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
}
