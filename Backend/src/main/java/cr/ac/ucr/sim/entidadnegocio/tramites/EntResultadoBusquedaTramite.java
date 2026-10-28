package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Fila del listado de la búsqueda de trámites (HU-034). No representa una tabla: la
 *   construye la consulta de la capa de acceso a datos a partir de SIMT_TRAMITE y sus catálogos.
 *   Solo contiene los datos que ya registra el sistema. Las fechas de envío y desalmacenaje, los
 *   impuestos, los costos de permisos y el total CIF se agregarán cuando existan los módulos que
 *   los registran; mientras tanto el listado los informa como no registrados.
 */
public class EntResultadoBusquedaTramite {

    private final Integer vgn_IdTramite;
    private final String vgc_ReferenciaInterna;
    private final LocalDateTime vgd_FechaRegistro;
    private final String vgc_TipoTramite;
    private final String vgc_RegimenAduanero;
    private final String vgc_Usuario;
    private final String vgc_CategoriaEstado;
    private final String vgc_EstadoTramite;
    private final BigDecimal vgn_ValorMercancia;
    private final String vgc_Moneda;
    private final String vgc_TipoExoneracion;
    private String vgc_CategoriaEstadoNombre;
    private String vgc_TipoExoneracionNombre;

    public EntResultadoBusquedaTramite(
            Integer pvn_IdTramite,
            String pvc_ReferenciaInterna,
            LocalDateTime pvd_FechaRegistro,
            String pvc_TipoTramite,
            String pvc_RegimenAduanero,
            String pvc_Usuario,
            String pvc_CategoriaEstado,
            String pvc_EstadoTramite,
            BigDecimal pvn_ValorMercancia,
            String pvc_Moneda,
            String pvc_TipoExoneracion) {
        this.vgn_IdTramite = pvn_IdTramite;
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
        this.vgd_FechaRegistro = pvd_FechaRegistro;
        this.vgc_TipoTramite = pvc_TipoTramite;
        this.vgc_RegimenAduanero = pvc_RegimenAduanero;
        this.vgc_Usuario = pvc_Usuario;
        this.vgc_CategoriaEstado = pvc_CategoriaEstado;
        this.vgc_EstadoTramite = pvc_EstadoTramite;
        this.vgn_ValorMercancia = pvn_ValorMercancia;
        this.vgc_Moneda = pvc_Moneda;
        this.vgc_TipoExoneracion = pvc_TipoExoneracion;
    }

    @JsonProperty("idTramite")
    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    @JsonProperty("referenciaInterna")
    public String getReferenciaInterna() {
        return this.vgc_ReferenciaInterna;
    }

    @JsonProperty("fechaRegistro")
    public LocalDateTime getFechaRegistro() {
        return this.vgd_FechaRegistro;
    }

    @JsonProperty("tipoTramite")
    public String getTipoTramite() {
        return this.vgc_TipoTramite;
    }

    @JsonProperty("regimenAduanero")
    public String getRegimenAduanero() {
        return this.vgc_RegimenAduanero;
    }

    @JsonProperty("usuario")
    public String getUsuario() {
        return this.vgc_Usuario;
    }

    @JsonProperty("categoriaEstado")
    public String getCategoriaEstado() {
        return this.vgc_CategoriaEstado;
    }

    @JsonProperty("categoriaEstadoNombre")
    public String getCategoriaEstadoNombre() {
        return this.vgc_CategoriaEstadoNombre;
    }

    public void setCategoriaEstadoNombre(String pvc_CategoriaEstadoNombre) {
        this.vgc_CategoriaEstadoNombre = pvc_CategoriaEstadoNombre;
    }

    @JsonProperty("estadoTramite")
    public String getEstadoTramite() {
        return this.vgc_EstadoTramite;
    }

    @JsonProperty("valorMercancia")
    public BigDecimal getValorMercancia() {
        return this.vgn_ValorMercancia;
    }

    @JsonProperty("moneda")
    public String getMoneda() {
        return this.vgc_Moneda;
    }

    @JsonProperty("tipoExoneracion")
    public String getTipoExoneracion() {
        return this.vgc_TipoExoneracion;
    }

    @JsonProperty("tipoExoneracionNombre")
    public String getTipoExoneracionNombre() {
        return this.vgc_TipoExoneracionNombre;
    }

    public void setTipoExoneracionNombre(String pvc_TipoExoneracionNombre) {
        this.vgc_TipoExoneracionNombre = pvc_TipoExoneracionNombre;
    }
}
