package cr.ac.ucr.sim.accesodatos.tramites.exoneraciones;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntAutorizacionExonet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Acceso a datos de SIMT_AUTORIZACION_EXONET (HU-023). No implementa reglas de
 *   negocio: los estados que filtran cada consulta los indica la lógica de negocio.
 *   No ofrece eliminación física: una autorización se anula o se sustituye modificando su
 *   ESTADO_AUTORIZACION, y el historial completo se conserva.
 *
 *   Cada inserción y modificación hace flush inmediato. Es indispensable para la sustitución:
 *   el índice único AK_SIMT_AUTORIZ_EXONET_VIGENTE exige que la autorización anterior quede
 *   marcada como sustituida en la base de datos antes de insertar la nueva vigente, y Hibernate,
 *   dentro de un mismo flush, ejecuta las inserciones antes que las actualizaciones.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
@Repository
public class DalAutorizacionExonet {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Inserta una solicitud / autorización EXONET de un expediente. Si el expediente
     *   ya tiene una vigente y la nueva también lo es, la base de datos rechaza la inserción por
     *   el índice único AK_SIMT_AUTORIZ_EXONET_VIGENTE.
     *
     * @param pvo_EntAutorizacion entidad a persistir
     */
    public void SIMT_AUTORIZACION_EXONET_InsertarRegistro(EntAutorizacionExonet pvo_EntAutorizacion) {
        this.vgo_EntityManager.persist(pvo_EntAutorizacion);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Actualiza una autorización. Se usa para completar datos pendientes de la
     *   vigente y para registrar su anulación o su sustitución. El flush deja el cambio aplicado
     *   en la base de datos antes de cualquier inserción posterior de la misma transacción.
     *
     * @param pvo_EntAutorizacion entidad a actualizar
     */
    public void SIMT_AUTORIZACION_EXONET_ModificarRegistro(EntAutorizacionExonet pvo_EntAutorizacion) {
        this.vgo_EntityManager.merge(pvo_EntAutorizacion);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene una autorización por su identificador, sin bloquearla.
     *
     * @param pvn_IdAutorizacion identificador de la autorización
     * @return autorización encontrada o null
     */
    public EntAutorizacionExonet SIMT_AUTORIZACION_EXONET_ObtenerRegistro(Long pvn_IdAutorizacion) {
        return pvn_IdAutorizacion == null
                ? null
                : this.vgo_EntityManager.find(EntAutorizacionExonet.class, pvn_IdAutorizacion);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene una autorización por su identificador y bloquea su fila hasta que
     *   termine la transacción (SELECT ... FOR UPDATE). Se usa al completar, anular o sustituir,
     *   después de bloquear el expediente, para leer el estado real de la fila y no uno anterior
     *   a una operación concurrente. Debe invocarse dentro de una transacción.
     *
     * @param pvn_IdAutorizacion identificador de la autorización
     * @return autorización encontrada y bloqueada, o null
     */
    public EntAutorizacionExonet SIMT_AUTORIZACION_EXONET_ObtenerRegistroParaModificar(Long pvn_IdAutorizacion) {
        return pvn_IdAutorizacion == null
                ? null
                : this.vgo_EntityManager.find(
                EntAutorizacionExonet.class,
                pvn_IdAutorizacion,
                LockModeType.PESSIMISTIC_WRITE);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Bloquea la fila del expediente de exoneración en SIMT_EXONERACION hasta que
     *   termine la transacción (SELECT ... FOR UPDATE). Serializa todas las escrituras de la
     *   HU-023 sobre un mismo expediente, incluido el registro de una autorización cuando todavía
     *   no existe ninguna vigente y, por lo tanto, no hay fila de autorización que bloquear.
     *   Solo bloquea: no modifica SIMT_EXONERACION. Debe invocarse dentro de una transacción y
     *   antes de cualquier otro bloqueo de la HU-023, para mantener siempre el mismo orden.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return expediente de exoneración bloqueado, o null si el trámite no es una exoneración
     */
    public EntExoneracion SIMT_AUTORIZACION_EXONET_BloquearExpediente(Integer pvn_IdTramite) {
        return pvn_IdTramite == null
                ? null
                : this.vgo_EntityManager.find(
                EntExoneracion.class,
                pvn_IdTramite,
                LockModeType.PESSIMISTIC_WRITE);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene la autorización de un expediente que está en el estado indicado. Con
     *   el estado vigente devuelve, como máximo, una fila: lo garantiza el índice único
     *   AK_SIMT_AUTORIZ_EXONET_VIGENTE.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvc_EstadoAutorizacion estado que identifica la autorización vigente
     * @return autorización encontrada o null
     */
    public EntAutorizacionExonet SIMT_AUTORIZACION_EXONET_ObtenerVigentePorTramite(
            Integer pvn_IdTramite,
            String pvc_EstadoAutorizacion) {
        String vlc_Jpql =
                "SELECT a FROM EntAutorizacionExonet a "
                        + "WHERE a.vgn_IdTramite = :pvn_IdTramite "
                        + "AND a.vgc_EstadoAutorizacion = :pvc_EstadoAutorizacion";

        TypedQuery<EntAutorizacionExonet> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntAutorizacionExonet.class);
        vlo_Consulta.setParameter("pvn_IdTramite", pvn_IdTramite);
        vlo_Consulta.setParameter("pvc_EstadoAutorizacion", pvc_EstadoAutorizacion);
        vlo_Consulta.setMaxResults(1);
        List<EntAutorizacionExonet> vlo_LsResultado = vlo_Consulta.getResultList();
        return vlo_LsResultado.isEmpty() ? null : vlo_LsResultado.get(0);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene el historial completo de autorizaciones de un expediente, en todos
     *   sus estados, de la más antigua a la más reciente según su fecha de registro. Con
     *   ID_AUTORIZACION_ANTERIOR de cada fila se reconstruye qué autorización sustituyó a cuál.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return autorizaciones del expediente
     */
    public List<EntAutorizacionExonet> SIMT_AUTORIZACION_EXONET_ObtenerHistorialPorTramite(Integer pvn_IdTramite) {
        String vlc_Jpql =
                "SELECT a FROM EntAutorizacionExonet a "
                        + "WHERE a.vgn_IdTramite = :pvn_IdTramite "
                        + "ORDER BY a.vgd_FechaRegistro, a.vgn_IdAutorizacionExonet";

        TypedQuery<EntAutorizacionExonet> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntAutorizacionExonet.class);
        vlo_Consulta.setParameter("pvn_IdTramite", pvn_IdTramite);
        return vlo_Consulta.getResultList();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Cuenta las autorizaciones registradas para un expediente, en cualquier estado.
     *   Permite saber si el expediente ya tiene historial de la HU-023: solo cuando no lo tiene se
     *   pueden ofrecer como precarga los números EXONET antiguos de SIMT_EXONERACION.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return cantidad de autorizaciones del expediente
     */
    public long SIMT_AUTORIZACION_EXONET_ContarPorTramite(Integer pvn_IdTramite) {
        String vlc_Jpql =
                "SELECT COUNT(a) FROM EntAutorizacionExonet a "
                        + "WHERE a.vgn_IdTramite = :pvn_IdTramite";

        TypedQuery<Long> vlo_Consulta = this.vgo_EntityManager.createQuery(vlc_Jpql, Long.class);
        vlo_Consulta.setParameter("pvn_IdTramite", pvn_IdTramite);
        return vlo_Consulta.getSingleResult();
    }
}
