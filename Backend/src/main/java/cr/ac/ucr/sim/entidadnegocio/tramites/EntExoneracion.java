package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 15/09/2026
 * Descripción: Entidad de negocio de la tabla SIMT_EXONERACION. Agrega los datos propios de la
 *   solicitud de exoneración y comparte la llave primaria con SIMT_TRAMITE.
 */
@Entity
@Table(name = "SIMT_EXONERACION")
public class EntExoneracion {

    @Id
    @Column(name = "ID_TRAMITE")
    private Integer vgn_IdTramite;

    @Column(name = "TIPO_EXONERACION", nullable = false, length = 20)
    private String vgc_TipoExoneracion;

    @Column(name = "NUMERO_CONTRATACION", nullable = false, length = 45)
    private String vgc_NumeroContratacion;

    @Column(name = "ORDEN_COMPRA_GECO", length = 45)
    private String vgc_OrdenCompraGeco;

    @Column(name = "CONTRATO_SICOP", length = 45) // opcional: hay exoneraciones que no van por SICOP
    private String vgc_ContratoSicop;

    @Column(name = "NUMERO_EXONET", length = 45)
    private String vgc_NumeroExonet;

    @Column(name = "NUMERO_AUTORIZACION_EXONET", length = 45)
    private String vgc_NumeroAutorizacionExonet;

    @Column(name = "RESOLUCION", length = 45)
    private String vgc_Resolucion;

    @Column(name = "FECHA_RESOLUCION")
    private LocalDate vgd_FechaResolucion;

    @JsonProperty("idTramite")
    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    @JsonProperty("tipoExoneracion")
    public String getTipoExoneracion() {
        return this.vgc_TipoExoneracion;
    }

    public void setTipoExoneracion(String pvc_TipoExoneracion) {
        this.vgc_TipoExoneracion = pvc_TipoExoneracion;
    }

    @JsonProperty("numeroContratacion")
    public String getNumeroContratacion() {
        return this.vgc_NumeroContratacion;
    }

    public void setNumeroContratacion(String pvc_NumeroContratacion) {
        this.vgc_NumeroContratacion = pvc_NumeroContratacion;
    }

    @JsonProperty("ordenCompraGeco")
    public String getOrdenCompraGeco() {
        return this.vgc_OrdenCompraGeco;
    }

    public void setOrdenCompraGeco(String pvc_OrdenCompraGeco) {
        this.vgc_OrdenCompraGeco = pvc_OrdenCompraGeco;
    }

    @JsonProperty("contratoSicop")
    public String getContratoSicop() {
        return this.vgc_ContratoSicop;
    }

    public void setContratoSicop(String pvc_ContratoSicop) {
        this.vgc_ContratoSicop = pvc_ContratoSicop;
    }

    @JsonProperty("numeroExonet")
    public String getNumeroExonet() {
        return this.vgc_NumeroExonet;
    }

    public void setNumeroExonet(String pvc_NumeroExonet) {
        this.vgc_NumeroExonet = pvc_NumeroExonet;
    }

    public String getNumeroAutorizacionExonet() {
        return this.vgc_NumeroAutorizacionExonet;
    }

    public void setNumeroAutorizacionExonet(String pvc_NumeroAutorizacionExonet) {
        this.vgc_NumeroAutorizacionExonet = pvc_NumeroAutorizacionExonet;
    }

    @JsonProperty("resolucion")
    public String getResolucion() {
        return this.vgc_Resolucion;
    }

    public void setResolucion(String pvc_Resolucion) {
        this.vgc_Resolucion = pvc_Resolucion;
    }

    @JsonProperty("fechaResolucion")
    public LocalDate getFechaResolucion() {
        return this.vgd_FechaResolucion;
    }

    public void setFechaResolucion(LocalDate pvd_FechaResolucion) {
        this.vgd_FechaResolucion = pvd_FechaResolucion;
    }
}
