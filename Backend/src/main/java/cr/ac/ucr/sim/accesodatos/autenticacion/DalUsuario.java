package cr.ac.ucr.sim.accesodatos.autenticacion;

import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Acceso a datos de SIMF_USUARIO. La HU-002 es la dueña de esta clase; aquí se
 *   declara solo la consulta que necesita el expediente de la HU-031. No implementa reglas
 *   de negocio.
 */
@Repository
public class DalUsuario {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene un usuario por su identificador.
     *
     * @param pvn_IdUsuario identificador del usuario
     * @return usuario encontrado o null
     */
    public EntUsuario SIMF_USUARIO_ObtenerRegistro(Integer pvn_IdUsuario) {
        return pvn_IdUsuario == null
                ? null
                : this.vgo_EntityManager.find(EntUsuario.class, pvn_IdUsuario);
    }

    public EntUsuario SIMF_USUARIO_ObtenerPorCorreoInstitucional(String pvc_CorreoInstitucional) {
        if (pvc_CorreoInstitucional == null || pvc_CorreoInstitucional.isBlank()) {
            return null;
        }

        TypedQuery<EntUsuario> vlo_Consulta = this.vgo_EntityManager.createQuery(
                "SELECT u FROM EntUsuario u "
                        + "WHERE LOWER(u.vgc_CorreoInstitucional) = LOWER(:pvc_CorreoInstitucional)",
                EntUsuario.class);
        vlo_Consulta.setParameter("pvc_CorreoInstitucional", pvc_CorreoInstitucional.trim());
        try {
            return vlo_Consulta.getSingleResult();
        } catch (NoResultException vlo_Excepcion) {
            return null;
        }
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 12/10/2026
     * Descripción: Indica si ya existe una cuenta con el correo institucional indicado.
     *
     * @param pvc_CorreoInstitucional correo institucional a consultar
     * @return verdadero si existe una cuenta con ese correo
     */
    public boolean SIMF_USUARIO_ExistePorCorreoInstitucional(String pvc_CorreoInstitucional) {
        return this.SIMF_USUARIO_ObtenerPorCorreoInstitucional(pvc_CorreoInstitucional) != null;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 12/10/2026
     * Descripción: Indica si ya existe una cuenta con el número de cédula indicado.
     *
     * @param pvc_NumeroCedula número de cédula a consultar
     * @return verdadero si existe una cuenta con esa cédula
     */
    public boolean SIMF_USUARIO_ExistePorNumeroCedula(String pvc_NumeroCedula) {
        if (pvc_NumeroCedula == null || pvc_NumeroCedula.isBlank()) {
            return false;
        }
        Long vln_Cantidad = this.vgo_EntityManager.createQuery(
                        "SELECT COUNT(u) FROM EntUsuario u WHERE u.vgc_NumeroCedula = :pvc_NumeroCedula",
                        Long.class)
                .setParameter("pvc_NumeroCedula", pvc_NumeroCedula.trim())
                .getSingleResult();
        return vln_Cantidad > 0;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 12/10/2026
     * Descripción: Obtiene la cuenta asociada a un token de verificación de correo.
     *
     * @param pvc_TokenVerificacion token enviado en el correo de validación
     * @return usuario encontrado o null
     */
    public EntUsuario SIMF_USUARIO_ObtenerPorTokenVerificacion(String pvc_TokenVerificacion) {
        if (pvc_TokenVerificacion == null || pvc_TokenVerificacion.isBlank()) {
            return null;
        }
        TypedQuery<EntUsuario> vlo_Consulta = this.vgo_EntityManager.createQuery(
                "SELECT u FROM EntUsuario u WHERE u.vgc_TokenVerificacion = :pvc_TokenVerificacion",
                EntUsuario.class);
        vlo_Consulta.setParameter("pvc_TokenVerificacion", pvc_TokenVerificacion.trim());
        return vlo_Consulta.getResultList().stream().findFirst().orElse(null);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 12/10/2026
     * Descripción: Registra una cuenta nueva. Debe llamarse dentro de una transacción.
     *
     * @param pvo_Usuario usuario a registrar
     * @return usuario registrado con su identificador
     */
    public EntUsuario SIMF_USUARIO_InsertarRegistro(EntUsuario pvo_Usuario) {
        this.vgo_EntityManager.persist(pvo_Usuario);
        return pvo_Usuario;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 12/10/2026
     * Descripción: Guarda los cambios de una cuenta existente. Debe llamarse dentro de una
     *   transacción.
     *
     * @param pvo_Usuario usuario modificado
     * @return usuario guardado
     */
    public EntUsuario SIMF_USUARIO_ModificarRegistro(EntUsuario pvo_Usuario) {
        return this.vgo_EntityManager.merge(pvo_Usuario);
    }
}
