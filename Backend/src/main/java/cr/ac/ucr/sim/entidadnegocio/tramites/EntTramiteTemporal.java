package cr.ac.ucr.sim.entidadnegocio.tramites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Autor: Camila Pastor Castro
 * Fecha: 03/10/2026
 * Descripción: Entidad de negocio de la tabla SIMT_TRAMITE_TEMPORAL. Contiene los datos propios
 *   de un trámite temporal y su fecha de vencimiento, que gobierna las alertas (HU-027).
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: el trámite temporal comparte la llave primaria con SIMT_TRAMITE (HU-025): la
 *     referencia, el tipo, la descripción y el responsable se leen del expediente común. Se
 *     agregan los datos de la solicitud, del ingreso, de la prórroga y de la salida, y la fecha de
 *     vencimiento pasa a ser opcional. Se conservan el plazo, la extensión y el contrato.
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: se agrega el motivo de la prórroga.
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: se agregan los datos que solo registra la exportación temporal (HU-026):
 *     antecedente, garantía, peso, país de destino, notificaciones y datos de la reimportación.
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 18/10/2026
 *   Descripción: se agrega si la reimportación paga impuestos y el monto pagado en colones.
 */
@Entity
@Table(name = "SIMT_TRAMITE_TEMPORAL")
public class EntTramiteTemporal {

    @Id
    @Column(name = "ID_TRAMITE")
    private Integer vgn_IdTramite;

    @Column(name = "UNIDAD_SOLICITANTE", nullable = false, length = 120)
    private String vgc_UnidadSolicitante;

    @Column(name = "OFICIO_SOLICITUD", nullable = false, length = 80)
    private String vgc_OficioSolicitud;

    @Column(name = "FECHA_RECEPCION", nullable = false)
    private LocalDate vgd_FechaRecepcion;

    @Column(name = "ESTADO_MERCANCIA", nullable = false, length = 15)
    private String vgc_EstadoMercancia;

    @Column(name = "NUMERO_FACTURA", length = 80)
    private String vgc_NumeroFactura;

    @Column(name = "OFICIO_ADUANA", length = 120)
    private String vgc_OficioAduana;

    @Column(name = "NUMERO_GESTION", length = 80)
    private String vgc_NumeroGestion;

    @Column(name = "RESOLUCION_AUTORIZACION", length = 120)
    private String vgc_ResolucionAutorizacion;

    @Column(name = "NUMERO_DUA_INICIO", length = 80)
    private String vgc_NumeroDuaInicio;

    @Column(name = "FECHA_DUA_INICIO")
    private LocalDate vgd_FechaDuaInicio;

    @Column(name = "FECHA_VENCIMIENTO")
    private LocalDate vgd_FechaVencimiento;

    @Column(name = "UNIDAD_PLAZO", length = 10)
    private String vgc_UnidadPlazo;

    @Column(name = "CANTIDAD_PLAZO")
    private Integer vgn_CantidadPlazo;

    @Column(name = "UNIDAD_EXTENSION", length = 10)
    private String vgc_UnidadExtension;

    @Column(name = "CANTIDAD_EXTENSION")
    private Integer vgn_CantidadExtension;

    @Column(name = "NUMERO_CONTRATO", length = 45)
    private String vgc_NumeroContrato;

    @Column(name = "MOTIVO_PRORROGA", length = 500)
    private String vgc_MotivoProrroga;

    @Column(name = "OFICIO_PRORROGA", length = 120)
    private String vgc_OficioProrroga;

    @Column(name = "NUMERO_GESTION_PRORROGA", length = 80)
    private String vgc_NumeroGestionProrroga;

    @Column(name = "AUTORIZACION_PRORROGA", length = 120)
    private String vgc_AutorizacionProrroga;

    @Column(name = "NUMERO_DUA_CIERRE", length = 80)
    private String vgc_NumeroDuaCierre;

    @Column(name = "FECHA_DUA_CIERRE")
    private LocalDate vgd_FechaDuaCierre;

    @Column(name = "CANCELACION_REGIMEN", length = 120)
    private String vgc_CancelacionRegimen;

    @Column(name = "FECHA_CANCELACION")
    private LocalDate vgd_FechaCancelacion;

    // Datos que solo registra la exportación temporal (HU-026); quedan nulos en la importación.

    @Column(name = "ANTECEDENTE", length = 120)
    private String vgc_Antecedente;

    @Column(name = "EN_GARANTIA")
    private Boolean vgb_EnGarantia;

    @Column(name = "PESO_KG", precision = 10, scale = 2)
    private BigDecimal vgn_PesoKg;

    @Column(name = "ID_PAIS_DESTINO")
    private Integer vgn_IdPaisDestino;

    @Column(name = "OFICIO_NOTIFICACION_USUARIO", length = 120)
    private String vgc_OficioNotificacionUsuario;

    @Column(name = "NOTIFICACION_DUA_ADUANA", length = 120)
    private String vgc_NotificacionDuaAduana;

    @Column(name = "SOLICITUD_INGRESO", length = 120)
    private String vgc_SolicitudIngreso;

