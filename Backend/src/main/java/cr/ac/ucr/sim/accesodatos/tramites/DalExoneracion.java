package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 15/09/2026
 * Descripción: Acceso a datos de SIMT_EXONERACION. No implementa reglas de negocio.
 *
 * Historial de cambios:
 *   Responsable: Katherine Susana Valverde Fallas
 *   Fecha: 22/09/2026
 *   Descripción: se agrega la actualización de los datos contractuales de la solicitud.
 */
@Repository
public class DalExoneracion {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Inserta los datos propios de la solicitud de exoneración.
     *
     * @param pvo_EntExoneracion entidad a persistir
     */
    public void SIMT_EXONERACION_InsertarRegistro(EntExoneracion pvo_EntExoneracion) {
        this.vgo_EntityManager.persist(pvo_EntExoneracion);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 22/09/2026
     * Descripción: Actualiza los datos propios de la solicitud, cuando la persona analista
     *   corrige los datos contractuales del expediente.
     *
     * @param pvo_EntExoneracion entidad con los datos corregidos
     */
    public void SIMT_EXONERACION_ModificarRegistro(EntExoneracion pvo_EntExoneracion) {
        this.vgo_EntityManager.merge(pvo_EntExoneracion);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene los datos de exoneración de un expediente.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return registro encontrado o null si el expediente no es una exoneración
     */
    public EntExoneracion SIMT_EXONERACION_ObtenerRegistro(Integer pvn_IdTramite) {
        return pvn_IdTramite == null
                ? null
                : this.vgo_EntityManager.find(EntExoneracion.class, pvn_IdTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 23/09/2026
     * Descripción: Elimina los datos propios del expediente cuando el trámite se
     *   reclasifica a otro tipo y esos datos dejan de corresponderle.
     *
     * @param pvn_IdTramite identificador del expediente
     */
    public void SIMT_EXONERACION_EliminarRegistro(Integer pvn_IdTramite) {
        EntExoneracion vlo_Registro = this.vgo_EntityManager.find(EntExoneracion.class, pvn_IdTramite);
        if (vlo_Registro != null) {
            this.vgo_EntityManager.remove(vlo_Registro);
            this.vgo_EntityManager.flush();
        }
    }
}
