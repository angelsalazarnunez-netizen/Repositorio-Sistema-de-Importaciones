package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Fila de la lista de expedientes de desalmacenaje especial. No representa una
 *   tabla: la construye la consulta de la capa de acceso a datos para la pantalla de seguimiento.
 */
public class EntExpedienteResumen {

    private final String vgc_ReferenciaInterna;
    private final String vgc_Estado;
    private final String vgc_OrigenDependencia;
    private final BigDecimal vgn_ValorEstimado;
    private final String vgc_Moneda;
    private final String vgc_UsuarioResponsable;
    private final LocalDateTime vgd_FechaCreacion;
    private final Long vgn_CantidadDocumentos;

    public EntExpedienteResumen(
            String pvc_ReferenciaInterna,
            String pvc_Estado,
            String pvc_OrigenDependencia,
            BigDecimal pvn_ValorEstimado,
            String pvc_Moneda,
            String pvc_UsuarioResponsable,
            LocalDateTime pvd_FechaCreacion,
            Long pvn_CantidadDocumentos) {
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
        this.vgc_Estado = pvc_Estado;
        this.vgc_OrigenDependencia = pvc_OrigenDependencia;
        this.vgn_ValorEstimado = pvn_ValorEstimado;
        this.vgc_Moneda = pvc_Moneda;
        this.vgc_UsuarioResponsable = pvc_UsuarioResponsable;
        this.vgd_FechaCreacion = pvd_FechaCreacion;
        this.vgn_CantidadDocumentos = pvn_CantidadDocumentos;
    }

    @JsonProperty("referenciaInterna")
    public String getReferenciaInterna() {
        return this.vgc_ReferenciaInterna;
    }

    @JsonProperty("estado")
    public String getEstado() {
        return this.vgc_Estado;
    }

    @JsonProperty("origenDependencia")
    public String getOrigenDependencia() {
        return this.vgc_OrigenDependencia;
    }

    @JsonProperty("valorEstimado")
    public BigDecimal getValorEstimado() {
        return this.vgn_ValorEstimado;
    }

    @JsonProperty("moneda")
    public String getMoneda() {
        return this.vgc_Moneda;
    }

    @JsonProperty("usuarioResponsable")
    public String getUsuarioResponsable() {
        return this.vgc_UsuarioResponsable;
    }

    @JsonProperty("fechaCreacion")
    public LocalDateTime getFechaCreacion() {
        return this.vgd_FechaCreacion;
    }

    @JsonProperty("cantidadDocumentos")
    public Long getCantidadDocumentos() {
        return this.vgn_CantidadDocumentos;
    }
}
