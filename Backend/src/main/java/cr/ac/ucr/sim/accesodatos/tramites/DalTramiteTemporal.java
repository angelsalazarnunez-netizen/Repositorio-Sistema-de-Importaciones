package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntAgenteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntFilaTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialProrrogaTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntVencimientoTemporal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Autor: Camila Pastor Castro
 * Fecha: 03/10/2026
 * Descripción: Acceso a datos de los trámites temporales y de sus agentes responsables.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: el trámite temporal se consulta junto con su expediente de SIMT_TRAMITE (HU-025).
 *     Se agregan la lista por tipo de trámite, las unidades solicitantes registradas y los agentes
 *     de un expediente. La consulta de vencimientos devuelve los expedientes abiertos que vencen
 *     dentro del periodo de alerta, y no solo los que vencen en una fecha exacta. Las consultas de
 *     las alertas pasan a DalAlertaTemporal.
 */
@Repository
public class DalTramiteTemporal {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    public void SIMT_HISTORIAL_PRORROGA_Insertar(EntHistorialProrrogaTemporal pvo_Historial) {
        this.vgo_EntityManager.persist(pvo_Historial);
        this.vgo_EntityManager.flush();
    }

    public List<EntHistorialProrrogaTemporal> SIMT_HISTORIAL_PRORROGA_ObtenerPorTramite(Integer pvn_IdTramite) {
        return this.vgo_EntityManager.createQuery(
                        "SELECT h FROM EntHistorialProrrogaTemporal h "
                                + "WHERE h.vgn_IdTramite = :pvn_IdTramite "
                                + "ORDER BY h.vgd_FechaRegistro, h.vgn_IdHistorialProrroga",
                        EntHistorialProrrogaTemporal.class)
                .setParameter("pvn_IdTramite", pvn_IdTramite)
                .getResultList();
    }

