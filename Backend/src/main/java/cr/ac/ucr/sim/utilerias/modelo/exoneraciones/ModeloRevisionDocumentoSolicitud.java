package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 03/10/2026
 * Descripcion: datos recibidos para registrar
 * la revision de un documento de exoneracion.
 */
public class ModeloRevisionDocumentoSolicitud {

    private String vgc_EstadoRevision;
    private String vgc_Observacion;

    public ModeloRevisionDocumentoSolicitud() {
    }

    public String getEstadoRevision() {
        return vgc_EstadoRevision;
    }

    public void setEstadoRevision(
            String vlc_EstadoRevision) {

        this.vgc_EstadoRevision =
                vlc_EstadoRevision;
    }

    public String getObservacion() {
        return vgc_Observacion;
    }

    public void setObservacion(
            String vlc_Observacion) {

        this.vgc_Observacion =
                vlc_Observacion;
    }
}