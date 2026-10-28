package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 03/10/2026
 * Descripcion: representa un elemento del checklist
 * documental de una solicitud de exoneracion.
 */
public class ModeloRevisionDocumentoRespuesta {

    private Integer vgn_IdTipoDocumento;
    private String vgc_CodigoTipoDocumento;
    private String vgc_NombreTipoDocumento;
    private Boolean vgb_Obligatorio;

    private Integer vgn_IdDocumento;
    private String vgc_NombreArchivo;
    private Short vgn_Version;

    private Integer vgn_IdRevisionDocumento;
    private String vgc_EstadoRevision;
    private String vgc_Observacion;
    private LocalDateTime vgd_FechaRevision;

    public ModeloRevisionDocumentoRespuesta() {
    }

    public Integer getIdTipoDocumento() {
        return vgn_IdTipoDocumento;
    }

    public void setIdTipoDocumento(Integer vln_IdTipoDocumento) {
        this.vgn_IdTipoDocumento = vln_IdTipoDocumento;
    }

    public String getCodigoTipoDocumento() {
        return vgc_CodigoTipoDocumento;
    }

    public void setCodigoTipoDocumento(String vlc_CodigoTipoDocumento) {
        this.vgc_CodigoTipoDocumento = vlc_CodigoTipoDocumento;
    }

    public String getNombreTipoDocumento() {
        return vgc_NombreTipoDocumento;
    }

    public void setNombreTipoDocumento(String vlc_NombreTipoDocumento) {
        this.vgc_NombreTipoDocumento = vlc_NombreTipoDocumento;
    }

    public Boolean getObligatorio() {
        return vgb_Obligatorio;
    }

    public void setObligatorio(Boolean vlb_Obligatorio) {
        this.vgb_Obligatorio = vlb_Obligatorio;
    }

    public Integer getIdDocumento() {
        return vgn_IdDocumento;
    }

    public void setIdDocumento(Integer vln_IdDocumento) {
        this.vgn_IdDocumento = vln_IdDocumento;
    }

    public String getNombreArchivo() {
        return vgc_NombreArchivo;
    }

    public void setNombreArchivo(String vlc_NombreArchivo) {
        this.vgc_NombreArchivo = vlc_NombreArchivo;
    }

    public Short getVersion() {
        return vgn_Version;
    }

    public void setVersion(Short vln_Version) {
        this.vgn_Version = vln_Version;
    }

    public Integer getIdRevisionDocumento() {
        return vgn_IdRevisionDocumento;
    }

    public void setIdRevisionDocumento(Integer vln_IdRevisionDocumento) {
        this.vgn_IdRevisionDocumento = vln_IdRevisionDocumento;
    }

    public String getEstadoRevision() {
        return vgc_EstadoRevision;
    }

    public void setEstadoRevision(String vlc_EstadoRevision) {
        this.vgc_EstadoRevision = vlc_EstadoRevision;
    }

    public String getObservacion() {
        return vgc_Observacion;
    }

    public void setObservacion(String vlc_Observacion) {
        this.vgc_Observacion = vlc_Observacion;
    }

    public LocalDateTime getFechaRevision() {
        return vgd_FechaRevision;
    }

    public void setFechaRevision(LocalDateTime vld_FechaRevision) {
        this.vgd_FechaRevision = vld_FechaRevision;
    }
}