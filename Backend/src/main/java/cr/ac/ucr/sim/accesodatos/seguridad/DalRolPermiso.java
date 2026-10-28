package cr.ac.ucr.sim.accesodatos.seguridad;

import cr.ac.ucr.sim.entidadnegocio.seguridad.EntPermiso;
import cr.ac.ucr.sim.entidadnegocio.seguridad.EntRol;
import cr.ac.ucr.sim.entidadnegocio.seguridad.EntRolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Descripcion: Repositorio del registro de la
 * la asignacion de un permiso a un rol. (rol_permiso)
 */
@Repository
public interface DalRolPermiso
        extends JpaRepository<EntRolPermiso, Long> {

    /**
     * Descripcion: Obtiene asignaciones por estado.
     * @param vlo_EnRol rol a consultar.
     * @param vlc_Estado estado de la asignacion.
     * @return lista de asignaciones encontradas.
     */
    @Query("""
            SELECT rp
            FROM EntRolPermiso rp
            WHERE rp.vgo_EnRol = :vlo_EnRol
              AND rp.vgc_Estado = :vlc_Estado
            """)
    List<EntRolPermiso> ObtenerPorRolEstado(
            @Param("vlo_EnRol")
            EntRol vlo_EnRol,

            @Param("vlc_Estado")
            String vlc_Estado
    );

    /**
     * Descripcion: obtiene una asignacion para la combinacion
     * de rol, permiso y estado.
     * @param vlo_EnRol rol a consultar.
     * @param vlo_EnPermiso permiso a consultar.
     * @param vlc_Estado estado de la asignacion.
     * @return asignacion encontrada, si existe.
     */
    @Query("""
            SELECT rp
            FROM EntRolPermiso rp
            WHERE rp.vgo_EnRol = :vlo_EnRol
              AND rp.vgo_EnPermiso = :vlo_EnPermiso
              AND rp.vgc_Estado = :vlc_Estado
            """)
    Optional<EntRolPermiso> ObtenerPorRolPermisoEstado(
            @Param("vlo_EnRol")
            EntRol vlo_EnRol,

            @Param("vlo_EnPermiso")
            EntPermiso vlo_EnPermiso,

            @Param("vlc_Estado")
            String vlc_Estado
    );

    /**
     * Descripcion: Verifica si existe asignacion
     * entre el rol, el permiso y el estado.
     * @param vlo_EnRol rol a consultar.
     * @param vlo_EnPermiso permiso a consultar.
     * @param vlc_Estado estado de la asignacion.
     * @return true si existe una asignacion con esos datos.
     */
    @Query("""
            SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END
            FROM EntRolPermiso rp
            WHERE rp.vgo_EnRol = :vlo_EnRol
              AND rp.vgo_EnPermiso = :vlo_EnPermiso
              AND rp.vgc_Estado = :vlc_Estado
            """)
    boolean ExistePorRolPermisoEstado(
            @Param("vlo_EnRol")
            EntRol vlo_EnRol,

            @Param("vlo_EnPermiso")
            EntPermiso vlo_EnPermiso,

            @Param("vlc_Estado")
            String vlc_Estado
    );
}