package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 25/09/2026
 * Descripción: Trámite tal como lo enumera el reporte de consultas internas (HU-036). No
 *   representa una tabla: reúne de cada expediente lo que el reporte resume y desglosa.
 */
public class EntTramiteReporte {

    private final String vgc_ReferenciaInterna;
    private final String vgc_TipoTramite;
    private final String vgc_RegimenAduanero;
    private final String vgc_Estado;
    private final String vgc_Moneda;
    private final BigDecimal vgn_ValorCif;
    private final String vgc_OrigenDependencia;
    private final String vgc_PartidoResponsable;
    private final BigDecimal vgn_ImpuestosPagados;
    private final BigDecimal vgn_ImpuestosExonerados;
    private final BigDecimal vgn_MontoBodegaje;
    private final String vgc_TipoExoneracion;
    private final LocalDateTime vgd_FechaCreacion;
    private final Long vgn_CantidadPermisos;

    public EntTramiteReporte(
            String pvc_ReferenciaInterna,
            String pvc_TipoTramite,
            String pvc_RegimenAduanero,
            String pvc_Estado,
            String pvc_Moneda,
            BigDecimal pvn_ValorCif,
            String pvc_OrigenDependencia,
            String pvc_PartidoResponsable,
            BigDecimal pvn_ImpuestosPagados,
            BigDecimal pvn_ImpuestosExonerados,
            BigDecimal pvn_MontoBodegaje,
            String pvc_TipoExoneracion,
            LocalDateTime pvd_FechaCreacion,
            Long pvn_CantidadPermisos) {
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
        this.vgc_TipoTramite = pvc_TipoTramite;
        this.vgc_RegimenAduanero = pvc_RegimenAduanero;
        this.vgc_Estado = pvc_Estado;
        this.vgc_Moneda = pvc_Moneda;
        this.vgn_ValorCif = pvn_ValorCif;
        this.vgc_OrigenDependencia = pvc_OrigenDependencia;
        this.vgc_PartidoResponsable = pvc_PartidoResponsable;
        this.vgn_ImpuestosPagados = pvn_ImpuestosPagados;
        this.vgn_ImpuestosExonerados = pvn_ImpuestosExonerados;
        this.vgn_MontoBodegaje = pvn_MontoBodegaje;
        this.vgc_TipoExoneracion = pvc_TipoExoneracion;
        this.vgd_FechaCreacion = pvd_FechaCreacion;
        this.vgn_CantidadPermisos = pvn_CantidadPermisos;
    }

    @JsonProperty("referenciaInterna")
    public String getReferenciaInterna() {
        return this.vgc_ReferenciaInterna;
    }

    @JsonProperty("tipoTramite")
    public String getTipoTramite() {
        return this.vgc_TipoTramite;
    }

    @JsonProperty("regimenAduanero")
    public String getRegimenAduanero() {
        return this.vgc_RegimenAduanero;
    }

    @JsonProperty("estado")
    public String getEstado() {
        return this.vgc_Estado;
    }

    @JsonProperty("moneda")
    public String getMoneda() {
        return this.vgc_Moneda;
    }

    @JsonProperty("valorCif")
    public BigDecimal getValorCif() {
        return this.vgn_ValorCif;
    }

    @JsonProperty("origenDependencia")
    public String getOrigenDependencia() {
        return this.vgc_OrigenDependencia;
    }

    @JsonProperty("tipoExoneracion")
    public String getTipoExoneracion() {
        return this.vgc_TipoExoneracion;
    }

    @JsonProperty("fechaCreacion")
    public LocalDateTime getFechaCreacion() {
        return this.vgd_FechaCreacion;
    }

    @JsonProperty("cantidadPermisos")
    public Long getCantidadPermisos() {
        return this.vgn_CantidadPermisos;
    }

    @JsonProperty("partidoResponsable")
    public String getPartidoResponsable() {
        return this.vgc_PartidoResponsable;
    }

    @JsonProperty("impuestosPagados")
    public BigDecimal getImpuestosPagados() {
        return this.vgn_ImpuestosPagados;
    }

    @JsonProperty("impuestosExonerados")
    public BigDecimal getImpuestosExonerados() {
        return this.vgn_ImpuestosExonerados;
    }

    @JsonProperty("montoBodegaje")
    public BigDecimal getMontoBodegaje() {
        return this.vgn_MontoBodegaje;
    }
}
