package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 25/09/2026
 * Descripcion: Linea tabulada del reporte de consultas internas (HU-036). Cada linea resume un
 *   grupo, por ejemplo un tipo de tramite o un regimen aduanero, y una moneda: los montos de
 *   monedas distintas no se suman entre si.
 */
public class EntLineaReporte {

    private String vgc_Grupo;
    private List<String> vgo_LsValores = new ArrayList<>();
    private String vgc_Moneda;
    private long vgn_CantidadTramites;
    private BigDecimal vgn_TotalCif;
    private BigDecimal vgn_TotalImpuestosPagados;
    private BigDecimal vgn_TotalImpuestosExonerados;
    private BigDecimal vgn_TotalBodegaje;
    private long vgn_CantidadPermisos;

    @JsonProperty("grupo")
    public String getGrupo() {
        return this.vgc_Grupo;
    }

    public void setGrupo(String pvc_Grupo) {
        this.vgc_Grupo = pvc_Grupo;
    }

    @JsonProperty("moneda")
    public String getMoneda() {
        return this.vgc_Moneda;
    }

    public void setMoneda(String pvc_Moneda) {
        this.vgc_Moneda = pvc_Moneda;
    }

    @JsonProperty("cantidadTramites")
    public long getCantidadTramites() {
        return this.vgn_CantidadTramites;
    }

    public void setCantidadTramites(long pvn_CantidadTramites) {
        this.vgn_CantidadTramites = pvn_CantidadTramites;
    }

    @JsonProperty("totalCif")
    public BigDecimal getTotalCif() {
        return this.vgn_TotalCif;
    }

    public void setTotalCif(BigDecimal pvn_TotalCif) {
        this.vgn_TotalCif = pvn_TotalCif;
    }

    @JsonProperty("cantidadPermisos")
    public long getCantidadPermisos() {
        return this.vgn_CantidadPermisos;
    }

    public void setCantidadPermisos(long pvn_CantidadPermisos) {
        this.vgn_CantidadPermisos = pvn_CantidadPermisos;
    }

    @JsonProperty("totalImpuestosPagados")
    public BigDecimal getTotalImpuestosPagados() {
        return this.vgn_TotalImpuestosPagados;
    }

    public void setTotalImpuestosPagados(BigDecimal pvn_TotalImpuestosPagados) {
        this.vgn_TotalImpuestosPagados = pvn_TotalImpuestosPagados;
    }

    @JsonProperty("totalImpuestosExonerados")
    public BigDecimal getTotalImpuestosExonerados() {
        return this.vgn_TotalImpuestosExonerados;
    }

    public void setTotalImpuestosExonerados(BigDecimal pvn_TotalImpuestosExonerados) {
        this.vgn_TotalImpuestosExonerados = pvn_TotalImpuestosExonerados;
    }

    @JsonProperty("totalBodegaje")
    public BigDecimal getTotalBodegaje() {
        return this.vgn_TotalBodegaje;
    }

    public void setTotalBodegaje(BigDecimal pvn_TotalBodegaje) {
        this.vgn_TotalBodegaje = pvn_TotalBodegaje;
    }

    /**
     * Valor de la línea en cada columna de agrupación, en el orden de los encabezados.
     */
    @JsonProperty("valores")
    public List<String> getValores() {
        return this.vgo_LsValores;
    }

    public void setValores(List<String> pvo_LsValores) {
        this.vgo_LsValores = pvo_LsValores;
    }
}
