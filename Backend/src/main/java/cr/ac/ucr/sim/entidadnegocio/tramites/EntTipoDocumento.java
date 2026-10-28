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
 * Descripción: Entidad de negocio de la tabla SIMM_TIPO_DOCUMENTO.
 */
@Entity
@Table(name = "SIMM_TIPO_DOCUMENTO")
public class EntTipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TIPO_DOCUMENTO")
    private Integer vgn_IdTipoDocumento;

    @Column(name = "CODIGO", nullable = false, length = 20)
    private String vgc_Codigo;

    @Column(name = "NOMBRE", nullable = false, length = 80)
    private String vgc_Nombre;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado;

    @JsonProperty("idTipoDocumento")
    public Integer getIdTipoDocumento() {
        return this.vgn_IdTipoDocumento;
    }

    public void setIdTipoDocumento(Integer pvn_IdTipoDocumento) {
        this.vgn_IdTipoDocumento = pvn_IdTipoDocumento;
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

    @JsonProperty("estado")
    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }
}
