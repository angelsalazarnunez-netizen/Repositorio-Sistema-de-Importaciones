package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 15/09/2026
 * Descripción: Expediente completo de una donación (HU-044): datos del trámite, datos propios de
 *   la donación, su documentación y su historial de estados. Es lo que consume la pantalla de
 *   gestión y lo que se devuelve después de cada actualización del expediente.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: la referencia FUNDEVI pasa a llamarse referencia de la Fundación UCR.
 */
public class EntExpedienteDonacion {

    private String vgc_ReferenciaInterna;
    private String vgc_TipoTramite;
    private String vgc_Estado;
    private Boolean vgb_EstadoFinal;
    private String vgc_TipoDonacion;
    private String vgc_NombreDonante;
    private String vgc_ReferenciaFundacionUcr;
    private String vgc_PaisOrigen;
    private String vgc_Moneda;
    private BigDecimal vgn_ValorEstimado;
    private String vgc_Observaciones;
    private String vgc_UsuarioResponsable;
    private LocalDateTime vgd_FechaCreacion;
    private LocalDate vgd_FechaCierre;
    private List<EntDocumentoExpediente> vgo_LsDocumentos = new ArrayList<>();
    private List<EntMovimientoEstado> vgo_LsHistorial = new ArrayList<>();

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

    @JsonProperty("estadoFinal")
    public Boolean getEstadoFinal() {
        return this.vgb_EstadoFinal;
    }

    public void setEstadoFinal(Boolean pvb_EstadoFinal) {
        this.vgb_EstadoFinal = pvb_EstadoFinal;
    }

    @JsonProperty("tipoDonacion")
    public String getTipoDonacion() {
        return this.vgc_TipoDonacion;
    }

    public void setTipoDonacion(String pvc_TipoDonacion) {
        this.vgc_TipoDonacion = pvc_TipoDonacion;
    }

    @JsonProperty("nombreDonante")
    public String getNombreDonante() {
        return this.vgc_NombreDonante;
    }

    public void setNombreDonante(String pvc_NombreDonante) {
        this.vgc_NombreDonante = pvc_NombreDonante;
    }

    @JsonProperty("referenciaFundacionUcr")
    public String getReferenciaFundacionUcr() {
        return this.vgc_ReferenciaFundacionUcr;
    }

    public void setReferenciaFundacionUcr(String pvc_ReferenciaFundacionUcr) {
        this.vgc_ReferenciaFundacionUcr = pvc_ReferenciaFundacionUcr;
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

    @JsonProperty("fechaCierre")
    public LocalDate getFechaCierre() {
        return this.vgd_FechaCierre;
    }

    public void setFechaCierre(LocalDate pvd_FechaCierre) {
        this.vgd_FechaCierre = pvd_FechaCierre;
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
}
