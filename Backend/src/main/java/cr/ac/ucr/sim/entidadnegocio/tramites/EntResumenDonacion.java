package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 15/09/2026
 * Descripción: Fila de la lista de expedientes de donación (HU-044). No representa una tabla:
 *   la construye la consulta de la capa de acceso a datos para la pantalla de gestión.
 */
public class EntResumenDonacion {

    private final String vgc_ReferenciaInterna;
    private final String vgc_Estado;
    private final String vgc_TipoDonacion;
    private final String vgc_NombreDonante;
    private final BigDecimal vgn_ValorEstimado;
    private final String vgc_Moneda;
    private final String vgc_UsuarioResponsable;
    private final LocalDateTime vgd_FechaCreacion;
    private final Long vgn_CantidadDocumentos;

    public EntResumenDonacion(
            String pvc_ReferenciaInterna,
            String pvc_Estado,
            String pvc_TipoDonacion,
            String pvc_NombreDonante,
            BigDecimal pvn_ValorEstimado,
            String pvc_Moneda,
            String pvc_UsuarioResponsable,
            LocalDateTime pvd_FechaCreacion,
            Long pvn_CantidadDocumentos) {
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
        this.vgc_Estado = pvc_Estado;
        this.vgc_TipoDonacion = pvc_TipoDonacion;
        this.vgc_NombreDonante = pvc_NombreDonante;
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

    @JsonProperty("tipoDonacion")
    public String getTipoDonacion() {
        return this.vgc_TipoDonacion;
    }

    @JsonProperty("nombreDonante")
    public String getNombreDonante() {
        return this.vgc_NombreDonante;
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
