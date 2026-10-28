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
 * Descripción: Entidad de negocio de la tabla SIMM_PAIS.
 */
@Entity
@Table(name = "SIMM_PAIS")
public class EntPais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PAIS")
    private Integer vgn_IdPais;

    @Column(name = "CODIGO_ISO", nullable = false, length = 3)
    private String vgc_CodigoIso;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String vgc_Nombre;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado;

    @JsonProperty("idPais")
    public Integer getIdPais() {
        return this.vgn_IdPais;
    }

    public void setIdPais(Integer pvn_IdPais) {
        this.vgn_IdPais = pvn_IdPais;
    }

    @JsonProperty("codigoIso")
    public String getCodigoIso() {
        return this.vgc_CodigoIso;
    }

    public void setCodigoIso(String pvc_CodigoIso) {
        this.vgc_CodigoIso = pvc_CodigoIso;
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
