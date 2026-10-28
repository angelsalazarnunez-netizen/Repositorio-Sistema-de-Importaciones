package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;
import cr.ac.ucr.sim.utilerias.Constantes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Entidad de negocio de la tabla SIMM_ESTADO_TRAMITE. El estado marcado
 *   con ES_INICIAL es el que se asigna al crear el expediente.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 16/09/2026
 *   Descripción: se agrega la categoría funcional del estado (pendiente de ingreso, trámite activo
 *     o trámite finalizado) que utiliza la búsqueda de trámites (HU-034).
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 30/09/2026
 *   Descripción: un estado nuevo nace como trámite activo, igual que el valor por defecto de la
 *     columna, para que los estados que se crean desde la aplicación (HU-006) tengan categoría.
 */
@Entity
@Table(name = "SIMM_ESTADO_TRAMITE")
public class EntEstadoTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ESTADO_TRAMITE")
    private Integer vgn_IdEstadoTramite;

    @Column(name = "ID_TIPO_TRAMITE", nullable = false)
    private Integer vgn_IdTipoTramite;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String vgc_Nombre;

    @Column(name = "ES_INICIAL", nullable = false)
    private Boolean vgb_EsInicial;

    @Column(name = "ES_FINAL", nullable = false)
    private Boolean vgb_EsFinal;

    @Column(name = "CATEGORIA_ESTADO", nullable = false, length = 3)
    private String vgc_CategoriaEstado = Constantes.CATEGORIA_ESTADO_TRAMITE_ACTIVO;

    @Column(name = "ORDEN", nullable = false)
    private Short vgn_Orden;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado;

    @JsonProperty("idEstadoTramite")
    public Integer getIdEstadoTramite() {
        return this.vgn_IdEstadoTramite;
    }

    public void setIdEstadoTramite(Integer pvn_IdEstadoTramite) {
        this.vgn_IdEstadoTramite = pvn_IdEstadoTramite;
    }

    @JsonProperty("idTipoTramite")
    public Integer getIdTipoTramite() {
        return this.vgn_IdTipoTramite;
    }

    public void setIdTipoTramite(Integer pvn_IdTipoTramite) {
        this.vgn_IdTipoTramite = pvn_IdTipoTramite;
    }

    @JsonProperty("nombre")
    public String getNombre() {
        return this.vgc_Nombre;
    }

    public void setNombre(String pvc_Nombre) {
        this.vgc_Nombre = pvc_Nombre;
    }

    @JsonProperty("esInicial")
    public Boolean getEsInicial() {
        return this.vgb_EsInicial;
    }

    public void setEsInicial(Boolean pvb_EsInicial) {
        this.vgb_EsInicial = pvb_EsInicial;
    }

    @JsonProperty("esFinal")
    public Boolean getEsFinal() {
        return this.vgb_EsFinal;
    }

    public void setEsFinal(Boolean pvb_EsFinal) {
        this.vgb_EsFinal = pvb_EsFinal;
    }

    @JsonProperty("categoriaEstado")
    public String getCategoriaEstado() {
        return this.vgc_CategoriaEstado;
    }

    public void setCategoriaEstado(String pvc_CategoriaEstado) {
        this.vgc_CategoriaEstado = pvc_CategoriaEstado;
    }

    @JsonProperty("orden")
    public Short getOrden() {
        return this.vgn_Orden;
    }

    public void setOrden(Short pvn_Orden) {
        this.vgn_Orden = pvn_Orden;
    }

    @JsonProperty("estado")
    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }
}
