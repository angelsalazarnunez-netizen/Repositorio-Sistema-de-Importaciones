package cr.ac.ucr.sim.accesodatos.tramites.exoneraciones;

import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntDecisionExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntNotificacionExoneracion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Acceso a datos de SIMT_DECISION_EXONERACION y SIMT_NOTIFICACION_EXONERACION
 *   (HU-022). No implementa reglas de negocio ni ofrece eliminación: las decisiones forman el
 *   historial del expediente y la notificación solo actualiza su estado de envío.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
@Repository
public class DalDecisionExoneracion {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Inserta una decisión sobre una solicitud de exoneración.
     *
     * @param pvo_EntDecision entidad a persistir
     */
    public void SIMT_DECISION_EXONERACION_InsertarRegistro(EntDecisionExoneracion pvo_EntDecision) {
        this.vgo_EntityManager.persist(pvo_EntDecision);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene las decisiones activas de un expediente, de la más reciente a la más
     *   antigua.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvc_Estado estado de registro de las decisiones
     * @return decisiones del expediente
     */
    public List<EntDecisionExoneracion> SIMT_DECISION_EXONERACION_ObtenerPorTramite(
            Integer pvn_IdTramite,
            String pvc_Estado) {
        String vlc_Jpql = "SELECT d FROM EntDecisionExoneracion d "
                + "WHERE d.vgn_IdTramite = :pvn_IdTramite AND d.vgc_Estado = :pvc_Estado "
                + "ORDER BY d.vgd_FechaHora DESC, d.vgn_IdDecision DESC";
        TypedQuery<EntDecisionExoneracion> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntDecisionExoneracion.class);
        vlo_Consulta.setParameter("pvn_IdTramite", pvn_IdTramite);
        vlo_Consulta.setParameter("pvc_Estado", pvc_Estado);
        return vlo_Consulta.getResultList();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Inserta la notificación de una decisión.
     *
     * @param pvo_EntNotificacion entidad a persistir
     */
    public void SIMT_NOTIFICACION_EXONERACION_InsertarRegistro(EntNotificacionExoneracion pvo_EntNotificacion) {
        this.vgo_EntityManager.persist(pvo_EntNotificacion);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Actualiza el estado de envío de una notificación.
     *
     * @param pvo_EntNotificacion entidad a actualizar
     */
    public void SIMT_NOTIFICACION_EXONERACION_ModificarRegistro(EntNotificacionExoneracion pvo_EntNotificacion) {
        this.vgo_EntityManager.merge(pvo_EntNotificacion);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene la notificación activa de una decisión.
     *
     * @param pvn_IdDecision identificador de la decisión
     * @param pvc_Estado estado de registro de la notificación
     * @return notificación encontrada o null
     */
    public EntNotificacionExoneracion SIMT_NOTIFICACION_EXONERACION_ObtenerPorDecision(
            Integer pvn_IdDecision,
            String pvc_Estado) {
        String vlc_Jpql = "SELECT n FROM EntNotificacionExoneracion n "
                + "WHERE n.vgn_IdDecision = :pvn_IdDecision AND n.vgc_Estado = :pvc_Estado "
                + "ORDER BY n.vgn_IdNotificacion DESC";
        TypedQuery<EntNotificacionExoneracion> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntNotificacionExoneracion.class);
        vlo_Consulta.setParameter("pvn_IdDecision", pvn_IdDecision);
        vlo_Consulta.setParameter("pvc_Estado", pvc_Estado);
        vlo_Consulta.setMaxResults(1);
        List<EntNotificacionExoneracion> vlo_Lista = vlo_Consulta.getResultList();
        return vlo_Lista.isEmpty() ? null : vlo_Lista.get(0);
    }
}
