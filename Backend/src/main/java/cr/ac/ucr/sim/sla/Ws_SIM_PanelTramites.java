package cr.ac.ucr.sim.sla;

import cr.ac.ucr.sim.seguridad.ReglasAcceso;
import cr.ac.ucr.sim.seguridad.SrvAutorizacion;
import org.springframework.security.access.prepost.PreAuthorize;
import cr.ac.ucr.sim.logicanegocio.tramites.BllPanelTramites;
import cr.ac.ucr.sim.utilerias.modelo.tramites.ModeloPanelTramitesRespuesta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Servicio REST del panel de trámites activos (HU-030). Es de solo consulta: la
 *   gestión de cada trámite se hace en el expediente de su tipo.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 *   19/10/2026 Equipo de Desarrollo SIM: restricciones por rol (HU-003). Entra quien pueda
 *     consultar al menos un tipo de trámite, y el panel muestra solo esos tipos; con
 *     REPORTE_CONSULTAR se muestran todos.
 */
@RestController
@RequestMapping("/api/v1/tramites/activos")
public class Ws_SIM_PanelTramites {

    private final BllPanelTramites vgo_BllPanelTramites;
    private final SrvAutorizacion vgo_SrvAutorizacion;

    public Ws_SIM_PanelTramites(BllPanelTramites pvo_BllPanelTramites, SrvAutorizacion pvo_SrvAutorizacion) {
        this.vgo_BllPanelTramites = pvo_BllPanelTramites;
        this.vgo_SrvAutorizacion = pvo_SrvAutorizacion;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene el panel de trámites: los indicadores de la vista operativa y la lista
     *   del alcance indicado.
     *
     * @param pvc_Alcance ACTIVOS (por defecto) o HISTORICO
     * @return panel de trámites
     */
    @PreAuthorize(ReglasAcceso.CONSULTA_TRAMITES)
    @GetMapping
    public ResponseEntity<ModeloPanelTramitesRespuesta> SIMT_TRAMITE_ObtenerPanelActivos(
            @RequestParam(name = "alcance", required = false) String pvc_Alcance) {
        return ResponseEntity.ok(this.vgo_BllPanelTramites.ObtenerPanel(
                pvc_Alcance, this.vgo_SrvAutorizacion.CodigosTipoTramiteConsultables()));
    }
}
