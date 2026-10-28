package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.math.BigDecimal;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 26/09/2026
 * Descripción: Datos de costo de un expediente (HU-036). No representa una tabla: transporta el
 *   partido al que se le carga el gasto, los impuestos pagados y exonerados y el monto de
 *   bodegaje, que son los montos que totaliza el reporte de consultas internas. Los datos que no
 *   se indican se conservan como estaban.
 */
public class EntCostosTramite {

    private String vgc_TipoPartidoResponsable;
    private String vgc_PartidoResponsable;
    private BigDecimal vgn_ImpuestosPagados;
    private BigDecimal vgn_ImpuestosExonerados;
    private BigDecimal vgn_MontoBodegaje;
    private Integer vgn_IdUsuarioResponsable;

    public String getPartidoResponsable() {
        return this.vgc_PartidoResponsable;
    }

    public void setPartidoResponsable(String pvc_PartidoResponsable) {
        this.vgc_PartidoResponsable = pvc_PartidoResponsable;
    }

    public BigDecimal getImpuestosPagados() {
        return this.vgn_ImpuestosPagados;
    }

    public void setImpuestosPagados(BigDecimal pvn_ImpuestosPagados) {
        this.vgn_ImpuestosPagados = pvn_ImpuestosPagados;
    }

    public BigDecimal getImpuestosExonerados() {
        return this.vgn_ImpuestosExonerados;
    }

    public void setImpuestosExonerados(BigDecimal pvn_ImpuestosExonerados) {
        this.vgn_ImpuestosExonerados = pvn_ImpuestosExonerados;
    }

    public BigDecimal getMontoBodegaje() {
        return this.vgn_MontoBodegaje;
    }

    public void setMontoBodegaje(BigDecimal pvn_MontoBodegaje) {
        this.vgn_MontoBodegaje = pvn_MontoBodegaje;
    }

    public Integer getIdUsuarioResponsable() {
        return this.vgn_IdUsuarioResponsable;
    }

    public void setIdUsuarioResponsable(Integer pvn_IdUsuarioResponsable) {
        this.vgn_IdUsuarioResponsable = pvn_IdUsuarioResponsable;
    }

    public String getTipoPartidoResponsable() {
        return this.vgc_TipoPartidoResponsable;
    }

    public void setTipoPartidoResponsable(String pvc_TipoPartidoResponsable) {
        this.vgc_TipoPartidoResponsable = pvc_TipoPartidoResponsable;
    }
}