    @Column(name = "FECHA_REINGRESO")
    private LocalDate vgd_FechaReingreso;

    @Column(name = "DOCUMENTO_EMBARQUE", length = 80)
    private String vgc_DocumentoEmbarque;

    @Column(name = "FECHA_DESALMACENAJE")
    private LocalDate vgd_FechaDesalmacenaje;

    // Exportación: la reimportación (RI-) puede pagar impuestos por los trabajos hechos al equipo
    // o no pagarlos cuando la reparación fue por garantía (pedido de la Unidad, Sprint 2).
    @Column(name = "PAGA_IMPUESTOS_REIMPORTACION")
    private Boolean vgb_PagaImpuestosReimportacion;

    @Column(name = "MONTO_IMPUESTOS_REIMPORTACION", precision = 15, scale = 2)
    private BigDecimal vgn_MontoImpuestosReimportacion;

    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    public String getUnidadSolicitante() {
        return this.vgc_UnidadSolicitante;
    }

    public void setUnidadSolicitante(String pvc_UnidadSolicitante) {
        this.vgc_UnidadSolicitante = pvc_UnidadSolicitante;
    }

    public String getOficioSolicitud() {
        return this.vgc_OficioSolicitud;
    }

    public void setOficioSolicitud(String pvc_OficioSolicitud) {
        this.vgc_OficioSolicitud = pvc_OficioSolicitud;
    }

    public LocalDate getFechaRecepcion() {
        return this.vgd_FechaRecepcion;
    }

    public void setFechaRecepcion(LocalDate pvd_FechaRecepcion) {
        this.vgd_FechaRecepcion = pvd_FechaRecepcion;
    }

    public String getEstadoMercancia() {
        return this.vgc_EstadoMercancia;
    }

    public void setEstadoMercancia(String pvc_EstadoMercancia) {
        this.vgc_EstadoMercancia = pvc_EstadoMercancia;
    }

    public String getNumeroFactura() {
        return this.vgc_NumeroFactura;
    }

    public void setNumeroFactura(String pvc_NumeroFactura) {
        this.vgc_NumeroFactura = pvc_NumeroFactura;
    }

    public String getOficioAduana() {
        return this.vgc_OficioAduana;
    }

    public void setOficioAduana(String pvc_OficioAduana) {
        this.vgc_OficioAduana = pvc_OficioAduana;
    }

    public String getNumeroGestion() {
        return this.vgc_NumeroGestion;
    }

    public void setNumeroGestion(String pvc_NumeroGestion) {
        this.vgc_NumeroGestion = pvc_NumeroGestion;
    }

    public String getResolucionAutorizacion() {
        return this.vgc_ResolucionAutorizacion;
    }

    public void setResolucionAutorizacion(String pvc_ResolucionAutorizacion) {
        this.vgc_ResolucionAutorizacion = pvc_ResolucionAutorizacion;
    }

    public String getNumeroDuaInicio() {
        return this.vgc_NumeroDuaInicio;
    }

    public void setNumeroDuaInicio(String pvc_NumeroDuaInicio) {
        this.vgc_NumeroDuaInicio = pvc_NumeroDuaInicio;
    }

    public LocalDate getFechaDuaInicio() {
        return this.vgd_FechaDuaInicio;
    }

    public void setFechaDuaInicio(LocalDate pvd_FechaDuaInicio) {
        this.vgd_FechaDuaInicio = pvd_FechaDuaInicio;
    }

    public LocalDate getFechaVencimiento() {
        return this.vgd_FechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate pvd_FechaVencimiento) {
        this.vgd_FechaVencimiento = pvd_FechaVencimiento;
    }

    public String getUnidadPlazo() {
        return this.vgc_UnidadPlazo;
    }

    public void setUnidadPlazo(String pvc_UnidadPlazo) {
        this.vgc_UnidadPlazo = pvc_UnidadPlazo;
    }

    public Integer getCantidadPlazo() {
        return this.vgn_CantidadPlazo;
    }

    public void setCantidadPlazo(Integer pvn_CantidadPlazo) {
        this.vgn_CantidadPlazo = pvn_CantidadPlazo;
    }

    public String getUnidadExtension() {
        return this.vgc_UnidadExtension;
    }

    public void setUnidadExtension(String pvc_UnidadExtension) {
        this.vgc_UnidadExtension = pvc_UnidadExtension;
    }

    public Integer getCantidadExtension() {
        return this.vgn_CantidadExtension;
    }

    public void setCantidadExtension(Integer pvn_CantidadExtension) {
        this.vgn_CantidadExtension = pvn_CantidadExtension;
    }

    public String getNumeroContrato() {
        return this.vgc_NumeroContrato;
    }

    public void setNumeroContrato(String pvc_NumeroContrato) {
        this.vgc_NumeroContrato = pvc_NumeroContrato;
    }

    public String getMotivoProrroga() {
        return this.vgc_MotivoProrroga;
    }

    public void setMotivoProrroga(String pvc_MotivoProrroga) {
        this.vgc_MotivoProrroga = pvc_MotivoProrroga;
    }

    public String getOficioProrroga() {
        return this.vgc_OficioProrroga;
    }

