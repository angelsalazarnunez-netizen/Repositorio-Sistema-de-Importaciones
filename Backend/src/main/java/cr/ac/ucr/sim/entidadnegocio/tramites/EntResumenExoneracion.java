package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 25/10/2026
 * Descripción: Fila de la lista de expedientes de exoneración. No representa una tabla: la construye
 *   la consulta de la capa de acceso a datos para la pantalla de entrada del módulo, que, como las de
 *   donaciones y desalmacenajes, muestra primero todas las exoneraciones registradas (comentario de
 *   las pruebas de usabilidad).
 */
public class EntResumenExoneracion {

    private final Integer vgn_IdTramite;
    private final String vgc_ReferenciaInterna;
    private final String vgc_Estado;
    private final String vgc_TipoExoneracion;
    private final String vgc_NumeroContratacion;
    private final String vgc_UsuarioResponsable;
    private final LocalDateTime vgd_FechaCreacion;
    private final Long vgn_CantidadDocumentos;

    public EntResumenExoneracion(
            Integer pvn_IdTramite,
            String pvc_ReferenciaInterna,
            String pvc_Estado,
            String pvc_TipoExoneracion,
            String pvc_NumeroContratacion,
            String pvc_UsuarioResponsable,
            LocalDateTime pvd_FechaCreacion,
            Long pvn_CantidadDocumentos) {
        this.vgn_IdTramite = pvn_IdTramite;
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
        this.vgc_Estado = pvc_Estado;
        this.vgc_TipoExoneracion = pvc_TipoExoneracion;
        this.vgc_NumeroContratacion = pvc_NumeroContratacion;
        this.vgc_UsuarioResponsable = pvc_UsuarioResponsable;
        this.vgd_FechaCreacion = pvd_FechaCreacion;
        this.vgn_CantidadDocumentos = pvn_CantidadDocumentos;
    }

    @JsonProperty("idTramite")
    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    @JsonProperty("referenciaInterna")
    public String getReferenciaInterna() {
        return this.vgc_ReferenciaInterna;
    }

    @JsonProperty("estado")
    public String getEstado() {
        return this.vgc_Estado;
    }

    @JsonProperty("tipoExoneracion")
    public String getTipoExoneracion() {
        return this.vgc_TipoExoneracion;
    }

    @JsonProperty("numeroContratacion")
    public String getNumeroContratacion() {
        return this.vgc_NumeroContratacion;
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
