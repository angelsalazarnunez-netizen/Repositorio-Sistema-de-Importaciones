package cr.ac.ucr.sim.accesodatos.seguridad;

import cr.ac.ucr.sim.entidadnegocio.seguridad.EntRol;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.seguridad.EntUsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Descripcion: Repositorio del registro de la
 *  * la asignacion de un rol a un usuario. (usuario_rol)
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main: las consultas usan la entidad EntUsuario
 *     de main (entidadnegocio.autenticacion).
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: heurística H2 (EH5-01). Se agregan las consultas para contar los roles activos
 *     vigentes de un usuario (asignación activa y rol activo) y los usuarios de un rol que
 *     quedarían sin roles activos si el rol se inactiva.
 */
@Repository
public interface DalUsuarioRol
        extends JpaRepository<EntUsuarioRol, Long> {

    /**
     * Descripcion: obtiene asignaciones de roles
     * registradas para un usuario, también asignaciones inactivas.
     * @param vlo_EnUsuario usuario a consultar.
     * @return lista de asignaciones de roles del usuario.
     */
    @Query("""
            SELECT ur
            FROM EntUsuarioRol ur
            WHERE ur.vgo_EnUsuario = :vlo_EnUsuario
            """)
    List<EntUsuarioRol> ObtenerPorUsuario(
            @Param("vlo_EnUsuario")
            EntUsuario vlo_EnUsuario
    );

    /**
     * Descripcion: obtiene las asignaciones de roles
     * de un usuario para el estado.
     * @param vlo_EnUsuario usuario a consultar.
     * @param vlc_Estado estado de la asignacion.
     * @return lista de asignaciones encontradas.
     */
    @Query("""
            SELECT ur
            FROM EntUsuarioRol ur
            WHERE ur.vgo_EnUsuario = :vlo_EnUsuario
              AND ur.vgc_Estado = :vlc_Estado
            """)
    List<EntUsuarioRol> ObtenerPorUsuarioEstado(
            @Param("vlo_EnUsuario")
            EntUsuario vlo_EnUsuario,

            @Param("vlc_Estado")
            String vlc_Estado
    );

    /**
     * Descripcion: obtiene una asignacion para la combinacion
     * de usuario, rol y estado.
     * @param vlo_EnUsuario usuario a consultar.
     * @param vlo_EnRol rol a consultar.
     * @param vlc_Estado estado de la asignacion.
     * @return asignacion encontrada, si existe.
     */
    @Query("""
            SELECT ur
            FROM EntUsuarioRol ur
            WHERE ur.vgo_EnUsuario = :vlo_EnUsuario
              AND ur.vgo_EnRol = :vlo_EnRol
              AND ur.vgc_Estado = :vlc_Estado
            """)
    Optional<EntUsuarioRol> ObtenerPorUsuarioRolEstado(
            @Param("vlo_EnUsuario")
            EntUsuario vlo_EnUsuario,

            @Param("vlo_EnRol")
            EntRol vlo_EnRol,

            @Param("vlc_Estado")
            String vlc_Estado
    );

    /**
     * Descripcion: verifica si hay una asignacion
     * entre el usuario, el rol y el estado.
     * @param vlo_EnUsuario usuario a consultar.
     * @param vlo_EnRol rol a consultar.
     * @param vlc_Estado estado de la asignacion.
     * @return true si existe una asignacion con esos datos.
     */
    @Query("""
            SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END
            FROM EntUsuarioRol ur
            WHERE ur.vgo_EnUsuario = :vlo_EnUsuario
              AND ur.vgo_EnRol = :vlo_EnRol
              AND ur.vgc_Estado = :vlc_Estado
            """)
    boolean ExistePorUsuarioRolEstado(
            @Param("vlo_EnUsuario")
            EntUsuario vlo_EnUsuario,

            @Param("vlo_EnRol")
            EntRol vlo_EnRol,

            @Param("vlc_Estado")
            String vlc_Estado
    );

    /**
     * Descripcion: cuenta las asignaciones activas de un usuario
     * cuyo rol también está activo.
     * @param vlo_EnUsuario usuario a consultar.
     * @param vlc_Estado estado activo de la asignacion y del rol.
     * @return cantidad de roles activos vigentes.
     */
    @Query("""
            SELECT COUNT(ur)
            FROM EntUsuarioRol ur
            WHERE ur.vgo_EnUsuario = :vlo_EnUsuario
              AND ur.vgc_Estado = :vlc_Estado
              AND ur.vgo_EnRol.vgc_Estado = :vlc_Estado
            """)
    long ContarRolesActivosVigentes(
            @Param("vlo_EnUsuario")
            EntUsuario vlo_EnUsuario,

            @Param("vlc_Estado")
            String vlc_Estado
    );

    /**
     * Descripcion: obtiene los usuarios que tienen
     * una asignacion activa del rol indicado.
     * @param vlo_EnRol rol a consultar.
     * @param vlc_Estado estado activo de la asignacion.
     * @return usuarios con el rol asignado.
     */
    @Query("""
            SELECT DISTINCT ur.vgo_EnUsuario
            FROM EntUsuarioRol ur
            WHERE ur.vgo_EnRol = :vlo_EnRol
              AND ur.vgc_Estado = :vlc_Estado
            """)
    List<EntUsuario> ObtenerUsuariosPorRolEstado(
            @Param("vlo_EnRol")
            EntRol vlo_EnRol,

            @Param("vlc_Estado")
            String vlc_Estado
    );

    /**
     * Descripcion: cuenta los usuarios que tienen el rol indicado
     * y ningun otro rol activo vigente; son los que quedarian sin
     * roles activos si el rol se inactiva.
     * @param vlo_EnRol rol a consultar.
     * @param vlc_Estado estado activo de la asignacion y del rol.
     * @return cantidad de usuarios que quedarian sin roles activos.
     */
    @Query("""
            SELECT COUNT(DISTINCT ur.vgo_EnUsuario.vgn_IdUsuario)
            FROM EntUsuarioRol ur
            WHERE ur.vgo_EnRol = :vlo_EnRol
              AND ur.vgc_Estado = :vlc_Estado
              AND NOT EXISTS (
                  SELECT 1
                  FROM EntUsuarioRol otro
                  WHERE otro.vgo_EnUsuario = ur.vgo_EnUsuario
                    AND otro.vgo_EnRol <> :vlo_EnRol
                    AND otro.vgc_Estado = :vlc_Estado
                    AND otro.vgo_EnRol.vgc_Estado = :vlc_Estado
              )
            """)
    long ContarUsuariosSinOtroRolActivo(
            @Param("vlo_EnRol")
            EntRol vlo_EnRol,

            @Param("vlc_Estado")
            String vlc_Estado
    );
}
