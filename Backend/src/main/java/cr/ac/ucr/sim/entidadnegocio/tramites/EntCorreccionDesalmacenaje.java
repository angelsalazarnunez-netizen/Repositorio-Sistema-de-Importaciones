package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.math.BigDecimal;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 05/10/2026
 * Descripción: Corrección de los datos ya guardados de un desalmacenaje especial (HU-032). No
 *   representa una tabla: transporta los datos que la persona analista corrige en el expediente
 *   cuando la dependencia aclara la información. Los datos que no se indican se conservan como
 *   estaban, y cada cambio queda anotado en la bitácora con su valor anterior.
 */
public class EntCorreccionDesalmacenaje {

    private String vgc_OrigenDependencia;
    private String vgc_Justificacion;
    private String vgc_PaisOrigen;
    private String vgc_Moneda;
    private BigDecimal vgn_ValorEstimado;
    private String vgc_Descripcion;
    private String vgc_RegimenAduanero;
    private String vgc_AduanaControl;
    private String vgc_Observaciones;
    private Integer vgn_IdUsuarioResponsable;

    public String getOrigenDependencia() {
        return this.vgc_OrigenDependencia;
    }

    public void setOrigenDependencia(String pvc_OrigenDependencia) {
        this.vgc_OrigenDependencia = pvc_OrigenDependencia;
    }

    public String getJustificacion() {
        return this.vgc_Justificacion;
    }

    public void setJustificacion(String pvc_Justificacion) {
        this.vgc_Justificacion = pvc_Justificacion;
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

    public String getDescripcion() {
        return this.vgc_Descripcion;
    }

    public void setDescripcion(String pvc_Descripcion) {
        this.vgc_Descripcion = pvc_Descripcion;
    }

    public String getRegimenAduanero() {
        return this.vgc_RegimenAduanero;
    }

    public void setRegimenAduanero(String pvc_RegimenAduanero) {
        this.vgc_RegimenAduanero = pvc_RegimenAduanero;
    }

    public String getAduanaControl() {
        return this.vgc_AduanaControl;
    }

    public void setAduanaControl(String pvc_AduanaControl) {
        this.vgc_AduanaControl = pvc_AduanaControl;
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
