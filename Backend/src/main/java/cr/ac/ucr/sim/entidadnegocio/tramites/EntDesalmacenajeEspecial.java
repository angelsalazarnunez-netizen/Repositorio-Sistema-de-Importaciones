package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Entidad de negocio de la tabla SIMT_DESALMACENAJE_ESPEC. Agrega los datos
 *   propios del desalmacenaje especial y comparte la llave primaria con SIMT_TRAMITE.
 */
@Entity
@Table(name = "SIMT_DESALMACENAJE_ESPEC")
public class EntDesalmacenajeEspecial {

    @Id
    @Column(name = "ID_TRAMITE")
    private Integer vgn_IdTramite;

    @Column(name = "ORIGEN_DEPENDENCIA", nullable = false, length = 120)
    private String vgc_OrigenDependencia;

    @Column(name = "JUSTIFICACION", nullable = false, length = 1000)
    private String vgc_Justificacion;

    @Column(name = "ESTADO_INVESTIGACION", length = 50)
    private String vgc_EstadoInvestigacion;

    @Column(name = "ESTADO_DOCUMENTAL", length = 10)
    private String vgc_EstadoDocumental;

    @Column(name = "OBSERVACIONES_DOCUMENTALES", length = 1000)
    private String vgc_ObservacionesDocumentales;

    @Column(name = "ID_TRAMITE_RECLASIFICADO")
    private Integer vgn_IdTramiteReclasificado;

    @JsonProperty("idTramite")
    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    @JsonProperty("origenDependencia")
    public String getOrigenDependencia() {
        return this.vgc_OrigenDependencia;
    }

    public void setOrigenDependencia(String pvc_OrigenDependencia) {
        this.vgc_OrigenDependencia = pvc_OrigenDependencia;
    }

    @JsonProperty("justificacion")
    public String getJustificacion() {
        return this.vgc_Justificacion;
    }

    public void setJustificacion(String pvc_Justificacion) {
        this.vgc_Justificacion = pvc_Justificacion;
    }

    @JsonProperty("estadoInvestigacion")
    public String getEstadoInvestigacion() {
        return this.vgc_EstadoInvestigacion;
    }

    public void setEstadoInvestigacion(String pvc_EstadoInvestigacion) {
        this.vgc_EstadoInvestigacion = pvc_EstadoInvestigacion;
    }

    @JsonProperty("idTramiteReclasificado")
    public Integer getIdTramiteReclasificado() {
        return this.vgn_IdTramiteReclasificado;
    }

    public void setIdTramiteReclasificado(Integer pvn_IdTramiteReclasificado) {
        this.vgn_IdTramiteReclasificado = pvn_IdTramiteReclasificado;
    }

    public String getEstadoDocumental() {
        return this.vgc_EstadoDocumental;
    }

    public void setEstadoDocumental(String pvc_EstadoDocumental) {
        this.vgc_EstadoDocumental = pvc_EstadoDocumental;
    }

    public String getObservacionesDocumentales() {
        return this.vgc_ObservacionesDocumentales;
    }

    public void setObservacionesDocumentales(String pvc_ObservacionesDocumentales) {
        this.vgc_ObservacionesDocumentales = pvc_ObservacionesDocumentales;
    }
}
