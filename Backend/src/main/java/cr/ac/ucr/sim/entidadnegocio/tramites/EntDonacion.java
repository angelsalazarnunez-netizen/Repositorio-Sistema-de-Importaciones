package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 10/09/2026
 * Descripción: Entidad de negocio de la tabla SIMT_DONACION. Agrega los datos propios de la
 *   donación y comparte la llave primaria con SIMT_TRAMITE.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: la referencia FUNDEVI pasa a REFERENCIA_FUNDACION_UCR, porque FUNDEVI es el
 *     nombre anterior de la Fundación UCR.
 */
@Entity
@Table(name = "SIMT_DONACION")
public class EntDonacion {

    @Id
    @Column(name = "ID_TRAMITE")
    private Integer vgn_IdTramite;

    @Column(name = "ID_DOCUMENTO_ACEPTACION", nullable = false)
    private Integer vgn_IdDocumentoAceptacion;

    @Column(name = "TIPO_DONACION", nullable = false, length = 20)
    private String vgc_TipoDonacion;

    @Column(name = "NOMBRE_DONANTE", nullable = false, length = 150)
    private String vgc_NombreDonante;

    @Column(name = "REFERENCIA_FUNDACION_UCR", length = 80)
    private String vgc_ReferenciaFundacionUcr;

    @JsonProperty("idTramite")
    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    @JsonProperty("idDocumentoAceptacion")
    public Integer getIdDocumentoAceptacion() {
        return this.vgn_IdDocumentoAceptacion;
    }

    public void setIdDocumentoAceptacion(Integer pvn_IdDocumentoAceptacion) {
        this.vgn_IdDocumentoAceptacion = pvn_IdDocumentoAceptacion;
    }

    @JsonProperty("tipoDonacion")
    public String getTipoDonacion() {
        return this.vgc_TipoDonacion;
    }

    public void setTipoDonacion(String pvc_TipoDonacion) {
        this.vgc_TipoDonacion = pvc_TipoDonacion;
    }

    @JsonProperty("nombreDonante")
    public String getNombreDonante() {
        return this.vgc_NombreDonante;
    }

    public void setNombreDonante(String pvc_NombreDonante) {
        this.vgc_NombreDonante = pvc_NombreDonante;
    }

    @JsonProperty("referenciaFundacionUcr")
    public String getReferenciaFundacionUcr() {
        return this.vgc_ReferenciaFundacionUcr;
    }

    public void setReferenciaFundacionUcr(String pvc_ReferenciaFundacionUcr) {
        this.vgc_ReferenciaFundacionUcr = pvc_ReferenciaFundacionUcr;
    }
}
