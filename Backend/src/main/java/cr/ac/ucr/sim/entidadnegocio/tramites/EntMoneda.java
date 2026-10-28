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
 * Descripción: Entidad de negocio de la tabla SIMM_MONEDA.
 */
@Entity
@Table(name = "SIMM_MONEDA")
public class EntMoneda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MONEDA")
    private Integer vgn_IdMoneda;

    @Column(name = "CODIGO_ISO", nullable = false, length = 3)
    private String vgc_CodigoIso;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String vgc_Nombre;

    @Column(name = "SIMBOLO", nullable = false, length = 5)
    private String vgc_Simbolo;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado;

    @JsonProperty("idMoneda")
    public Integer getIdMoneda() {
        return this.vgn_IdMoneda;
    }

    public void setIdMoneda(Integer pvn_IdMoneda) {
        this.vgn_IdMoneda = pvn_IdMoneda;
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

    @JsonProperty("simbolo")
    public String getSimbolo() {
        return this.vgc_Simbolo;
    }

    public void setSimbolo(String pvc_Simbolo) {
        this.vgc_Simbolo = pvc_Simbolo;
    }

    @JsonProperty("estado")
    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }
}
