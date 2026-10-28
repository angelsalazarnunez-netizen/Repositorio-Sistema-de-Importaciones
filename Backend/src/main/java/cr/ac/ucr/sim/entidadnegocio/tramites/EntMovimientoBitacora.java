package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 15/09/2026
 * Descripción: Registro de la bitácora tal como se muestra y se imprime en el expediente, con el
 *   nombre del usuario y la descripción de la acción ya resueltos.
 */
public class EntMovimientoBitacora {

    private final Integer vgn_IdBitacora;
    private final String vgc_Accion;
    private final String vgc_DescripcionAccion;
    private final String vgc_Usuario;
    private final String vgc_DatosAnteriores;
    private final String vgc_DatosNuevos;
    private final LocalDateTime vgd_FechaHora;

    public EntMovimientoBitacora(
            Integer pvn_IdBitacora,
            String pvc_Accion,
            String pvc_DescripcionAccion,
            String pvc_Usuario,
            String pvc_DatosAnteriores,
            String pvc_DatosNuevos,
            LocalDateTime pvd_FechaHora) {
        this.vgn_IdBitacora = pvn_IdBitacora;
        this.vgc_Accion = pvc_Accion;
        this.vgc_DescripcionAccion = pvc_DescripcionAccion;
        this.vgc_Usuario = pvc_Usuario;
        this.vgc_DatosAnteriores = pvc_DatosAnteriores;
        this.vgc_DatosNuevos = pvc_DatosNuevos;
        this.vgd_FechaHora = pvd_FechaHora;
    }

    @JsonProperty("idBitacora")
    public Integer getIdBitacora() {
        return this.vgn_IdBitacora;
    }

    @JsonProperty("accion")
    public String getAccion() {
        return this.vgc_Accion;
    }

    @JsonProperty("descripcionAccion")
    public String getDescripcionAccion() {
        return this.vgc_DescripcionAccion;
    }

    @JsonProperty("usuario")
    public String getUsuario() {
        return this.vgc_Usuario;
    }

    @JsonProperty("datosAnteriores")
    public String getDatosAnteriores() {
        return this.vgc_DatosAnteriores;
    }

    @JsonProperty("datosNuevos")
    public String getDatosNuevos() {
        return this.vgc_DatosNuevos;
    }

    @JsonProperty("fechaHora")
    public LocalDateTime getFechaHora() {
        return this.vgd_FechaHora;
    }
}
