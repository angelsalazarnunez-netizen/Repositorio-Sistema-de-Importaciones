package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Entidad de negocio de la tabla SIMM_TIPO_TRAMITE. El prefijo de referencia
 *   y los indicadores de SICOP y orden de compra gobiernan por datos las reglas de la HU-031.
 */
@Entity
@Table(name = "SIMM_TIPO_TRAMITE")
public class EntTipoTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TIPO_TRAMITE")
    private Integer vgn_IdTipoTramite;

    @Column(name = "CODIGO", nullable = false, length = 15)
    private String vgc_Codigo;

    @Column(name = "NOMBRE", nullable = false, length = 60)
    private String vgc_Nombre;

    @Column(name = "PREFIJO_REFERENCIA", nullable = false, length = 10)
    private String vgc_PrefijoReferencia;

    @Column(name = "REQUIERE_SICOP", nullable = false)
    private Boolean vgb_RequiereSicop;

    @Column(name = "REQUIERE_ORDEN_COMPRA", nullable = false)
    private Boolean vgb_RequiereOrdenCompra;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado;

    @JsonProperty("idTipoTramite")
    public Integer getIdTipoTramite() {
        return this.vgn_IdTipoTramite;
    }

    public void setIdTipoTramite(Integer pvn_IdTipoTramite) {
        this.vgn_IdTipoTramite = pvn_IdTipoTramite;
    }

    @JsonProperty("codigo")
    public String getCodigo() {
        return this.vgc_Codigo;
    }

    public void setCodigo(String pvc_Codigo) {
        this.vgc_Codigo = pvc_Codigo;
    }

    @JsonProperty("nombre")
    public String getNombre() {
        return this.vgc_Nombre;
    }

    public void setNombre(String pvc_Nombre) {
        this.vgc_Nombre = pvc_Nombre;
    }

    @JsonProperty("prefijoReferencia")
    public String getPrefijoReferencia() {
        return this.vgc_PrefijoReferencia;
    }

    public void setPrefijoReferencia(String pvc_PrefijoReferencia) {
        this.vgc_PrefijoReferencia = pvc_PrefijoReferencia;
    }

    @JsonProperty("requiereSicop")
    public Boolean getRequiereSicop() {
        return this.vgb_RequiereSicop;
    }

    public void setRequiereSicop(Boolean pvb_RequiereSicop) {
        this.vgb_RequiereSicop = pvb_RequiereSicop;
    }

    @JsonProperty("requiereOrdenCompra")
    public Boolean getRequiereOrdenCompra() {
        return this.vgb_RequiereOrdenCompra;
    }

    public void setRequiereOrdenCompra(Boolean pvb_RequiereOrdenCompra) {
        this.vgb_RequiereOrdenCompra = pvb_RequiereOrdenCompra;
    }

    @JsonProperty("estado")
    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }
}
