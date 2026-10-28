package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Acceso a datos de SIMM_TIPO_TRAMITE. No implementa reglas de negocio.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 16/09/2026
 *   Descripción: se agrega la consulta de los tipos de trámite activos para el filtro de tipo de
 *     contratación de la búsqueda de trámites (HU-034).
 */
@Repository
public class DalTipoTramite {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene un tipo de trámite activo por su código.
     *
     * @param pvc_Codigo código del tipo de trámite
     * @return tipo de trámite encontrado o null
     */
    public EntTipoTramite SIMM_TIPO_TRAMITE_ObtenerPorCodigo(String pvc_Codigo) {
        String vlc_Jpql = "SELECT t FROM EntTipoTramite t "
                + "WHERE t.vgc_Codigo = :pvc_Codigo AND t.vgc_Estado = :pvc_Estado";
        TypedQuery<EntTipoTramite> vlo_Consulta = this.vgo_EntityManager.createQuery(vlc_Jpql, EntTipoTramite.class);
        vlo_Consulta.setParameter("pvc_Codigo", pvc_Codigo);
        vlo_Consulta.setParameter("pvc_Estado", "ACT"); // ACT = catálogo activo
        try {
            return vlo_Consulta.getSingleResult();
        } catch (NoResultException vlo_Excepcion) {
            return null;
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Obtiene los tipos de trámite vigentes del catálogo.
     *
     * @param pvc_Estado estado que identifica las filas vigentes del catálogo
     * @return tipos de trámite ordenados por nombre
     */
    public List<EntTipoTramite> SIMM_TIPO_TRAMITE_ObtenerLista(String pvc_Estado) {
        String vlc_Jpql = "SELECT t FROM EntTipoTramite t WHERE t.vgc_Estado = :pvc_Estado ORDER BY t.vgc_Nombre";
        TypedQuery<EntTipoTramite> vlo_Consulta = this.vgo_EntityManager.createQuery(vlc_Jpql, EntTipoTramite.class);
        vlo_Consulta.setParameter("pvc_Estado", pvc_Estado);
        return vlo_Consulta.getResultList();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 23/09/2026
     * Descripción: Obtiene el tipo de trámite activo cuya sigla encabeza la referencia
     *   digitada. Así el sistema reconoce a qué tipo pertenece una referencia nueva.
     *
     * @param pvc_ReferenciaInterna referencia interna digitada, en mayúsculas
     * @return tipo de trámite cuya sigla calza con la referencia o null
     */
    public EntTipoTramite SIMM_TIPO_TRAMITE_ObtenerPorReferencia(String pvc_ReferenciaInterna) {
        if (pvc_ReferenciaInterna == null || pvc_ReferenciaInterna.isEmpty()) {
            return null;
        }
        String vlc_Jpql = "SELECT t FROM EntTipoTramite t WHERE t.vgc_Estado = :pvc_Estado "
                + "ORDER BY LENGTH(t.vgc_PrefijoReferencia) DESC";
        TypedQuery<EntTipoTramite> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntTipoTramite.class);
        vlo_Consulta.setParameter("pvc_Estado", "ACT"); // ACT = catálogo activo
        for (EntTipoTramite vlo_EnTipo : vlo_Consulta.getResultList()) {
            if (pvc_ReferenciaInterna.startsWith(vlo_EnTipo.getPrefijoReferencia())) {
                return vlo_EnTipo;
            }
        }
        return null;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 23/09/2026
     * Descripción: Obtiene un tipo de trámite por su identificador.
     *
     * @param pvn_IdTipoTramite identificador del tipo de trámite
     * @return tipo de trámite encontrado o null
     */
    public EntTipoTramite SIMM_TIPO_TRAMITE_ObtenerRegistro(Integer pvn_IdTipoTramite) {
        return pvn_IdTipoTramite == null
                ? null
                : this.vgo_EntityManager.find(EntTipoTramite.class, pvn_IdTipoTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 26/09/2026
     * Descripción: Obtiene el catálogo vigente, para que las pantallas no tengan que repetir
     *   los códigos ni los nombres.
     *
     * @return registros activos del catálogo, ordenados por nombre
     */
    public java.util.List<EntTipoTramite> SIMM_TIPO_TRAMITE_ObtenerLista() {
        String vlc_Jpql = "SELECT c FROM EntTipoTramite c WHERE c.vgc_Estado = :pvc_Estado "
                + "ORDER BY c.vgc_Nombre";
        TypedQuery<EntTipoTramite> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntTipoTramite.class);
        vlo_Consulta.setParameter("pvc_Estado", "ACT"); // ACT = catálogo activo
        return vlo_Consulta.getResultList();
    }
}
