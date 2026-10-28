package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 04/10/2026
 * Descripción: Observaciones de un expediente separadas como las muestra la pantalla (HU-020):
 *   los borradores propios del usuario autenticado y el historial confirmado compartido.
 *
 * Historial de cambios:
 *   04/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
public class ModeloObservacionesExpedienteRespuesta {

    private List<ModeloObservacionRespuesta> vgo_LsBorradores = new ArrayList<>();
    private List<ModeloObservacionRespuesta> vgo_LsHistorial = new ArrayList<>();

    public ModeloObservacionesExpedienteRespuesta() {
    }

    /**
     * Borradores vigentes del usuario autenticado, del más antiguo al más reciente.
     */
    public List<ModeloObservacionRespuesta> getBorradores() {
        return this.vgo_LsBorradores;
    }

    public void setBorradores(List<ModeloObservacionRespuesta> pvo_LsBorradores) {
        this.vgo_LsBorradores = pvo_LsBorradores;
    }

    /**
     * Observaciones confirmadas del expediente, por fecha de confirmación ascendente.
     */
    public List<ModeloObservacionRespuesta> getHistorial() {
        return this.vgo_LsHistorial;
    }

    public void setHistorial(List<ModeloObservacionRespuesta> pvo_LsHistorial) {
        this.vgo_LsHistorial = pvo_LsHistorial;
    }
}
