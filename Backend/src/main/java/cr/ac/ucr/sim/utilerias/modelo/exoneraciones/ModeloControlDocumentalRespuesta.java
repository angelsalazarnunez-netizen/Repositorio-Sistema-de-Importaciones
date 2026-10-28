package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Resultado del control de documentos obligatorios de una solicitud de exoneración
 *   (HU-021): el estado documental del expediente, la lista de documentos pendientes y si la
 *   documentación permite continuar a aprobación.
 */
public class ModeloControlDocumentalRespuesta {

    private Integer vgn_IdTramite;
    private String vgc_ReferenciaInterna;
    private String vgc_TipoExoneracion;
    private String vgc_EstadoExpediente;
    private String vgc_EstadoDocumental;
    private Boolean vgb_CumpleDocumentacion;
    private Boolean vgb_PuedeContinuarAprobacion;
    private Integer vgn_TotalObligatorios;
    private Integer vgn_ObligatoriosCumplidos;
    private Integer vgn_ObligatoriosPendientes;
    private Integer vgn_PorcentajeCumplimiento;
    private List<ModeloRequisitoControlRespuesta> vgo_LsRequisitos = new ArrayList<>();
    private List<String> vgo_LsDocumentosPendientes = new ArrayList<>();

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

    public String getEstadoExpediente() {
        return this.vgc_EstadoExpediente;
    }

    public void setEstadoExpediente(String pvc_EstadoExpediente) {
        this.vgc_EstadoExpediente = pvc_EstadoExpediente;
    }

    public String getEstadoDocumental() {
        return this.vgc_EstadoDocumental;
    }

    public void setEstadoDocumental(String pvc_EstadoDocumental) {
        this.vgc_EstadoDocumental = pvc_EstadoDocumental;
    }

    public Boolean getCumpleDocumentacion() {
        return this.vgb_CumpleDocumentacion;
    }

    public void setCumpleDocumentacion(Boolean pvb_CumpleDocumentacion) {
        this.vgb_CumpleDocumentacion = pvb_CumpleDocumentacion;
    }

    public Boolean getPuedeContinuarAprobacion() {
        return this.vgb_PuedeContinuarAprobacion;
    }

    public void setPuedeContinuarAprobacion(Boolean pvb_PuedeContinuarAprobacion) {
        this.vgb_PuedeContinuarAprobacion = pvb_PuedeContinuarAprobacion;
    }

    public Integer getTotalObligatorios() {
        return this.vgn_TotalObligatorios;
    }

    public void setTotalObligatorios(Integer pvn_TotalObligatorios) {
        this.vgn_TotalObligatorios = pvn_TotalObligatorios;
    }

    public Integer getObligatoriosCumplidos() {
        return this.vgn_ObligatoriosCumplidos;
    }

    public void setObligatoriosCumplidos(Integer pvn_ObligatoriosCumplidos) {
        this.vgn_ObligatoriosCumplidos = pvn_ObligatoriosCumplidos;
    }

    public Integer getObligatoriosPendientes() {
        return this.vgn_ObligatoriosPendientes;
    }

    public void setObligatoriosPendientes(Integer pvn_ObligatoriosPendientes) {
        this.vgn_ObligatoriosPendientes = pvn_ObligatoriosPendientes;
    }

    public Integer getPorcentajeCumplimiento() {
        return this.vgn_PorcentajeCumplimiento;
    }

    public void setPorcentajeCumplimiento(Integer pvn_PorcentajeCumplimiento) {
        this.vgn_PorcentajeCumplimiento = pvn_PorcentajeCumplimiento;
    }

    public List<ModeloRequisitoControlRespuesta> getRequisitos() {
        return this.vgo_LsRequisitos;
    }

    public void setRequisitos(List<ModeloRequisitoControlRespuesta> pvo_LsRequisitos) {
        this.vgo_LsRequisitos = pvo_LsRequisitos;
    }

    public List<String> getDocumentosPendientes() {
        return this.vgo_LsDocumentosPendientes;
    }

    public void setDocumentosPendientes(List<String> pvo_LsDocumentosPendientes) {
        this.vgo_LsDocumentosPendientes = pvo_LsDocumentosPendientes;
    }
}
