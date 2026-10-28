package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Acceso a datos de SIMM_ESTADO_TRAMITE. No implementa reglas de negocio.
 *
 * Historial de cambios:
 *   Responsable: Katherine Susana Valverde Fallas
 *   Fecha: 15/09/2026
 *   Descripción: se agrega la consulta de los estados de un tipo de trámite, que necesita el
 *     cambio de estado del seguimiento (HU-032).
 */
@Repository
public class DalEstadoTramite {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene un estado por su identificador.
     *
     * @param pvn_IdEstadoTramite identificador del estado
     * @return estado encontrado o null
     */
    public EntEstadoTramite SIMM_ESTADO_TRAMITE_ObtenerRegistro(Integer pvn_IdEstadoTramite) {
        return pvn_IdEstadoTramite == null
                ? null
                : this.vgo_EntityManager.find(EntEstadoTramite.class, pvn_IdEstadoTramite);
    }

    public EntEstadoTramite SIMM_ESTADO_TRAMITE_ObtenerPorNombreCualquiera(
            Integer pvn_IdTipoTramite,
            String pvc_Nombre) {
        String vlc_Jpql = "SELECT e FROM EntEstadoTramite e "
                + "WHERE e.vgn_IdTipoTramite = :pvn_IdTipoTramite "
                + "AND UPPER(e.vgc_Nombre) = UPPER(:pvc_Nombre)";
        TypedQuery<EntEstadoTramite> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntEstadoTramite.class);
        vlo_Consulta.setParameter("pvn_IdTipoTramite", pvn_IdTipoTramite);
        vlo_Consulta.setParameter("pvc_Nombre", pvc_Nombre);
        try {
            return vlo_Consulta.getSingleResult();
        } catch (NoResultException vlo_Excepcion) {
            return null;
        }
    }

    public void SIMM_ESTADO_TRAMITE_InsertarRegistro(EntEstadoTramite pvo_EntEstadoTramite) {
        this.vgo_EntityManager.persist(pvo_EntEstadoTramite);
        this.vgo_EntityManager.flush();
    }

    public void SIMM_ESTADO_TRAMITE_ModificarRegistro(EntEstadoTramite pvo_EntEstadoTramite) {
        this.vgo_EntityManager.merge(pvo_EntEstadoTramite);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene un estado activo de un tipo de trámite por su nombre, sin distinguir
     *   mayúsculas. El catálogo no admite dos estados con el mismo nombre dentro de un tipo.
     *
     * @param pvn_IdTipoTramite identificador del tipo de trámite
     * @param pvc_Nombre nombre del estado
     * @param pvc_Estado estado que identifica las filas vigentes del catálogo
     * @return estado encontrado o null
     */
    public EntEstadoTramite SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
            Integer pvn_IdTipoTramite,
            String pvc_Nombre,
            String pvc_Estado) {
        String vlc_Jpql = "SELECT e FROM EntEstadoTramite e "
                + "WHERE e.vgn_IdTipoTramite = :pvn_IdTipoTramite "
                + "AND UPPER(e.vgc_Nombre) = UPPER(:pvc_Nombre) AND e.vgc_Estado = :pvc_Estado";
        TypedQuery<EntEstadoTramite> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntEstadoTramite.class);
        vlo_Consulta.setParameter("pvn_IdTipoTramite", pvn_IdTipoTramite);
        vlo_Consulta.setParameter("pvc_Nombre", pvc_Nombre);
        vlo_Consulta.setParameter("pvc_Estado", pvc_Estado);
        try {
            return vlo_Consulta.getSingleResult();
        } catch (NoResultException vlo_Excepcion) {
            return null;
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene los estados activos de un tipo de trámite, en el orden definido por el
     *   catálogo, para ofrecerlos como destino de un cambio de estado.
     *
     * @param pvn_IdTipoTramite identificador del tipo de trámite
     * @param pvc_Estado estado que identifica las filas vigentes del catálogo
     * @return estados disponibles del tipo de trámite
     */
    public List<EntEstadoTramite> SIMM_ESTADO_TRAMITE_ObtenerPorTipoTramite(
            Integer pvn_IdTipoTramite,
            String pvc_Estado) {
        String vlc_Jpql = "SELECT e FROM EntEstadoTramite e "
                + "WHERE e.vgn_IdTipoTramite = :pvn_IdTipoTramite AND e.vgc_Estado = :pvc_Estado "
                + "ORDER BY e.vgn_Orden, e.vgn_IdEstadoTramite";
        TypedQuery<EntEstadoTramite> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntEstadoTramite.class);
        vlo_Consulta.setParameter("pvn_IdTipoTramite", pvn_IdTipoTramite);
        vlo_Consulta.setParameter("pvc_Estado", pvc_Estado);
        return vlo_Consulta.getResultList();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene el estado inicial configurado para un tipo de trámite.
     *
     * @param pvn_IdTipoTramite identificador del tipo de trámite
     * @return estado marcado como inicial, o null si el catálogo no lo define
     */
    public EntEstadoTramite SIMM_ESTADO_TRAMITE_ObtenerEstadoInicial(Integer pvn_IdTipoTramite) {
        String vlc_Jpql = "SELECT e FROM EntEstadoTramite e "
                + "WHERE e.vgn_IdTipoTramite = :pvn_IdTipoTramite "
                + "AND e.vgb_EsInicial = :pvb_EsInicial AND e.vgc_Estado = :pvc_Estado "
                + "ORDER BY e.vgn_Orden";
        TypedQuery<EntEstadoTramite> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntEstadoTramite.class);
        vlo_Consulta.setParameter("pvn_IdTipoTramite", pvn_IdTipoTramite);
        vlo_Consulta.setParameter("pvb_EsInicial", Boolean.TRUE);
        vlo_Consulta.setParameter("pvc_Estado", "ACT"); // ACT = catálogo activo
        vlo_Consulta.setMaxResults(1);
        try {
            return vlo_Consulta.getSingleResult();
        } catch (NoResultException vlo_Excepcion) {
            return null;
        }
    }
}
