package cr.ac.ucr.sim.accesodatos.seguridad;

import cr.ac.ucr.sim.entidadnegocio.seguridad.EntRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Descripcion: Repostorio de rol.
 */
@Repository
public interface DalRol extends JpaRepository<EntRol, Long> {

    /**
     * Descripcion: Obtiene rol por nombre.
     * @param vlc_Nombre nombre del rol.
     * @return rol encontrado, si existe.
     */
    @Query("""
            SELECT r
            FROM EntRol r
            WHERE LOWER(r.vgc_Nombre) = LOWER(:vlc_Nombre)
            """)
    Optional<EntRol> ObtenerPorNombre(
            @Param("vlc_Nombre") String vlc_Nombre
    );

    /**
     * Descripcion: Verificar si existe un rol por nombre.
     * @param vlc_Nombre nombre del rol a consultar.
     * @return true si existe un rol con el nombre indicado.
     */
    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM EntRol r
            WHERE LOWER(r.vgc_Nombre) = LOWER(:vlc_Nombre)
            """)
    boolean ExistePorNombre(
            @Param("vlc_Nombre") String vlc_Nombre
    );
}