package cr.ac.ucr.sim.entidadnegocio.tramites;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 15/09/2026
 * Descripción: Datos de un cambio de estado de un expediente (HU-032). No representa una tabla:
 *   transporta la entrada del caso de uso hacia la lógica de negocio. El estado destino se puede
 *   indicar por su nombre o por su identificador, porque la pantalla de seguimiento trabaja con
 *   los nombres que muestra la lista desplegable.
 */
public class EntCambioEstado {

    private String vgc_NuevoEstado;
    private Integer vgn_IdEstadoNuevo;
    private String vgc_Justificacion;
    private Integer vgn_IdUsuarioResponsable;

    public String getNuevoEstado() {
        return this.vgc_NuevoEstado;
    }

    public void setNuevoEstado(String pvc_NuevoEstado) {
        this.vgc_NuevoEstado = pvc_NuevoEstado;
    }

    public Integer getIdEstadoNuevo() {
        return this.vgn_IdEstadoNuevo;
    }

    public void setIdEstadoNuevo(Integer pvn_IdEstadoNuevo) {
        this.vgn_IdEstadoNuevo = pvn_IdEstadoNuevo;
    }

    public String getJustificacion() {
        return this.vgc_Justificacion;
    }

    public void setJustificacion(String pvc_Justificacion) {
        this.vgc_Justificacion = pvc_Justificacion;
    }

    public Integer getIdUsuarioResponsable() {
        return this.vgn_IdUsuarioResponsable;
    }

    public void setIdUsuarioResponsable(Integer pvn_IdUsuarioResponsable) {
        this.vgn_IdUsuarioResponsable = pvn_IdUsuarioResponsable;
    }
}
