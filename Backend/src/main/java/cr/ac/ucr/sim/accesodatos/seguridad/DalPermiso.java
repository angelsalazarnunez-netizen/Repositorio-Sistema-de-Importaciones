package cr.ac.ucr.sim.accesodatos.seguridad;

import cr.ac.ucr.sim.entidadnegocio.seguridad.EntPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Descripcion: Repositorio de Permisos.
 */
@Repository
public interface DalPermiso extends JpaRepository<EntPermiso, Long> {

    /**
     * Descripcion: Obtiene permiso por código.
     * @param vlc_Codigo codigo del permiso.
     * @return permiso encontrado, si existe.
     */
    @Query("""
            SELECT p
            FROM EntPermiso p
            WHERE UPPER(p.vgc_Codigo) = UPPER(:vlc_Codigo)
            """)
    Optional<EntPermiso> ObtenerPorCodigo(
            @Param("vlc_Codigo") String vlc_Codigo
    );

    /**
     * Descripcion: Verificar si existe un permiso.
     * @param vlc_Codigo codigo del permiso a consultar.
     * @return true si existe un permiso con el codigo indicado.
     */
    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
            FROM EntPermiso p
            WHERE UPPER(p.vgc_Codigo) = UPPER(:vlc_Codigo)
            """)
    boolean ExistePorCodigo(
            @Param("vlc_Codigo") String vlc_Codigo
    );
}