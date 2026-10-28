package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntDesalmacenajeEspecial;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Acceso a datos de SIMT_DESALMACENAJE_ESPEC. No implementa reglas de negocio.
 */
@Repository
public class DalDesalmacenajeEspecial {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Inserta los datos propios del desalmacenaje especial.
     *
     * @param pvo_EntDesalmacenajeEspecial entidad a persistir
     */
    public void SIMT_DESALMACENAJE_ESPEC_InsertarRegistro(
            EntDesalmacenajeEspecial pvo_EntDesalmacenajeEspecial) {
        this.vgo_EntityManager.persist(pvo_EntDesalmacenajeEspecial);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene los datos del desalmacenaje especial de un expediente.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return registro encontrado o null
     */
    public EntDesalmacenajeEspecial SIMT_DESALMACENAJE_ESPEC_ObtenerRegistro(Integer pvn_IdTramite) {
        return this.vgo_EntityManager.find(EntDesalmacenajeEspecial.class, pvn_IdTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 23/09/2026
     * Descripción: Elimina los datos propios del expediente cuando el trámite se
     *   reclasifica a otro tipo y esos datos dejan de corresponderle.
     *
     * @param pvn_IdTramite identificador del expediente
     */
    public void SIMT_DESALMACENAJE_ESPEC_EliminarRegistro(Integer pvn_IdTramite) {
        EntDesalmacenajeEspecial vlo_Registro = this.vgo_EntityManager.find(EntDesalmacenajeEspecial.class, pvn_IdTramite);
        if (vlo_Registro != null) {
            this.vgo_EntityManager.remove(vlo_Registro);
            this.vgo_EntityManager.flush();
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 25/09/2026
     * Descripción: Actualiza los datos propios del desalmacenaje, como su estado documental.
     *
     * @param pvo_EntDesalmacenaje entidad con los datos actualizados
     */
    public void SIMT_DESALMACENAJE_ESPEC_ModificarRegistro(
            EntDesalmacenajeEspecial pvo_EntDesalmacenaje) {
        this.vgo_EntityManager.merge(pvo_EntDesalmacenaje);
        this.vgo_EntityManager.flush();
    }
}