    public long SIMT_HISTORIAL_PRORROGA_ContarPorTramite(Integer pvn_IdTramite) {
        return this.vgo_EntityManager.createQuery(
                        "SELECT COUNT(h) FROM EntHistorialProrrogaTemporal h "
                                + "WHERE h.vgn_IdTramite = :pvn_IdTramite",
                        Long.class)
                .setParameter("pvn_IdTramite", pvn_IdTramite)
                .getSingleResult();
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Inserta los datos propios de un trámite temporal.
     *
     * @param pvo_Tramite datos del trámite temporal, con la llave de su expediente
     */
    public void SIMT_TRAMITE_TEMPORAL_InsertarRegistro(EntTramiteTemporal pvo_Tramite) {
        this.vgo_EntityManager.persist(pvo_Tramite);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Actualiza los datos propios de un trámite temporal.
     *
     * @param pvo_Tramite datos del trámite temporal
     */
    public void SIMT_TRAMITE_TEMPORAL_ModificarRegistro(EntTramiteTemporal pvo_Tramite) {
        this.vgo_EntityManager.merge(pvo_Tramite);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene los datos propios del trámite temporal de un expediente.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return datos del trámite temporal o null
     */
    public EntTramiteTemporal SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(Integer pvn_IdTramite) {
        return pvn_IdTramite == null
                ? null
                : this.vgo_EntityManager.find(EntTramiteTemporal.class, pvn_IdTramite);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene los expedientes temporales de un tipo de trámite con su estado y su
     *   moneda, del más reciente al más antiguo.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: incluye el nombre del país de destino de la exportación temporal (HU-026); la
     *     importación no lo registra, por eso el país se une de forma opcional.
     *
     * @param pvn_IdTipoTramite tipo de trámite temporal
     * @return filas de la lista
     */
    public List<EntFilaTramiteTemporal> SIMT_TRAMITE_TEMPORAL_ObtenerLista(Integer pvn_IdTipoTramite) {
        return this.vgo_EntityManager.createQuery(
                        "SELECT NEW cr.ac.ucr.sim.entidadnegocio.tramites.EntFilaTramiteTemporal("
                                + "t, x, e, m.vgc_CodigoIso, p.vgc_Nombre) "
                                + "FROM EntTramiteTemporal x "
                                + "JOIN EntTramite t ON t.vgn_IdTramite = x.vgn_IdTramite "
                                + "JOIN EntEstadoTramite e ON e.vgn_IdEstadoTramite = t.vgn_IdEstadoTramite "
                                + "JOIN EntMoneda m ON m.vgn_IdMoneda = t.vgn_IdMoneda "
                                + "LEFT JOIN EntPais p ON p.vgn_IdPais = x.vgn_IdPaisDestino "
                                + "WHERE t.vgn_IdTipoTramite = :pvn_IdTipoTramite "
                                + "ORDER BY t.vgd_FechaCreacion DESC",
                        EntFilaTramiteTemporal.class)
                .setParameter("pvn_IdTipoTramite", pvn_IdTipoTramite)
                .getResultList();
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene las unidades solicitantes ya registradas, para sugerirlas en el
     *   formulario y evitar que la misma unidad se escriba de formas distintas.
     *
     * @return unidades solicitantes ordenadas alfabéticamente
     */
    public List<String> SIMT_TRAMITE_TEMPORAL_ObtenerUnidadesSolicitantes() {
        return this.vgo_EntityManager.createQuery(
                        "SELECT DISTINCT x.vgc_UnidadSolicitante FROM EntTramiteTemporal x "
                                + "ORDER BY x.vgc_UnidadSolicitante",
                        String.class)
                .getResultList();
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Obtiene los vencimientos que deben alertarse.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: devuelve los expedientes abiertos que vencen en la fecha límite o antes, y no
     *     solo los que vencen exactamente ese día: el vencimiento se registra cuando la aduana lo
     *     autoriza y puede caer ya dentro del periodo de alerta. Un expediente cerrado no se alerta.
     *
     * @param pvd_FechaLimite último vencimiento que entra en el periodo de alerta
     * @return expedientes por alertar con su usuario responsable
     */
    public List<EntVencimientoTemporal> SIMT_TRAMITE_TEMPORAL_ObtenerPorVencimiento(LocalDate pvd_FechaLimite) {
        return this.vgo_EntityManager.createQuery(
                        "SELECT NEW cr.ac.ucr.sim.entidadnegocio.tramites.EntVencimientoTemporal("
                                + "t.vgn_IdTramite, t.vgn_IdUsuarioResponsable, x.vgd_FechaVencimiento) "
                                + "FROM EntTramiteTemporal x, EntTramite t, EntEstadoTramite e "
                                + "WHERE x.vgn_IdTramite = t.vgn_IdTramite "
                                + "AND e.vgn_IdEstadoTramite = t.vgn_IdEstadoTramite "
                                + "AND e.vgb_EsFinal = false "
                                + "AND x.vgd_FechaVencimiento IS NOT NULL "
                                + "AND x.vgd_FechaVencimiento <= :pvd_FechaLimite",
                        EntVencimientoTemporal.class)
                .setParameter("pvd_FechaLimite", pvd_FechaLimite)
                .getResultList();
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Obtiene los usuarios validados con un rol de agente de aduanas activo.
     *
     * @return agentes disponibles ordenados por nombre
     */
    public List<EntAgenteTemporal> SIMF_USUARIO_ObtenerAgentesResponsables() {
        List<?> vlo_Filas = this.vgo_EntityManager.createNativeQuery(
                        "SELECT DISTINCT u.ID_USUARIO, u.NOMBRE_COMPLETO "
                                + "FROM SIMF_USUARIO u "
                                + "JOIN SIMT_USUARIO_ROL ur ON ur.ID_USUARIO = u.ID_USUARIO "
                                + "JOIN SIMM_ROL r ON r.ID_ROL = ur.ID_ROL "
                                + "WHERE u.ESTADO_VALIDACION = 'VAL' AND u.ESTADO_ROL = 'ASI' "
                                + "AND ur.ESTADO = 'ACT' AND ur.FECHA_FINALIZACION IS NULL "
                                + "AND r.ESTADO = 'ACT' AND UPPER(r.NOMBRE) LIKE '%AGENTE%' "
                                + "ORDER BY u.NOMBRE_COMPLETO")
                .getResultList();
        return vlo_Filas.stream()
                .map(Object[].class::cast)
                .map(pvo_Fila -> new EntAgenteTemporal(
                        ((Number) pvo_Fila[0]).intValue(),
                        pvo_Fila[1].toString()))
                .toList();
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Asigna un agente de aduanas a un trámite temporal.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvn_IdAgente usuario agente
     */
    public void SIMT_TEMPORAL_AGENTE_Insertar(Integer pvn_IdTramite, Integer pvn_IdAgente) {
        this.vgo_EntityManager.createNativeQuery(
                        "INSERT INTO SIMT_TEMPORAL_AGENTE (ID_TRAMITE, ID_USUARIO_AGENTE) "
                                + "VALUES (:pvn_IdTramite, :pvn_IdAgente)")
                .setParameter("pvn_IdTramite", pvn_IdTramite)
                .setParameter("pvn_IdAgente", pvn_IdAgente)
                .executeUpdate();
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Obtiene los agentes asignados a un trámite temporal, con su nombre.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: devuelve también el nombre, para mostrar los agentes en el expediente.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return agentes asignados ordenados por nombre
     */
    public List<EntAgenteTemporal> SIMT_TEMPORAL_AGENTE_ObtenerLista(Integer pvn_IdTramite) {
        List<?> vlo_Filas = this.vgo_EntityManager.createNativeQuery(
                        "SELECT u.ID_USUARIO, u.NOMBRE_COMPLETO "
                                + "FROM SIMT_TEMPORAL_AGENTE a "
                                + "JOIN SIMF_USUARIO u ON u.ID_USUARIO = a.ID_USUARIO_AGENTE "
                                + "WHERE a.ID_TRAMITE = :pvn_IdTramite "
                                + "ORDER BY u.NOMBRE_COMPLETO")
                .setParameter("pvn_IdTramite", pvn_IdTramite)
                .getResultList();
        return vlo_Filas.stream()
                .map(Object[].class::cast)
                .map(pvo_Fila -> new EntAgenteTemporal(
                        ((Number) pvo_Fila[0]).intValue(),
                        pvo_Fila[1].toString()))
                .toList();
    }
}
