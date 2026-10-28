package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Entidad de negocio de la tabla SIMF_DOCUMENTO. Representa un archivo de
 *   respaldo asociado a un expediente de trámite.
 */
@Entity
@Table(name = "SIMF_DOCUMENTO")
public class EntDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DOCUMENTO")
    private Integer vgn_IdDocumento;

    @Column(name = "ID_TRAMITE", nullable = false)
    private Integer vgn_IdTramite;

    @Column(name = "ID_TIPO_DOCUMENTO", nullable = false)
    private Integer vgn_IdTipoDocumento;

    @Column(name = "ID_USUARIO_CARGA", nullable = false)
    private Integer vgn_IdUsuarioCarga;

    @Column(name = "ID_DOCUMENTO_ANTERIOR")
    private Integer vgn_IdDocumentoAnterior;

    @Column(name = "NOMBRE_ARCHIVO", nullable = false, length = 120)
    private String vgc_NombreArchivo;

    @Column(name = "RUTA_ARCHIVO", nullable = false, length = 255)
    private String vgc_RutaArchivo;

    @Column(name = "VERSION", nullable = false)
    private Short vgn_Version;

    @Column(name = "ESTADO_DOCUMENTO", nullable = false, length = 3)
    private String vgc_EstadoDocumento;

    @Column(name = "FECHA_CARGA", nullable = false)
    private LocalDateTime vgd_FechaCarga;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Asigna la fecha de carga del documento cuando no viene indicada.
     */
    @PrePersist
    public void AsignarFechaCarga() {
        if (this.vgd_FechaCarga == null) {
            this.vgd_FechaCarga = LocalDateTime.now();
        }
    }

    @JsonProperty("idDocumento")
    public Integer getIdDocumento() {
        return this.vgn_IdDocumento;
    }

    public void setIdDocumento(Integer pvn_IdDocumento) {
        this.vgn_IdDocumento = pvn_IdDocumento;
    }

    @JsonProperty("idTramite")
    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    @JsonProperty("idTipoDocumento")
    public Integer getIdTipoDocumento() {
        return this.vgn_IdTipoDocumento;
    }

    public void setIdTipoDocumento(Integer pvn_IdTipoDocumento) {
        this.vgn_IdTipoDocumento = pvn_IdTipoDocumento;
    }

    @JsonProperty("idUsuarioCarga")
    public Integer getIdUsuarioCarga() {
        return this.vgn_IdUsuarioCarga;
    }

    public void setIdUsuarioCarga(Integer pvn_IdUsuarioCarga) {
        this.vgn_IdUsuarioCarga = pvn_IdUsuarioCarga;
    }

    @JsonProperty("idDocumentoAnterior")
    public Integer getIdDocumentoAnterior() {
        return this.vgn_IdDocumentoAnterior;
    }

    public void setIdDocumentoAnterior(Integer pvn_IdDocumentoAnterior) {
        this.vgn_IdDocumentoAnterior = pvn_IdDocumentoAnterior;
    }

    @JsonProperty("nombreArchivo")
    public String getNombreArchivo() {
        return this.vgc_NombreArchivo;
    }

    public void setNombreArchivo(String pvc_NombreArchivo) {
        this.vgc_NombreArchivo = pvc_NombreArchivo;
    }

    @JsonProperty("rutaArchivo")
    public String getRutaArchivo() {
        return this.vgc_RutaArchivo;
    }

    public void setRutaArchivo(String pvc_RutaArchivo) {
        this.vgc_RutaArchivo = pvc_RutaArchivo;
    }

    @JsonProperty("version")
    public Short getVersion() {
        return this.vgn_Version;
    }

    public void setVersion(Short pvn_Version) {
        this.vgn_Version = pvn_Version;
    }

    @JsonProperty("estadoDocumento")
    public String getEstadoDocumento() {
        return this.vgc_EstadoDocumento;
    }

    public void setEstadoDocumento(String pvc_EstadoDocumento) {
        this.vgc_EstadoDocumento = pvc_EstadoDocumento;
    }

    @JsonProperty("fechaCarga")
    public LocalDateTime getFechaCarga() {
        return this.vgd_FechaCarga;
    }

    public void setFechaCarga(LocalDateTime pvd_FechaCarga) {
        this.vgd_FechaCarga = pvd_FechaCarga;
    }
}
