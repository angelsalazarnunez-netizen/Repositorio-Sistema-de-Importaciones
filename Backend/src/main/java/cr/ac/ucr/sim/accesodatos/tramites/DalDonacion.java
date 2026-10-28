package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntDonacion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 10/09/2026
 * Descripción: Acceso a datos de SIMT_DONACION. No implementa reglas de negocio.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 15/09/2026
 *   Descripción: se agrega la consulta del registro para la gestión de donaciones (HU-044).
 */
@Repository
public class DalDonacion {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Inserta los datos propios de la donación.
     *
     * @param pvo_EntDonacion entidad a persistir
     */
    public void SIMT_DONACION_InsertarRegistro(EntDonacion pvo_EntDonacion) {
        this.vgo_EntityManager.persist(pvo_EntDonacion);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 23/09/2026
     * Descripción: Elimina los datos propios del expediente cuando el trámite se
     *   reclasifica a otro tipo y esos datos dejan de corresponderle.
     *
     * @param pvn_IdTramite identificador del expediente
     */
    public void SIMT_DONACION_EliminarRegistro(Integer pvn_IdTramite) {
        EntDonacion vlo_Registro = this.vgo_EntityManager.find(EntDonacion.class, pvn_IdTramite);
        if (vlo_Registro != null) {
            this.vgo_EntityManager.remove(vlo_Registro);
            this.vgo_EntityManager.flush();
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Obtiene los datos propios de la donación de un expediente. La llave primaria
     *   es compartida con SIMT_TRAMITE, por lo que se busca con el identificador del trámite.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return donación encontrada o null
     */
    public EntDonacion SIMT_DONACION_ObtenerRegistro(Integer pvn_IdTramite) {
        return pvn_IdTramite == null
                ? null
                : this.vgo_EntityManager.find(EntDonacion.class, pvn_IdTramite);
    }
}
