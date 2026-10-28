package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Entidad de negocio de la tabla SIMT_TRAMITE. Contiene el expediente común
 *   a todos los tipos de trámite de importación.
 *
 * Historial de cambios:
 *   Responsable: Katherine Susana Valverde Fallas
 *   Fecha: 15/09/2026
 *   Descripción: la moneda y el valor estimado pasan a ser opcionales, porque la solicitud de
 *     exoneración (HU-018) no los declara.
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: la descripción admite 255 caracteres (HU-026): la Unidad describe los equipos
 *     temporales con su marca, modelo, serie y placa.
 */
@Entity
@Table(name = "SIMT_TRAMITE")
public class EntTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TRAMITE")
    private Integer vgn_IdTramite;

    @Column(name = "REFERENCIA_INTERNA", nullable = false, length = 45, unique = true)
    private String vgc_ReferenciaInterna;

    @Column(name = "ID_TIPO_TRAMITE", nullable = false)
    private Integer vgn_IdTipoTramite;

    @Column(name = "ID_ESTADO_TRAMITE", nullable = false)
    private Integer vgn_IdEstadoTramite;

    @Column(name = "ID_USUARIO_RESPONSABLE", nullable = false)
    private Integer vgn_IdUsuarioResponsable;

    @Column(name = "ID_PAIS_ORIGEN")
    private Integer vgn_IdPaisOrigen;

    @Column(name = "ID_MONEDA")
    private Integer vgn_IdMoneda;

    @Column(name = "VALOR_ESTIMADO", precision = 13, scale = 2)
    private BigDecimal vgn_ValorEstimado;

    @Column(name = "DESCRIPCION", length = 255)
    private String vgc_Descripcion;

    @Column(name = "REGIMEN_ADUANERO", length = 60)
    private String vgc_RegimenAduanero;

    @Column(name = "ADUANA_CONTROL", length = 60)
    private String vgc_AduanaControl;

    @Column(name = "TIPO_PARTIDO_RESPONSABLE", length = 25)
    private String vgc_TipoPartidoResponsable;

    @Column(name = "PARTIDO_RESPONSABLE", length = 120)
    private String vgc_PartidoResponsable;

    @Column(name = "IMPUESTOS_PAGADOS", precision = 13, scale = 2)
    private BigDecimal vgn_ImpuestosPagados;

    @Column(name = "IMPUESTOS_EXONERADOS", precision = 13, scale = 2)
    private BigDecimal vgn_ImpuestosExonerados;

    @Column(name = "MONTO_BODEGAJE", precision = 13, scale = 2)
    private BigDecimal vgn_MontoBodegaje;

    @Column(name = "OBSERVACIONES", length = 4000)
    private String vgc_Observaciones;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime vgd_FechaCreacion;

    @Column(name = "FECHA_CIERRE")
    private LocalDate vgd_FechaCierre;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Asigna la fecha de creación del expediente cuando no viene indicada.
     */
    @PrePersist
    public void AsignarFechaCreacion() {
        if (this.vgd_FechaCreacion == null) {
            this.vgd_FechaCreacion = LocalDateTime.now();
        }
    }

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

    @JsonProperty("idTipoTramite")
    public Integer getIdTipoTramite() {
        return this.vgn_IdTipoTramite;
    }

    public void setIdTipoTramite(Integer pvn_IdTipoTramite) {
        this.vgn_IdTipoTramite = pvn_IdTipoTramite;
    }

    @JsonProperty("idEstadoTramite")
    public Integer getIdEstadoTramite() {
        return this.vgn_IdEstadoTramite;
    }

    public void setIdEstadoTramite(Integer pvn_IdEstadoTramite) {
        this.vgn_IdEstadoTramite = pvn_IdEstadoTramite;
    }

    @JsonProperty("idUsuarioResponsable")
    public Integer getIdUsuarioResponsable() {
        return this.vgn_IdUsuarioResponsable;
    }

    public void setIdUsuarioResponsable(Integer pvn_IdUsuarioResponsable) {
        this.vgn_IdUsuarioResponsable = pvn_IdUsuarioResponsable;
    }

    @JsonProperty("idPaisOrigen")
    public Integer getIdPaisOrigen() {
        return this.vgn_IdPaisOrigen;
    }

    public void setIdPaisOrigen(Integer pvn_IdPaisOrigen) {
        this.vgn_IdPaisOrigen = pvn_IdPaisOrigen;
    }

    @JsonProperty("idMoneda")
    public Integer getIdMoneda() {
        return this.vgn_IdMoneda;
    }

    public void setIdMoneda(Integer pvn_IdMoneda) {
        this.vgn_IdMoneda = pvn_IdMoneda;
    }

    @JsonProperty("valorEstimado")
    public BigDecimal getValorEstimado() {
        return this.vgn_ValorEstimado;
    }

    public void setValorEstimado(BigDecimal pvn_ValorEstimado) {
        this.vgn_ValorEstimado = pvn_ValorEstimado;
    }

    @JsonProperty("descripcion")
    public String getDescripcion() {
        return this.vgc_Descripcion;
    }

    public void setDescripcion(String pvc_Descripcion) {
        this.vgc_Descripcion = pvc_Descripcion;
    }

    @JsonProperty("regimenAduanero")
    public String getRegimenAduanero() {
        return this.vgc_RegimenAduanero;
    }

    public void setRegimenAduanero(String pvc_RegimenAduanero) {
        this.vgc_RegimenAduanero = pvc_RegimenAduanero;
    }

    @JsonProperty("aduanaControl")
    public String getAduanaControl() {
        return this.vgc_AduanaControl;
    }

    public void setAduanaControl(String pvc_AduanaControl) {
        this.vgc_AduanaControl = pvc_AduanaControl;
    }

    @JsonProperty("observaciones")
    public String getObservaciones() {
        return this.vgc_Observaciones;
    }

    public void setObservaciones(String pvc_Observaciones) {
        this.vgc_Observaciones = pvc_Observaciones;
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

    public String getPartidoResponsable() {
        return this.vgc_PartidoResponsable;
    }

    public void setPartidoResponsable(String pvc_PartidoResponsable) {
        this.vgc_PartidoResponsable = pvc_PartidoResponsable;
    }

    public BigDecimal getImpuestosPagados() {
        return this.vgn_ImpuestosPagados;
    }

    public void setImpuestosPagados(BigDecimal pvn_ImpuestosPagados) {
        this.vgn_ImpuestosPagados = pvn_ImpuestosPagados;
    }

    public BigDecimal getImpuestosExonerados() {
        return this.vgn_ImpuestosExonerados;
    }

    public void setImpuestosExonerados(BigDecimal pvn_ImpuestosExonerados) {
        this.vgn_ImpuestosExonerados = pvn_ImpuestosExonerados;
    }

    public BigDecimal getMontoBodegaje() {
        return this.vgn_MontoBodegaje;
    }

    public void setMontoBodegaje(BigDecimal pvn_MontoBodegaje) {
        this.vgn_MontoBodegaje = pvn_MontoBodegaje;
    }

    public String getTipoPartidoResponsable() {
        return this.vgc_TipoPartidoResponsable;
    }

    public void setTipoPartidoResponsable(String pvc_TipoPartidoResponsable) {
        this.vgc_TipoPartidoResponsable = pvc_TipoPartidoResponsable;
    }
}
