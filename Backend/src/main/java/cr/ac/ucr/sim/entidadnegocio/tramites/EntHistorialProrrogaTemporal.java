package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Historial de una prórroga autorizada de un trámite temporal (HU-028).
 */
@Entity
@Table(name = "SIMT_HISTORIAL_PRORROGA_TEMPORAL")
public class EntHistorialProrrogaTemporal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HISTORIAL_PRORROGA")
    private Integer vgn_IdHistorialProrroga;

    @Column(name = "ID_TRAMITE", nullable = false)
    private Integer vgn_IdTramite;

    @Column(name = "FECHA_VENCIMIENTO_ANTERIOR", nullable = false)
    private LocalDate vgd_FechaVencimientoAnterior;

    @Column(name = "FECHA_VENCIMIENTO_NUEVA", nullable = false)
    private LocalDate vgd_FechaVencimientoNueva;

    @Column(name = "MOTIVO", nullable = false, length = 500)
    private String vgc_Motivo;

    @Column(name = "OFICIO_SOLICITUD", nullable = false, length = 120)
    private String vgc_OficioSolicitud;

    @Column(name = "NUMERO_GESTION", nullable = false, length = 80)
    private String vgc_NumeroGestion;

    @Column(name = "NUMERO_AUTORIZACION", nullable = false, length = 120)
    private String vgc_NumeroAutorizacion;

    @Column(name = "NUMERO_CONTRATO", length = 120)
    private String vgc_NumeroContrato;

    @Column(name = "ID_DOCUMENTO_OFICIO", nullable = false)
    private Integer vgn_IdDocumentoOficio;

    @Column(name = "ID_DOCUMENTO_RESOLUCION", nullable = false)
    private Integer vgn_IdDocumentoResolucion;

    @Column(name = "ID_USUARIO_REGISTRO", nullable = false)
    private Integer vgn_IdUsuarioRegistro;

    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDateTime vgd_FechaRegistro;

    @PrePersist
    public void AsignarFechaRegistro() {
        if (this.vgd_FechaRegistro == null) {
            this.vgd_FechaRegistro = LocalDateTime.now();
        }
    }

    @JsonProperty("idHistorialProrroga")
    public Integer getIdHistorialProrroga() {
        return this.vgn_IdHistorialProrroga;
    }

    public void setIdHistorialProrroga(Integer pvn_IdHistorialProrroga) {
        this.vgn_IdHistorialProrroga = pvn_IdHistorialProrroga;
    }

    @JsonProperty("idTramite")
    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    @JsonProperty("fechaVencimientoAnterior")
    public LocalDate getFechaVencimientoAnterior() {
        return this.vgd_FechaVencimientoAnterior;
    }

    public void setFechaVencimientoAnterior(LocalDate pvd_Fecha) {
        this.vgd_FechaVencimientoAnterior = pvd_Fecha;
    }

    @JsonProperty("fechaVencimientoNueva")
    public LocalDate getFechaVencimientoNueva() {
        return this.vgd_FechaVencimientoNueva;
    }

    public void setFechaVencimientoNueva(LocalDate pvd_Fecha) {
        this.vgd_FechaVencimientoNueva = pvd_Fecha;
    }

    @JsonProperty("motivo")
    public String getMotivo() {
        return this.vgc_Motivo;
    }

    public void setMotivo(String pvc_Motivo) {
        this.vgc_Motivo = pvc_Motivo;
    }

    @JsonProperty("oficioSolicitud")
    public String getOficioSolicitud() {
        return this.vgc_OficioSolicitud;
    }

    public void setOficioSolicitud(String pvc_OficioSolicitud) {
        this.vgc_OficioSolicitud = pvc_OficioSolicitud;
    }

    @JsonProperty("numeroGestion")
    public String getNumeroGestion() {
        return this.vgc_NumeroGestion;
    }

    public void setNumeroGestion(String pvc_NumeroGestion) {
        this.vgc_NumeroGestion = pvc_NumeroGestion;
    }

    @JsonProperty("numeroAutorizacion")
    public String getNumeroAutorizacion() {
        return this.vgc_NumeroAutorizacion;
    }

    public void setNumeroAutorizacion(String pvc_NumeroAutorizacion) {
        this.vgc_NumeroAutorizacion = pvc_NumeroAutorizacion;
    }

    @JsonProperty("numeroContrato")
    public String getNumeroContrato() {
        return this.vgc_NumeroContrato;
    }

    public void setNumeroContrato(String pvc_NumeroContrato) {
        this.vgc_NumeroContrato = pvc_NumeroContrato;
    }

    @JsonProperty("idDocumentoOficio")
    public Integer getIdDocumentoOficio() {
        return this.vgn_IdDocumentoOficio;
    }

    public void setIdDocumentoOficio(Integer pvn_IdDocumentoOficio) {
        this.vgn_IdDocumentoOficio = pvn_IdDocumentoOficio;
    }

    @JsonProperty("idDocumentoResolucion")
    public Integer getIdDocumentoResolucion() {
        return this.vgn_IdDocumentoResolucion;
    }

    public void setIdDocumentoResolucion(Integer pvn_IdDocumentoResolucion) {
        this.vgn_IdDocumentoResolucion = pvn_IdDocumentoResolucion;
    }

    @JsonProperty("idUsuarioRegistro")
    public Integer getIdUsuarioRegistro() {
        return this.vgn_IdUsuarioRegistro;
    }

    public void setIdUsuarioRegistro(Integer pvn_IdUsuarioRegistro) {
        this.vgn_IdUsuarioRegistro = pvn_IdUsuarioRegistro;
    }

    @JsonProperty("fechaRegistro")
    public LocalDateTime getFechaRegistro() {
        return this.vgd_FechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime pvd_FechaRegistro) {
        this.vgd_FechaRegistro = pvd_FechaRegistro;
    }
}
