package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntBitacora;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMovimientoBitacora;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 15/09/2026
 * Descripción: Acceso a datos de SIML_BITACORA. No implementa reglas de negocio.
 */
@Repository
public class DalBitacora {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Inserta una acción en la bitácora de un expediente.
     *
     * @param pvo_EntBitacora entidad a persistir
     */
    public void SIML_BITACORA_InsertarRegistro(EntBitacora pvo_EntBitacora) {
        this.vgo_EntityManager.persist(pvo_EntBitacora);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Actualiza un registro de la bitácora.
     *
     * @param pvo_EntBitacora entidad a actualizar
     */
    public void SIML_BITACORA_ModificarRegistro(EntBitacora pvo_EntBitacora) {
        this.vgo_EntityManager.merge(pvo_EntBitacora);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene un registro de la bitácora por su identificador.
     *
     * @param pvn_IdBitacora identificador del registro
     * @return registro encontrado o null
     */
    public EntBitacora SIML_BITACORA_ObtenerRegistro(Integer pvn_IdBitacora) {
        return pvn_IdBitacora == null
                ? null
                : this.vgo_EntityManager.find(EntBitacora.class, pvn_IdBitacora);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene los registros activos de la bitácora de un expediente, del más antiguo
     *   al más reciente, con el usuario y la descripción de la acción ya resueltos.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvc_Estado estado que identifica los registros vigentes
     * @return registros de la bitácora
     */
    public List<EntMovimientoBitacora> SIML_BITACORA_ObtenerPorTramite(Integer pvn_IdTramite, String pvc_Estado) {
        String vlc_Jpql =
                "SELECT NEW cr.ac.ucr.sim.entidadnegocio.tramites.EntMovimientoBitacora("
                        + "b.vgn_IdBitacora, "
                        + "b.vgc_Accion, "
                        + "a.vgc_Descripcion, "
                        + "u.vgc_NombreCompleto, "
                        + "b.vgc_DatosAnteriores, "
                        + "b.vgc_DatosNuevos, "
                        + "b.vgd_FechaHora) "
                        + "FROM EntBitacora b "
                        + "JOIN EntAccionBitacora a "
                        + "ON a.vgc_Accion = b.vgc_Accion "
                        + "JOIN EntUsuario u "
                        + "ON u.vgn_IdUsuario = b.vgn_IdUsuario "
                        + "WHERE b.vgn_IdTramite = :pvn_IdTramite "
                        + "AND b.vgc_Estado = :pvc_Estado "
                        + "ORDER BY b.vgd_FechaHora, b.vgn_IdBitacora";

        TypedQuery<EntMovimientoBitacora> vlo_Consulta =
                this.vgo_EntityManager.createQuery(
                        vlc_Jpql,
                        EntMovimientoBitacora.class
                );

        vlo_Consulta.setParameter(
                "pvn_IdTramite",
                pvn_IdTramite
        );

        vlo_Consulta.setParameter(
                "pvc_Estado",
                pvc_Estado
        );

        return vlo_Consulta.getResultList();
    }
}