    public void setOficioProrroga(String pvc_OficioProrroga) {
        this.vgc_OficioProrroga = pvc_OficioProrroga;
    }

    public String getNumeroGestionProrroga() {
        return this.vgc_NumeroGestionProrroga;
    }

    public void setNumeroGestionProrroga(String pvc_NumeroGestionProrroga) {
        this.vgc_NumeroGestionProrroga = pvc_NumeroGestionProrroga;
    }

    public String getAutorizacionProrroga() {
        return this.vgc_AutorizacionProrroga;
    }

    public void setAutorizacionProrroga(String pvc_AutorizacionProrroga) {
        this.vgc_AutorizacionProrroga = pvc_AutorizacionProrroga;
    }

    public String getNumeroDuaCierre() {
        return this.vgc_NumeroDuaCierre;
    }

    public void setNumeroDuaCierre(String pvc_NumeroDuaCierre) {
        this.vgc_NumeroDuaCierre = pvc_NumeroDuaCierre;
    }

    public LocalDate getFechaDuaCierre() {
        return this.vgd_FechaDuaCierre;
    }

    public void setFechaDuaCierre(LocalDate pvd_FechaDuaCierre) {
        this.vgd_FechaDuaCierre = pvd_FechaDuaCierre;
    }

    public String getCancelacionRegimen() {
        return this.vgc_CancelacionRegimen;
    }

    public void setCancelacionRegimen(String pvc_CancelacionRegimen) {
        this.vgc_CancelacionRegimen = pvc_CancelacionRegimen;
    }

    public LocalDate getFechaCancelacion() {
        return this.vgd_FechaCancelacion;
    }

    public void setFechaCancelacion(LocalDate pvd_FechaCancelacion) {
        this.vgd_FechaCancelacion = pvd_FechaCancelacion;
    }

    public String getAntecedente() {
        return this.vgc_Antecedente;
    }

    public void setAntecedente(String pvc_Antecedente) {
        this.vgc_Antecedente = pvc_Antecedente;
    }

    public Boolean getEnGarantia() {
        return this.vgb_EnGarantia;
    }

    public void setEnGarantia(Boolean pvb_EnGarantia) {
        this.vgb_EnGarantia = pvb_EnGarantia;
    }

    public BigDecimal getPesoKg() {
        return this.vgn_PesoKg;
    }

    public void setPesoKg(BigDecimal pvn_PesoKg) {
        this.vgn_PesoKg = pvn_PesoKg;
    }

    public Integer getIdPaisDestino() {
        return this.vgn_IdPaisDestino;
    }

    public void setIdPaisDestino(Integer pvn_IdPaisDestino) {
        this.vgn_IdPaisDestino = pvn_IdPaisDestino;
    }

    public String getOficioNotificacionUsuario() {
        return this.vgc_OficioNotificacionUsuario;
    }

    public void setOficioNotificacionUsuario(String pvc_OficioNotificacionUsuario) {
        this.vgc_OficioNotificacionUsuario = pvc_OficioNotificacionUsuario;
    }

    public String getNotificacionDuaAduana() {
        return this.vgc_NotificacionDuaAduana;
    }

    public void setNotificacionDuaAduana(String pvc_NotificacionDuaAduana) {
        this.vgc_NotificacionDuaAduana = pvc_NotificacionDuaAduana;
    }

    public String getSolicitudIngreso() {
        return this.vgc_SolicitudIngreso;
    }

    public void setSolicitudIngreso(String pvc_SolicitudIngreso) {
        this.vgc_SolicitudIngreso = pvc_SolicitudIngreso;
    }

    public LocalDate getFechaReingreso() {
        return this.vgd_FechaReingreso;
    }

    public void setFechaReingreso(LocalDate pvd_FechaReingreso) {
        this.vgd_FechaReingreso = pvd_FechaReingreso;
    }

    public String getDocumentoEmbarque() {
        return this.vgc_DocumentoEmbarque;
    }

    public void setDocumentoEmbarque(String pvc_DocumentoEmbarque) {
        this.vgc_DocumentoEmbarque = pvc_DocumentoEmbarque;
    }

    public LocalDate getFechaDesalmacenaje() {
        return this.vgd_FechaDesalmacenaje;
    }

    public void setFechaDesalmacenaje(LocalDate pvd_FechaDesalmacenaje) {
        this.vgd_FechaDesalmacenaje = pvd_FechaDesalmacenaje;
    }

    public Boolean getPagaImpuestosReimportacion() {
        return this.vgb_PagaImpuestosReimportacion;
    }

    public void setPagaImpuestosReimportacion(Boolean pvb_PagaImpuestosReimportacion) {
        this.vgb_PagaImpuestosReimportacion = pvb_PagaImpuestosReimportacion;
    }

    public BigDecimal getMontoImpuestosReimportacion() {
        return this.vgn_MontoImpuestosReimportacion;
    }

    public void setMontoImpuestosReimportacion(BigDecimal pvn_MontoImpuestosReimportacion) {
        this.vgn_MontoImpuestosReimportacion = pvn_MontoImpuestosReimportacion;
    }
}
