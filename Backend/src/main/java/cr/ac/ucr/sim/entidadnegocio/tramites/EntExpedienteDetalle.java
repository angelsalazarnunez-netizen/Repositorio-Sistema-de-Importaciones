package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Expediente completo de un desalmacenaje especial: datos del trámite, su
 *   documentación y su historial de estados. Es lo que consume la pantalla de seguimiento.
 */
public class EntExpedienteDetalle {

    private String vgc_ReferenciaInterna;
    private String vgc_TipoTramite;
    private String vgc_Estado;
    private Boolean vgb_RequiereSicop;
    private Boolean vgb_RequiereOrdenCompra;
    private String vgc_OrigenDependencia;
    private String vgc_PaisOrigen;
    private String vgc_Moneda;
    private BigDecimal vgn_ValorEstimado;
    private String vgc_Justificacion;
    private String vgc_Descripcion;
    private String vgc_RegimenAduanero;
    private String vgc_AduanaControl;
    private String vgc_Observaciones;
    private String vgc_TipoPartidoResponsable;
    private String vgc_PartidoResponsable;
    private BigDecimal vgn_ImpuestosPagados;
    private BigDecimal vgn_ImpuestosExonerados;
    private BigDecimal vgn_MontoBodegaje;

    private String vgc_UsuarioResponsable;
    private LocalDateTime vgd_FechaCreacion;
    private List<EntDocumentoExpediente> vgo_LsDocumentos = new ArrayList<>();
    private List<EntMovimientoEstado> vgo_LsHistorial = new ArrayList<>();
    private List<EntMovimientoBitacora> vgo_LsBitacora = new ArrayList<>();
    private String vgc_EstadoDocumental;
    private String vgc_ObservacionesDocumentales;

    @JsonProperty("referenciaInterna")
    public String getReferenciaInterna() {
        return this.vgc_ReferenciaInterna;
    }

    public void setReferenciaInterna(String pvc_ReferenciaInterna) {
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
    }

    @JsonProperty("tipoTramite")
    public String getTipoTramite() {
        return this.vgc_TipoTramite;
    }

    public void setTipoTramite(String pvc_TipoTramite) {
        this.vgc_TipoTramite = pvc_TipoTramite;
    }

