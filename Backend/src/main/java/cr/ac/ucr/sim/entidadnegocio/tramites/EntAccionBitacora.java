package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 15/09/2026
 * Descripción: Entidad de negocio de la tabla SIMC_ACCION_BITACORA, que cataloga las acciones que
 *   se registran en la bitácora de un expediente.
 */
@Entity
@Table(name = "SIMC_ACCION_BITACORA")
public class EntAccionBitacora {

    @Id
    @Column(name = "ACCION", length = 30)
    private String vgc_Accion;

    @Column(name = "DESCRIPCION", nullable = false, length = 100)
    private String vgc_Descripcion;

    @JsonProperty("accion")
    public String getAccion() {
        return this.vgc_Accion;
    }

    public void setAccion(String pvc_Accion) {
        this.vgc_Accion = pvc_Accion;
    }

    @JsonProperty("descripcion")
    public String getDescripcion() {
        return this.vgc_Descripcion;
    }

    public void setDescripcion(String pvc_Descripcion) {
        this.vgc_Descripcion = pvc_Descripcion;
    }
}
