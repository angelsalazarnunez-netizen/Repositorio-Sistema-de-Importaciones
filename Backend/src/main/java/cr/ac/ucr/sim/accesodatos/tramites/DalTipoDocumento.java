package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoDocumento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Acceso a datos de SIMM_TIPO_DOCUMENTO. No implementa reglas de negocio.
 */
@Repository
public class DalTipoDocumento {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene un tipo de documento activo por su código.
     *
     * @param pvc_Codigo código del tipo de documento
     * @return tipo de documento encontrado o null
     */
    public EntTipoDocumento SIMM_TIPO_DOCUMENTO_ObtenerPorCodigo(String pvc_Codigo) {
        String vlc_Jpql = "SELECT d FROM EntTipoDocumento d "
                + "WHERE d.vgc_Codigo = :pvc_Codigo AND d.vgc_Estado = :pvc_Estado";
        TypedQuery<EntTipoDocumento> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntTipoDocumento.class);
        vlo_Consulta.setParameter("pvc_Codigo", pvc_Codigo);
        vlo_Consulta.setParameter("pvc_Estado", "ACT"); // ACT = catálogo activo
        try {
            return vlo_Consulta.getSingleResult();
        } catch (NoResultException vlo_Excepcion) {
            return null;
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 25/09/2026
     * Descripción: Obtiene un tipo de documento por su identificador, para describir la
     *   clasificación anterior de un documento en la bitácora.
     *
     * @param pvn_IdTipoDocumento identificador del tipo de documento
     * @return tipo de documento encontrado o null
     */
    public EntTipoDocumento SIMM_TIPO_DOCUMENTO_ObtenerRegistro(Integer pvn_IdTipoDocumento) {
        return pvn_IdTipoDocumento == null
                ? null
                : this.vgo_EntityManager.find(EntTipoDocumento.class, pvn_IdTipoDocumento);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 26/09/2026
     * Descripción: Obtiene el catálogo vigente, para que las pantallas no tengan que repetir
     *   los códigos ni los nombres.
     *
     * @return registros activos del catálogo, ordenados por nombre
     */
    public java.util.List<EntTipoDocumento> SIMM_TIPO_DOCUMENTO_ObtenerLista() {
        String vlc_Jpql = "SELECT c FROM EntTipoDocumento c WHERE c.vgc_Estado = :pvc_Estado "
                + "ORDER BY c.vgc_Nombre";
        TypedQuery<EntTipoDocumento> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntTipoDocumento.class);
        vlo_Consulta.setParameter("pvc_Estado", "ACT"); // ACT = catálogo activo
        return vlo_Consulta.getResultList();
    }
}