    @JsonProperty("estado")
    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }

    @JsonProperty("requiereSicop")
    public Boolean getRequiereSicop() {
        return this.vgb_RequiereSicop;
    }

    public void setRequiereSicop(Boolean pvb_RequiereSicop) {
        this.vgb_RequiereSicop = pvb_RequiereSicop;
    }

    @JsonProperty("requiereOrdenCompra")
    public Boolean getRequiereOrdenCompra() {
        return this.vgb_RequiereOrdenCompra;
    }

    public void setRequiereOrdenCompra(Boolean pvb_RequiereOrdenCompra) {
        this.vgb_RequiereOrdenCompra = pvb_RequiereOrdenCompra;
    }

    @JsonProperty("origenDependencia")
    public String getOrigenDependencia() {
        return this.vgc_OrigenDependencia;
    }

    public void setOrigenDependencia(String pvc_OrigenDependencia) {
        this.vgc_OrigenDependencia = pvc_OrigenDependencia;
    }

    @JsonProperty("paisOrigen")
    public String getPaisOrigen() {
        return this.vgc_PaisOrigen;
    }

    public void setPaisOrigen(String pvc_PaisOrigen) {
        this.vgc_PaisOrigen = pvc_PaisOrigen;
    }

    @JsonProperty("moneda")
    public String getMoneda() {
        return this.vgc_Moneda;
    }

    public void setMoneda(String pvc_Moneda) {
        this.vgc_Moneda = pvc_Moneda;
    }

    @JsonProperty("valorEstimado")
    public BigDecimal getValorEstimado() {
        return this.vgn_ValorEstimado;
    }

    public void setValorEstimado(BigDecimal pvn_ValorEstimado) {
        this.vgn_ValorEstimado = pvn_ValorEstimado;
    }

    @JsonProperty("justificacion")
    public String getJustificacion() {
        return this.vgc_Justificacion;
    }

    public void setJustificacion(String pvc_Justificacion) {
        this.vgc_Justificacion = pvc_Justificacion;
    }

    @JsonProperty("usuarioResponsable")
    public String getUsuarioResponsable() {
        return this.vgc_UsuarioResponsable;
    }

    public void setUsuarioResponsable(String pvc_UsuarioResponsable) {
        this.vgc_UsuarioResponsable = pvc_UsuarioResponsable;
    }

    @JsonProperty("fechaCreacion")
    public LocalDateTime getFechaCreacion() {
        return this.vgd_FechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime pvd_FechaCreacion) {
        this.vgd_FechaCreacion = pvd_FechaCreacion;
    }

    @JsonProperty("documentos")
    public List<EntDocumentoExpediente> getDocumentos() {
        return this.vgo_LsDocumentos;
    }

    public void setDocumentos(List<EntDocumentoExpediente> pvo_LsDocumentos) {
        this.vgo_LsDocumentos = pvo_LsDocumentos;
    }

    @JsonProperty("historial")
    public List<EntMovimientoEstado> getHistorial() {
        return this.vgo_LsHistorial;
    }

    public void setHistorial(List<EntMovimientoEstado> pvo_LsHistorial) {
        this.vgo_LsHistorial = pvo_LsHistorial;
    }

    @JsonProperty("bitacora")
    public List<EntMovimientoBitacora> getBitacora() {
        return this.vgo_LsBitacora;
    }

    public void setBitacora(List<EntMovimientoBitacora> pvo_LsBitacora) {
        this.vgo_LsBitacora = pvo_LsBitacora;
    }

    @JsonProperty("estadoDocumental")
    public String getEstadoDocumental() {
        return this.vgc_EstadoDocumental;
    }

    public void setEstadoDocumental(String pvc_EstadoDocumental) {
        this.vgc_EstadoDocumental = pvc_EstadoDocumental;
    }

    @JsonProperty("observacionesDocumentales")
    public String getObservacionesDocumentales() {
        return this.vgc_ObservacionesDocumentales;
    }

    public void setObservacionesDocumentales(String pvc_ObservacionesDocumentales) {
        this.vgc_ObservacionesDocumentales = pvc_ObservacionesDocumentales;
    }

    @JsonProperty("descripcion")
    public String getDescripcion() {
        return this.vgc_Descripcion;
    }

    public void setDescripcion(String pvc_Descripcion) {
        this.vgc_Descripcion = pvc_Descripcion;
    }

    @JsonProperty("regimenAduanero")
    public String getRegimenAduanero() {
        return this.vgc_RegimenAduanero;
    }

    public void setRegimenAduanero(String pvc_RegimenAduanero) {
        this.vgc_RegimenAduanero = pvc_RegimenAduanero;
    }

    @JsonProperty("aduanaControl")
    public String getAduanaControl() {
        return this.vgc_AduanaControl;
    }

    public void setAduanaControl(String pvc_AduanaControl) {
        this.vgc_AduanaControl = pvc_AduanaControl;
    }

    @JsonProperty("observaciones")
    public String getObservaciones() {
        return this.vgc_Observaciones;
    }

    public void setObservaciones(String pvc_Observaciones) {
        this.vgc_Observaciones = pvc_Observaciones;
    }

    @JsonProperty("tipoPartidoResponsable")
    public String getTipoPartidoResponsable() {
        return this.vgc_TipoPartidoResponsable;
    }

    public void setTipoPartidoResponsable(String pvc_TipoPartidoResponsable) {
        this.vgc_TipoPartidoResponsable = pvc_TipoPartidoResponsable;
    }

    @JsonProperty("partidoResponsable")
    public String getPartidoResponsable() {
        return this.vgc_PartidoResponsable;
    }

    public void setPartidoResponsable(String pvc_PartidoResponsable) {
        this.vgc_PartidoResponsable = pvc_PartidoResponsable;
    }

    @JsonProperty("impuestosPagados")
    public BigDecimal getImpuestosPagados() {
        return this.vgn_ImpuestosPagados;
    }

    public void setImpuestosPagados(BigDecimal pvn_ImpuestosPagados) {
        this.vgn_ImpuestosPagados = pvn_ImpuestosPagados;
    }

    @JsonProperty("impuestosExonerados")
    public BigDecimal getImpuestosExonerados() {
        return this.vgn_ImpuestosExonerados;
    }

    public void setImpuestosExonerados(BigDecimal pvn_ImpuestosExonerados) {
        this.vgn_ImpuestosExonerados = pvn_ImpuestosExonerados;
    }

    @JsonProperty("montoBodegaje")
    public BigDecimal getMontoBodegaje() {
        return this.vgn_MontoBodegaje;
    }

    public void setMontoBodegaje(BigDecimal pvn_MontoBodegaje) {
        this.vgn_MontoBodegaje = pvn_MontoBodegaje;
    }
}
