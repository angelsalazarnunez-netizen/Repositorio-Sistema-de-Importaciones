package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntPais;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Acceso a datos de SIMM_PAIS. No implementa reglas de negocio.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: se agrega la lista de países activos para la exportación temporal (HU-026).
 */
@Repository
public class DalPais {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene un país por su identificador.
     *
     * @param pvn_IdPais identificador del país
     * @return país encontrado o null
     */
    public EntPais SIMM_PAIS_ObtenerRegistro(Integer pvn_IdPais) {
        return pvn_IdPais == null ? null : this.vgo_EntityManager.find(EntPais.class, pvn_IdPais);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene un país activo por su nombre, sin distinguir mayúsculas.
     *
     * @param pvc_Nombre nombre del país
     * @return país encontrado o null
     */
    public EntPais SIMM_PAIS_ObtenerPorNombre(String pvc_Nombre) {
        String vlc_Jpql = "SELECT p FROM EntPais p "
                + "WHERE UPPER(p.vgc_Nombre) = UPPER(:pvc_Nombre) AND p.vgc_Estado = :pvc_Estado";
        TypedQuery<EntPais> vlo_Consulta = this.vgo_EntityManager.createQuery(vlc_Jpql, EntPais.class);
        vlo_Consulta.setParameter("pvc_Nombre", pvc_Nombre);
        vlo_Consulta.setParameter("pvc_Estado", "ACT"); // ACT = catálogo activo
        try {
            return vlo_Consulta.getSingleResult();
        } catch (NoResultException vlo_Excepcion) {
            return null;
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene los países activos ordenados por nombre, para elegir el país de destino
     *   de una exportación temporal (HU-026).
     *
     * @return países activos
     */
    public List<EntPais> SIMM_PAIS_ObtenerActivos() {
        return this.vgo_EntityManager.createQuery(
                        "SELECT p FROM EntPais p WHERE p.vgc_Estado = :pvc_Estado ORDER BY p.vgc_Nombre",
                        EntPais.class)
                .setParameter("pvc_Estado", "ACT") // ACT = catálogo activo
                .getResultList();
    }
}
