package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntMoneda;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Acceso a datos de SIMM_MONEDA. No implementa reglas de negocio.
 */
@Repository
public class DalMoneda {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene una moneda por su identificador.
     *
     * @param pvn_IdMoneda identificador de la moneda
     * @return moneda encontrada o null
     */
    public EntMoneda SIMM_MONEDA_ObtenerRegistro(Integer pvn_IdMoneda) {
        return pvn_IdMoneda == null ? null : this.vgo_EntityManager.find(EntMoneda.class, pvn_IdMoneda);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene una moneda activa por su código ISO.
     *
     * @param pvc_CodigoIso código ISO de la moneda
     * @return moneda encontrada o null
     */
    public EntMoneda SIMM_MONEDA_ObtenerPorCodigoIso(String pvc_CodigoIso) {
        String vlc_Jpql = "SELECT m FROM EntMoneda m "
                + "WHERE m.vgc_CodigoIso = :pvc_CodigoIso AND m.vgc_Estado = :pvc_Estado";
        TypedQuery<EntMoneda> vlo_Consulta = this.vgo_EntityManager.createQuery(vlc_Jpql, EntMoneda.class);
        vlo_Consulta.setParameter("pvc_CodigoIso", pvc_CodigoIso);
        vlo_Consulta.setParameter("pvc_Estado", "ACT"); // ACT = catálogo activo
        try {
            return vlo_Consulta.getSingleResult();
        } catch (NoResultException vlo_Excepcion) {
            return null;
        }
    }

    public List<EntMoneda> SIMM_MONEDA_ObtenerActivas() {
        String vlc_Jpql = "SELECT m FROM EntMoneda m WHERE m.vgc_Estado = :pvc_Estado "
                + "ORDER BY m.vgc_CodigoIso";
        TypedQuery<EntMoneda> vlo_Consulta = this.vgo_EntityManager.createQuery(vlc_Jpql, EntMoneda.class);
        vlo_Consulta.setParameter("pvc_Estado", "ACT");
        return vlo_Consulta.getResultList();
    }
}
