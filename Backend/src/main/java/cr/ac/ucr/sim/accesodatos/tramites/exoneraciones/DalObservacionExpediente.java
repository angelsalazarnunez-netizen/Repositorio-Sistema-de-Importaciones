package cr.ac.ucr.sim.accesodatos.tramites.exoneraciones;

import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntObservacionExpediente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 04/10/2026
 * Descripción: Acceso a datos de SIMT_OBSERVACION_EXPED (HU-020). No implementa reglas de
 *   negocio: los estados que filtran cada consulta los indica la lógica de negocio.
 *   No ofrece eliminación física: un borrador se da de baja modificando su ESTADO.
 *
 * Historial de cambios:
 *   04/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
@Repository
public class DalObservacionExpediente {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Inserta una observación de un expediente.
     *
     * @param pvo_EntObservacion entidad a persistir
     */
    public void SIMT_OBSERVACION_EXPED_InsertarRegistro(EntObservacionExpediente pvo_EntObservacion) {
        this.vgo_EntityManager.persist(pvo_EntObservacion);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Actualiza una observación de un expediente. Se usa para editar un borrador,
     *   confirmarlo o darlo de baja lógica.
     *
     * @param pvo_EntObservacion entidad a actualizar
     */
    public void SIMT_OBSERVACION_EXPED_ModificarRegistro(EntObservacionExpediente pvo_EntObservacion) {
        this.vgo_EntityManager.merge(pvo_EntObservacion);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Obtiene una observación por su identificador, sin bloquearla.
     *
     * @param pvn_IdObservacion identificador de la observación
     * @return observación encontrada o null
     */
    public EntObservacionExpediente SIMT_OBSERVACION_EXPED_ObtenerRegistro(Long pvn_IdObservacion) {
        return pvn_IdObservacion == null
                ? null
                : this.vgo_EntityManager.find(EntObservacionExpediente.class, pvn_IdObservacion);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Obtiene una observación por su identificador y bloquea su fila hasta que
     *   termine la transacción (SELECT ... FOR UPDATE). Evita que dos solicitudes simultáneas
     *   sobre el mismo borrador, por ejemplo editarlo y confirmarlo, se pisen entre sí.
     *   Debe invocarse dentro de una transacción.
     *
     * @param pvn_IdObservacion identificador de la observación
     * @return observación encontrada y bloqueada, o null
     */
    public EntObservacionExpediente SIMT_OBSERVACION_EXPED_ObtenerRegistroParaModificar(Long pvn_IdObservacion) {
        return pvn_IdObservacion == null
                ? null
                : this.vgo_EntityManager.find(
                        EntObservacionExpediente.class,
                        pvn_IdObservacion,
                        LockModeType.PESSIMISTIC_WRITE);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Obtiene el historial de observaciones de un expediente, ordenado de la más
     *   antigua a la más reciente según su fecha de confirmación.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvc_EstadoObservacion estado que identifica las observaciones confirmadas
     * @param pvc_Estado estado que identifica los registros vigentes
     * @return observaciones del historial
     */
    public List<EntObservacionExpediente> SIMT_OBSERVACION_EXPED_ObtenerHistorialPorTramite(
            Integer pvn_IdTramite,
            String pvc_EstadoObservacion,
            String pvc_Estado) {
        String vlc_Jpql =
                "SELECT o FROM EntObservacionExpediente o "
                        + "WHERE o.vgn_IdTramite = :pvn_IdTramite "
                        + "AND o.vgc_EstadoObservacion = :pvc_EstadoObservacion "
                        + "AND o.vgc_Estado = :pvc_Estado "
                        + "ORDER BY o.vgd_FechaConfirmacion, o.vgn_IdObservacion";

        TypedQuery<EntObservacionExpediente> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntObservacionExpediente.class);
        vlo_Consulta.setParameter("pvn_IdTramite", pvn_IdTramite);
        vlo_Consulta.setParameter("pvc_EstadoObservacion", pvc_EstadoObservacion);
        vlo_Consulta.setParameter("pvc_Estado", pvc_Estado);
        return vlo_Consulta.getResultList();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Obtiene las observaciones de un expediente redactadas por un usuario, en un
     *   estado dado, de la más antigua a la más reciente según su fecha de creación. Sirve para
     *   listar los borradores propios: los borradores de otros usuarios no se consultan.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvn_IdUsuarioAutor usuario que redactó las observaciones
     * @param pvc_EstadoObservacion estado que identifica los borradores
     * @param pvc_Estado estado que identifica los registros vigentes
     * @return observaciones del autor en el expediente
     */
    public List<EntObservacionExpediente> SIMT_OBSERVACION_EXPED_ObtenerPorTramiteYAutor(
            Integer pvn_IdTramite,
            Integer pvn_IdUsuarioAutor,
            String pvc_EstadoObservacion,
            String pvc_Estado) {
        String vlc_Jpql =
                "SELECT o FROM EntObservacionExpediente o "
                        + "WHERE o.vgn_IdTramite = :pvn_IdTramite "
                        + "AND o.vgn_IdUsuarioAutor = :pvn_IdUsuarioAutor "
                        + "AND o.vgc_EstadoObservacion = :pvc_EstadoObservacion "
                        + "AND o.vgc_Estado = :pvc_Estado "
                        + "ORDER BY o.vgd_FechaCreacion, o.vgn_IdObservacion";

        TypedQuery<EntObservacionExpediente> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntObservacionExpediente.class);
        vlo_Consulta.setParameter("pvn_IdTramite", pvn_IdTramite);
        vlo_Consulta.setParameter("pvn_IdUsuarioAutor", pvn_IdUsuarioAutor);
        vlo_Consulta.setParameter("pvc_EstadoObservacion", pvc_EstadoObservacion);
        vlo_Consulta.setParameter("pvc_Estado", pvc_Estado);
        return vlo_Consulta.getResultList();
    }
}
