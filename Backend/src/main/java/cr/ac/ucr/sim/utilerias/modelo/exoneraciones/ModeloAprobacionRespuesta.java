package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Resumen de una solicitud de exoneración para que la jefatura registre su decisión (HU-022):
 *   datos del expediente, resultado de la revisión documental, observaciones, si el usuario está
 *   autorizado y si el expediente admite una decisión, y el historial de decisiones.
 */
public class ModeloAprobacionRespuesta {

    private Integer vgn_IdTramite;
    private String vgc_ReferenciaInterna;
    private String vgc_TipoExoneracion;
    private String vgc_NumeroContratacion;
    private String vgc_ContratoSicop;
    private String vgc_OrdenCompraGeco;
    private String vgc_EstadoExpediente;
    private String vgc_UsuarioResponsable;
    private String vgc_EnviadoPor;
    private LocalDateTime vgd_FechaEnvio;
    private Integer vgn_DocumentosRecibidos;
    private Integer vgn_TotalObligatorios;
    private Boolean vgb_CumpleDocumentacion;
    private List<String> vgo_LsDocumentosPendientes = new ArrayList<>();
    private Integer vgn_ObservacionesRegistradas;
    private LocalDateTime vgd_UltimaObservacion;
    private Boolean vgb_UsuarioAutorizado;
    private Boolean vgb_PuedeDecidir;
    private String vgc_MotivoNoDecide;
    private List<ModeloDecisionRespuesta> vgo_LsDecisiones = new ArrayList<>();
    private String vgc_Mensaje;

    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    public String getReferenciaInterna() {
        return this.vgc_ReferenciaInterna;
    }

    public void setReferenciaInterna(String pvc_ReferenciaInterna) {
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
    }

    public String getTipoExoneracion() {
        return this.vgc_TipoExoneracion;
    }

    public void setTipoExoneracion(String pvc_TipoExoneracion) {
        this.vgc_TipoExoneracion = pvc_TipoExoneracion;
    }

    public String getNumeroContratacion() {
        return this.vgc_NumeroContratacion;
    }

    public void setNumeroContratacion(String pvc_NumeroContratacion) {
        this.vgc_NumeroContratacion = pvc_NumeroContratacion;
    }

    public String getContratoSicop() {
        return this.vgc_ContratoSicop;
    }

    public void setContratoSicop(String pvc_ContratoSicop) {
        this.vgc_ContratoSicop = pvc_ContratoSicop;
    }

    public String getOrdenCompraGeco() {
        return this.vgc_OrdenCompraGeco;
    }

    public void setOrdenCompraGeco(String pvc_OrdenCompraGeco) {
        this.vgc_OrdenCompraGeco = pvc_OrdenCompraGeco;
    }

    public String getEstadoExpediente() {
        return this.vgc_EstadoExpediente;
    }

    public void setEstadoExpediente(String pvc_EstadoExpediente) {
        this.vgc_EstadoExpediente = pvc_EstadoExpediente;
    }

    public String getUsuarioResponsable() {
        return this.vgc_UsuarioResponsable;
    }

    public void setUsuarioResponsable(String pvc_UsuarioResponsable) {
        this.vgc_UsuarioResponsable = pvc_UsuarioResponsable;
    }

    public String getEnviadoPor() {
        return this.vgc_EnviadoPor;
    }

    public void setEnviadoPor(String pvc_EnviadoPor) {
        this.vgc_EnviadoPor = pvc_EnviadoPor;
    }

    public LocalDateTime getFechaEnvio() {
        return this.vgd_FechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime pvd_FechaEnvio) {
        this.vgd_FechaEnvio = pvd_FechaEnvio;
    }

    public Integer getDocumentosRecibidos() {
        return this.vgn_DocumentosRecibidos;
    }

    public void setDocumentosRecibidos(Integer pvn_DocumentosRecibidos) {
        this.vgn_DocumentosRecibidos = pvn_DocumentosRecibidos;
    }

    public Integer getTotalObligatorios() {
        return this.vgn_TotalObligatorios;
    }

    public void setTotalObligatorios(Integer pvn_TotalObligatorios) {
        this.vgn_TotalObligatorios = pvn_TotalObligatorios;
    }

    public Boolean getCumpleDocumentacion() {
        return this.vgb_CumpleDocumentacion;
    }

    public void setCumpleDocumentacion(Boolean pvb_CumpleDocumentacion) {
        this.vgb_CumpleDocumentacion = pvb_CumpleDocumentacion;
    }

    public List<String> getDocumentosPendientes() {
        return this.vgo_LsDocumentosPendientes;
    }

    public void setDocumentosPendientes(List<String> pvo_LsDocumentosPendientes) {
        this.vgo_LsDocumentosPendientes = pvo_LsDocumentosPendientes;
    }

    public Integer getObservacionesRegistradas() {
        return this.vgn_ObservacionesRegistradas;
    }

    public void setObservacionesRegistradas(Integer pvn_ObservacionesRegistradas) {
        this.vgn_ObservacionesRegistradas = pvn_ObservacionesRegistradas;
    }

    public LocalDateTime getUltimaObservacion() {
        return this.vgd_UltimaObservacion;
    }

    public void setUltimaObservacion(LocalDateTime pvd_UltimaObservacion) {
        this.vgd_UltimaObservacion = pvd_UltimaObservacion;
    }

    public Boolean getUsuarioAutorizado() {
        return this.vgb_UsuarioAutorizado;
    }

    public void setUsuarioAutorizado(Boolean pvb_UsuarioAutorizado) {
        this.vgb_UsuarioAutorizado = pvb_UsuarioAutorizado;
    }

    public Boolean getPuedeDecidir() {
        return this.vgb_PuedeDecidir;
    }

    public void setPuedeDecidir(Boolean pvb_PuedeDecidir) {
        this.vgb_PuedeDecidir = pvb_PuedeDecidir;
    }

    public String getMotivoNoDecide() {
        return this.vgc_MotivoNoDecide;
    }

    public void setMotivoNoDecide(String pvc_MotivoNoDecide) {
        this.vgc_MotivoNoDecide = pvc_MotivoNoDecide;
    }

    public List<ModeloDecisionRespuesta> getDecisiones() {
        return this.vgo_LsDecisiones;
    }

    public void setDecisiones(List<ModeloDecisionRespuesta> pvo_LsDecisiones) {
        this.vgo_LsDecisiones = pvo_LsDecisiones;
    }

    public String getMensaje() {
        return this.vgc_Mensaje;
    }

    public void setMensaje(String pvc_Mensaje) {
        this.vgc_Mensaje = pvc_Mensaje;
    }
}
