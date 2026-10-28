package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 15/09/2026
 * Descripción: Expediente completo de una solicitud de exoneración: sus datos, su documentación,
 *   los documentos obligatorios que siguen pendientes y su bitácora.
 *
 * Historial de cambios:
 *   04/10/2026 Equipo de Desarrollo SIM: se agrega idTramite (HU-020) para que la pantalla de
 *     observaciones consuma los servicios que identifican el expediente por su llave técnica.
 *     La referencia interna sigue siendo el identificador visible de negocio.
 */
public class EntExoneracionDetalle {

    private Integer vgn_IdTramite;
    private String vgc_ReferenciaInterna;
    private String vgc_Estado;
    private String vgc_TipoExoneracion;
    private String vgc_NumeroContratacion;
    private String vgc_OrdenCompraGeco;
    private String vgc_ContratoSicop;
    private String vgc_NumeroExonet;
    private String vgc_NumeroAutorizacionExonet;
    private String vgc_Observaciones;
    private String vgc_TipoPartidoResponsable;
    private String vgc_PartidoResponsable;
    private BigDecimal vgn_ImpuestosPagados;
    private BigDecimal vgn_ImpuestosExonerados;
    private BigDecimal vgn_MontoBodegaje;

    private String vgc_UsuarioResponsable;
    private LocalDateTime vgd_FechaCreacion;
    private List<EntDocumentoExpediente> vgo_LsDocumentos = new ArrayList<>();
    private List<String> vgo_LsDocumentosPendientes = new ArrayList<>();
    private List<EntMovimientoBitacora> vgo_LsBitacora = new ArrayList<>();

    @JsonProperty("idTramite")
    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    @JsonProperty("referenciaInterna")
    public String getReferenciaInterna() {
        return this.vgc_ReferenciaInterna;
    }

    public void setReferenciaInterna(String pvc_ReferenciaInterna) {
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
    }

    @JsonProperty("estado")
    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }

    @JsonProperty("tipoExoneracion")
    public String getTipoExoneracion() {
        return this.vgc_TipoExoneracion;
    }

    public void setTipoExoneracion(String pvc_TipoExoneracion) {
        this.vgc_TipoExoneracion = pvc_TipoExoneracion;
    }

    @JsonProperty("numeroContratacion")
    public String getNumeroContratacion() {
        return this.vgc_NumeroContratacion;
    }

    public void setNumeroContratacion(String pvc_NumeroContratacion) {
        this.vgc_NumeroContratacion = pvc_NumeroContratacion;
    }

    @JsonProperty("ordenCompraGeco")
    public String getOrdenCompraGeco() {
        return this.vgc_OrdenCompraGeco;
    }

    public void setOrdenCompraGeco(String pvc_OrdenCompraGeco) {
        this.vgc_OrdenCompraGeco = pvc_OrdenCompraGeco;
    }

    @JsonProperty("contratoSicop")
    public String getContratoSicop() {
        return this.vgc_ContratoSicop;
    }

    public void setContratoSicop(String pvc_ContratoSicop) {
        this.vgc_ContratoSicop = pvc_ContratoSicop;
    }

    @JsonProperty("observaciones")
    public String getObservaciones() {
        return this.vgc_Observaciones;
    }

    public void setObservaciones(String pvc_Observaciones) {
        this.vgc_Observaciones = pvc_Observaciones;
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

    @JsonProperty("documentosPendientes")
    public List<String> getDocumentosPendientes() {
        return this.vgo_LsDocumentosPendientes;
    }

    public void setDocumentosPendientes(List<String> pvo_LsDocumentosPendientes) {
        this.vgo_LsDocumentosPendientes = pvo_LsDocumentosPendientes;
    }

    @JsonProperty("bitacora")
    public List<EntMovimientoBitacora> getBitacora() {
        return this.vgo_LsBitacora;
    }

    public void setBitacora(List<EntMovimientoBitacora> pvo_LsBitacora) {
        this.vgo_LsBitacora = pvo_LsBitacora;
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
