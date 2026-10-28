package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMovimientoEstado;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Acceso a datos de SIMH_ESTADO_TRAMITE. No implementa reglas de negocio.
 */
@Repository
public class DalHistorialEstado {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Inserta un movimiento del historial de estados del expediente.
     *
     * @param pvo_EntHistorialEstado entidad a persistir
     */
    public void SIMH_ESTADO_TRAMITE_InsertarRegistro(EntHistorialEstado pvo_EntHistorialEstado) {
        this.vgo_EntityManager.persist(pvo_EntHistorialEstado);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene el historial de estados de un expediente, del movimiento más antiguo
     *   al más reciente, con los nombres de estado y de usuario ya resueltos.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return movimientos del expediente
     */
    public List<EntMovimientoEstado> SIMH_ESTADO_TRAMITE_ObtenerPorTramite(Integer pvn_IdTramite) {
        String vlc_Jpql = "SELECT NEW cr.ac.ucr.sim.entidadnegocio.tramites.EntMovimientoEstado("
                + "ea.vgc_Nombre, en.vgc_Nombre, u.vgc_NombreCompleto, "
                + "h.vgc_Justificacion, h.vgd_FechaHora) "
                + "FROM EntHistorialEstado h "
                + "LEFT JOIN EntEstadoTramite ea ON ea.vgn_IdEstadoTramite = h.vgn_IdEstadoAnterior "
                + "JOIN EntEstadoTramite en ON en.vgn_IdEstadoTramite = h.vgn_IdEstadoNuevo "
                + "JOIN EntUsuario u ON u.vgn_IdUsuario = h.vgn_IdUsuario "
                + "WHERE h.vgn_IdTramite = :pvn_IdTramite "
                + "ORDER BY h.vgd_FechaHora, h.vgn_IdHistorial";
        TypedQuery<EntMovimientoEstado> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntMovimientoEstado.class);
        vlo_Consulta.setParameter("pvn_IdTramite", pvn_IdTramite);
        return vlo_Consulta.getResultList();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene el movimiento más reciente con el que el expediente llegó a un estado.
     *   La aprobación de exoneraciones (HU-022) lo usa para saber quién envió el expediente a
     *   aprobación y cuándo.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvn_IdEstadoNuevo estado al que llegó el expediente
     * @return movimiento encontrado o null
     */
    public EntHistorialEstado SIMH_ESTADO_TRAMITE_ObtenerUltimoIngreso(
            Integer pvn_IdTramite,
            Integer pvn_IdEstadoNuevo) {
        TypedQuery<EntHistorialEstado> vlo_Consulta = this.vgo_EntityManager.createQuery(
                "SELECT h FROM EntHistorialEstado h "
                        + "WHERE h.vgn_IdTramite = :pvn_IdTramite AND h.vgn_IdEstadoNuevo = :pvn_IdEstadoNuevo "
                        + "ORDER BY h.vgd_FechaHora DESC, h.vgn_IdHistorial DESC",
                EntHistorialEstado.class);
        vlo_Consulta.setParameter("pvn_IdTramite", pvn_IdTramite);
        vlo_Consulta.setParameter("pvn_IdEstadoNuevo", pvn_IdEstadoNuevo);
        vlo_Consulta.setMaxResults(1);
        List<EntHistorialEstado> vlo_Lista = vlo_Consulta.getResultList();
        return vlo_Lista.isEmpty() ? null : vlo_Lista.get(0);
    }
}
