package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntAlertaTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenAlertaTemporal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Autor: Camila Pastor Castro
 * Fecha: 03/10/2026
 * Descripción: Acceso a datos de las alertas de vencimiento de los trámites temporales (HU-027).
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: las consultas de alertas se separan de DalTramiteTemporal. La alerta se relaciona
 *     con el expediente por ID_TRAMITE, se dirige a un usuario destinatario y su resumen toma la
 *     referencia, el tipo y la descripción del expediente común (HU-025).
 */
@Repository
public class DalAlertaTemporal {

    // Columnas de la bandeja de alertas. La indicación se calcula en la lógica de negocio.
    private static final String SELECCION_RESUMEN =
            "SELECT NEW cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenAlertaTemporal("
                    + "a.vgn_IdAlertaTemporal, t.vgc_ReferenciaInterna, tt.vgc_Nombre, "
                    + "x.vgc_UnidadSolicitante, t.vgc_Descripcion, a.vgd_FechaVencimiento, "
                    + "x.vgn_CantidadPlazo, x.vgc_UnidadPlazo, x.vgn_CantidadExtension, "
                    + "x.vgc_UnidadExtension, x.vgc_NumeroContrato, a.vgc_Estado, a.vgc_EstadoCorreo, '') "
                    + "FROM EntAlertaTemporal a, EntTramiteTemporal x, EntTramite t, EntTipoTramite tt "
                    + "WHERE x.vgn_IdTramite = a.vgn_IdTramite "
                    + "AND t.vgn_IdTramite = a.vgn_IdTramite "
                    + "AND tt.vgn_IdTipoTramite = t.vgn_IdTipoTramite ";

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Indica si ya existe la alerta de un destinatario para un vencimiento.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvn_IdUsuarioDestino usuario que recibe la alerta
     * @param pvd_Vencimiento vencimiento alertado
     * @return true si la alerta ya existe
     */
    public boolean SIMT_ALERTA_TEMPORAL_Existe(
            Integer pvn_IdTramite,
            Integer pvn_IdUsuarioDestino,
            LocalDate pvd_Vencimiento) {
        Long vln_Cantidad = this.vgo_EntityManager.createQuery(
                        "SELECT COUNT(a) FROM EntAlertaTemporal a "
                                + "WHERE a.vgn_IdTramite = :pvn_IdTramite "
                                + "AND a.vgn_IdUsuarioDestino = :pvn_IdUsuarioDestino "
                                + "AND a.vgd_FechaVencimiento = :pvd_Vencimiento",
                        Long.class)
                .setParameter("pvn_IdTramite", pvn_IdTramite)
                .setParameter("pvn_IdUsuarioDestino", pvn_IdUsuarioDestino)
                .setParameter("pvd_Vencimiento", pvd_Vencimiento)
                .getSingleResult();
        return vln_Cantidad != null && vln_Cantidad > 0;
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Inserta una alerta de vencimiento.
     *
     * @param pvo_Alerta alerta por registrar
     */
    public void SIMT_ALERTA_TEMPORAL_InsertarRegistro(EntAlertaTemporal pvo_Alerta) {
        this.vgo_EntityManager.persist(pvo_Alerta);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Actualiza el estado de una alerta o de su correo.
     *
     * @param pvo_Alerta alerta modificada
     */
    public void SIMT_ALERTA_TEMPORAL_ModificarRegistro(EntAlertaTemporal pvo_Alerta) {
        this.vgo_EntityManager.merge(pvo_Alerta);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Obtiene una alerta de un destinatario.
     *
     * @param pvn_IdAlerta identificador de la alerta
     * @param pvn_IdUsuarioDestino usuario que recibe la alerta
     * @return alerta encontrada o null
     */
    public EntAlertaTemporal SIMT_ALERTA_TEMPORAL_ObtenerRegistro(Integer pvn_IdAlerta, Integer pvn_IdUsuarioDestino) {
        try {
            return this.vgo_EntityManager.createQuery(
                            "SELECT a FROM EntAlertaTemporal a "
                                    + "WHERE a.vgn_IdAlertaTemporal = :pvn_IdAlerta "
                                    + "AND a.vgn_IdUsuarioDestino = :pvn_IdUsuarioDestino",
                            EntAlertaTemporal.class)
                    .setParameter("pvn_IdAlerta", pvn_IdAlerta)
                    .setParameter("pvn_IdUsuarioDestino", pvn_IdUsuarioDestino)
                    .getSingleResult();
        } catch (NoResultException vlo_Excepcion) {
            return null;
        }
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Obtiene las alertas cuyo correo aún no se envió, de la más antigua a la más
     *   reciente.
     *
     * @return alertas con correo pendiente
     */
    public List<EntAlertaTemporal> SIMT_ALERTA_TEMPORAL_ObtenerCorreosPendientes() {
        return this.vgo_EntityManager.createQuery(
                        "SELECT a FROM EntAlertaTemporal a "
                                + "WHERE a.vgc_EstadoCorreo = 'PENDIENTE' "
                                + "ORDER BY a.vgd_FechaGeneracion, a.vgn_IdAlertaTemporal",
                        EntAlertaTemporal.class)
                .getResultList();
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Obtiene las alertas pendientes de un expediente, de todos sus destinatarios.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return alertas pendientes
     */
    public List<EntAlertaTemporal> SIMT_ALERTA_TEMPORAL_ObtenerPendientesPorTramite(Integer pvn_IdTramite) {
        return this.vgo_EntityManager.createQuery(
                        "SELECT a FROM EntAlertaTemporal a "
                                + "WHERE a.vgn_IdTramite = :pvn_IdTramite "
                                + "AND a.vgc_Estado = 'PENDIENTE'",
                        EntAlertaTemporal.class)
                .setParameter("pvn_IdTramite", pvn_IdTramite)
                .getResultList();
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Obtiene el resumen de una alerta para el correo y para la bandeja.
     *
     * @param pvn_IdAlerta identificador de la alerta
     * @return resumen de la alerta
     */
    public EntResumenAlertaTemporal SIMT_ALERTA_TEMPORAL_ObtenerResumen(Integer pvn_IdAlerta) {
        return this.vgo_EntityManager.createQuery(
                        SELECCION_RESUMEN + "AND a.vgn_IdAlertaTemporal = :pvn_IdAlerta",
                        EntResumenAlertaTemporal.class)
                .setParameter("pvn_IdAlerta", pvn_IdAlerta)
                .getSingleResult();
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Obtiene la bandeja de alertas de un usuario, de la más reciente a la más antigua.
     *
     * @param pvn_IdUsuarioDestino usuario que recibe las alertas
     * @return alertas del usuario
     */
    public List<EntResumenAlertaTemporal> SIMT_ALERTA_TEMPORAL_ObtenerLista(Integer pvn_IdUsuarioDestino) {
        return this.vgo_EntityManager.createQuery(
                        SELECCION_RESUMEN
                                + "AND a.vgn_IdUsuarioDestino = :pvn_IdUsuarioDestino "
                                + "ORDER BY a.vgd_FechaGeneracion DESC, a.vgn_IdAlertaTemporal DESC",
                        EntResumenAlertaTemporal.class)
                .setParameter("pvn_IdUsuarioDestino", pvn_IdUsuarioDestino)
                .getResultList();
    }
}
