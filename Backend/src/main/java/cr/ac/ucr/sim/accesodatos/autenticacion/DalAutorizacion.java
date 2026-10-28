package cr.ac.ucr.sim.accesodatos.autenticacion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class DalAutorizacion {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    @Transactional(readOnly = true)
    public List<String> SIMT_USUARIO_ROL_ObtenerRolesActivos(Integer pvn_IdUsuario) {
        List<?> vlo_Resultado = this.vgo_EntityManager.createNativeQuery(
                        """
                                SELECT DISTINCT r.NOMBRE
                                FROM SIMT_USUARIO_ROL ur
                                INNER JOIN SIMM_ROL r ON r.ID_ROL = ur.ID_ROL
                                WHERE ur.ID_USUARIO = :pvn_IdUsuario
                                  AND ur.ESTADO = 'ACT'
                                  AND ur.FECHA_FINALIZACION IS NULL
                                  AND r.ESTADO = 'ACT'
                                """)
                .setParameter("pvn_IdUsuario", pvn_IdUsuario)
                .getResultList();
        return vlo_Resultado.stream().map(Object::toString).toList();
    }

    @Transactional(readOnly = true)
    public List<String> SIMT_USUARIO_ROL_ObtenerPermisosActivos(Integer pvn_IdUsuario) {
        List<?> vlo_Resultado = this.vgo_EntityManager.createNativeQuery(
                        """
                                SELECT DISTINCT p.CODIGO
                                FROM SIMT_USUARIO_ROL ur
                                INNER JOIN SIMM_ROL r ON r.ID_ROL = ur.ID_ROL
                                INNER JOIN SIMT_ROL_PERMISO rp ON rp.ID_ROL = r.ID_ROL
                                INNER JOIN SIMM_PERMISO p ON p.ID_PERMISO = rp.ID_PERMISO
                                WHERE ur.ID_USUARIO = :pvn_IdUsuario
                                  AND ur.ESTADO = 'ACT'
                                  AND ur.FECHA_FINALIZACION IS NULL
                                  AND r.ESTADO = 'ACT'
                                  AND rp.ESTADO = 'ACT'
                                  AND p.ESTADO = 'ACT'
                                """)
                .setParameter("pvn_IdUsuario", pvn_IdUsuario)
                .getResultList();
        return vlo_Resultado.stream().map(Object::toString).toList();
    }
}
