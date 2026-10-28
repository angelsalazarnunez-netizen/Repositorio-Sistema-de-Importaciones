package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Requisito documental del control de documentos obligatorios (HU-021): el
 *   documento exigido, el archivo adjunto que lo atiende y si cumple o no el requisito.
 *   El estado de control resume el archivo y su revisión (HU-019):
 *   CUMPLIDO = adjunto y recibido, FALTANTE = sin adjuntar,
 *   POR_REVISAR = adjunto sin revisión de recibido, RECHAZADO = adjunto y rechazado.
 */
public class ModeloRequisitoControlRespuesta {

    private Integer vgn_IdTipoDocumento;
    private String vgc_CodigoTipoDocumento;
    private String vgc_NombreTipoDocumento;
    private Boolean vgb_Obligatorio;
    private Integer vgn_IdDocumento;
    private String vgc_NombreArchivo;
    private LocalDateTime vgd_FechaCarga;
    private String vgc_EstadoRevision;
    private String vgc_EstadoControl;

    public Integer getIdTipoDocumento() {
        return this.vgn_IdTipoDocumento;
    }

    public void setIdTipoDocumento(Integer pvn_IdTipoDocumento) {
        this.vgn_IdTipoDocumento = pvn_IdTipoDocumento;
    }

    public String getCodigoTipoDocumento() {
        return this.vgc_CodigoTipoDocumento;
    }

    public void setCodigoTipoDocumento(String pvc_CodigoTipoDocumento) {
        this.vgc_CodigoTipoDocumento = pvc_CodigoTipoDocumento;
    }

    public String getNombreTipoDocumento() {
        return this.vgc_NombreTipoDocumento;
    }

    public void setNombreTipoDocumento(String pvc_NombreTipoDocumento) {
        this.vgc_NombreTipoDocumento = pvc_NombreTipoDocumento;
    }

    public Boolean getObligatorio() {
        return this.vgb_Obligatorio;
    }

    public void setObligatorio(Boolean pvb_Obligatorio) {
        this.vgb_Obligatorio = pvb_Obligatorio;
    }

    public Integer getIdDocumento() {
        return this.vgn_IdDocumento;
    }

    public void setIdDocumento(Integer pvn_IdDocumento) {
        this.vgn_IdDocumento = pvn_IdDocumento;
    }

    public String getNombreArchivo() {
        return this.vgc_NombreArchivo;
    }

    public void setNombreArchivo(String pvc_NombreArchivo) {
        this.vgc_NombreArchivo = pvc_NombreArchivo;
    }

    public LocalDateTime getFechaCarga() {
        return this.vgd_FechaCarga;
    }

    public void setFechaCarga(LocalDateTime pvd_FechaCarga) {
        this.vgd_FechaCarga = pvd_FechaCarga;
    }

    public String getEstadoRevision() {
        return this.vgc_EstadoRevision;
    }

    public void setEstadoRevision(String pvc_EstadoRevision) {
        this.vgc_EstadoRevision = pvc_EstadoRevision;
    }

    public String getEstadoControl() {
        return this.vgc_EstadoControl;
    }

    public void setEstadoControl(String pvc_EstadoControl) {
        this.vgc_EstadoControl = pvc_EstadoControl;
    }
}
