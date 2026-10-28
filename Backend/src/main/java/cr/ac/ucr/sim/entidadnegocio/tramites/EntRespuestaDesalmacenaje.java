package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Confirmación que devuelve el servicio al registrar un desalmacenaje especial.
 *   Incluye la referencia interna asignada y el estado inicial del expediente.
 */
public class EntRespuestaDesalmacenaje {

    private String vgc_Mensaje;
    private Integer vgn_IdTramite;
    private String vgc_ReferenciaInterna;
    private String vgc_Estado;
    private String vgc_UsuarioResponsable;
    private Integer vgn_CantidadDocumentos;
    private LocalDateTime vgd_FechaCreacion;

    @JsonProperty("mensaje")
    public String getMensaje() {
        return this.vgc_Mensaje;
    }

    public void setMensaje(String pvc_Mensaje) {
        this.vgc_Mensaje = pvc_Mensaje;
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

    @JsonProperty("estado")
    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }

    @JsonProperty("usuarioResponsable")
    public String getUsuarioResponsable() {
        return this.vgc_UsuarioResponsable;
    }

    public void setUsuarioResponsable(String pvc_UsuarioResponsable) {
        this.vgc_UsuarioResponsable = pvc_UsuarioResponsable;
    }

    @JsonProperty("cantidadDocumentos")
    public Integer getCantidadDocumentos() {
        return this.vgn_CantidadDocumentos;
    }

    public void setCantidadDocumentos(Integer pvn_CantidadDocumentos) {
        this.vgn_CantidadDocumentos = pvn_CantidadDocumentos;
    }

    @JsonProperty("fechaCreacion")
    public LocalDateTime getFechaCreacion() {
        return this.vgd_FechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime pvd_FechaCreacion) {
        this.vgd_FechaCreacion = pvd_FechaCreacion;
    }
}
