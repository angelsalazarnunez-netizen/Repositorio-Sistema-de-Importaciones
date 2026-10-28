package cr.ac.ucr.sim.accesodatos.autenticacion;

import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntSesion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public class DalSesion {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    @Transactional
    public EntSesion SIT_SESION_Guardar(EntSesion pvo_Sesion) {
        this.vgo_EntityManager.persist(pvo_Sesion);
        return pvo_Sesion;
    }

    @Transactional(readOnly = true)
    public EntSesion SIT_SESION_ObtenerActiva(String pvc_TokenHash, LocalDateTime pvd_Ahora) {
        TypedQuery<EntSesion> vlo_Consulta = this.vgo_EntityManager.createQuery(
                "SELECT s FROM EntSesion s "
                        + "WHERE s.vgc_TokenHash = :pvc_TokenHash "
                        + "AND s.vgb_EsActiva = true "
                        + "AND s.vgd_FechaExpiracion > :pvd_Ahora",
                EntSesion.class);
        vlo_Consulta.setParameter("pvc_TokenHash", pvc_TokenHash);
        vlo_Consulta.setParameter("pvd_Ahora", pvd_Ahora);
        return vlo_Consulta.getResultList().stream().findFirst().orElse(null);
    }

    @Transactional
    public void SIT_SESION_Desactivar(String pvc_TokenHash) {
        this.vgo_EntityManager.createQuery(
                        "UPDATE EntSesion s SET s.vgb_EsActiva = false "
                                + "WHERE s.vgc_TokenHash = :pvc_TokenHash AND s.vgb_EsActiva = true")
                .setParameter("pvc_TokenHash", pvc_TokenHash)
                .executeUpdate();
    }
}
