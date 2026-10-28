package cr.ac.ucr.sim.accesodatos.tramites.exoneraciones;

import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntRequisitoDocumental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 03/10/2026
 * Descripcion: acceso a datos para la configuracion
 * de requisitos documentales de los tramites.
 */
@Repository
public interface DalRequisitoDocumental
        extends JpaRepository<EntRequisitoDocumental, Integer> {

    /**
     * Descripcion: obtiene los requisitos activos
     * que aplican a una modalidad de exoneracion.
     *
     * Incluye tanto requisitos generales como
     * requisitos especificos de la modalidad.
     *
     * @param vln_IdTipoTramite identificador del tipo de tramite.
     * @param vlc_TipoExoneracion modalidad de exoneracion.
     * @return requisitos documentales aplicables.
     */
    @Query("""
            SELECT r
            FROM EntRequisitoDocumental r
            WHERE r.vgo_EnTipoTramite.vgn_IdTipoTramite = :vln_IdTipoTramite
              AND r.vgc_Estado = 'ACT'
              AND (
                    r.vgc_TipoExoneracion = 'GENERAL'
                    OR r.vgc_TipoExoneracion = :vlc_TipoExoneracion
                  )
            ORDER BY r.vgb_Obligatorio DESC,
                     r.vgo_EnTipoDocumento.vgc_Nombre ASC
            """)
    List<EntRequisitoDocumental> ObtenerRequisitosAplicables(
            @Param("vln_IdTipoTramite")
            Integer vln_IdTipoTramite,

            @Param("vlc_TipoExoneracion")
            String vlc_TipoExoneracion
    );

    /**
     * Descripcion: determina si ya existe un requisito
     * activo para la combinacion indicada.
     *
     * @param vln_IdTipoTramite tipo de tramite.
     * @param vln_IdTipoDocumento tipo de documento.
     * @param vlc_TipoExoneracion modalidad.
     * @return verdadero si el requisito existe.
     */
    @Query("""
            SELECT COUNT(r) > 0
            FROM EntRequisitoDocumental r
            WHERE r.vgo_EnTipoTramite.vgn_IdTipoTramite = :vln_IdTipoTramite
              AND r.vgo_EnTipoDocumento.vgn_IdTipoDocumento = :vln_IdTipoDocumento
              AND r.vgc_TipoExoneracion = :vlc_TipoExoneracion
              AND r.vgc_Estado = 'ACT'
            """)
    boolean ExisteRequisitoActivo(
            @Param("vln_IdTipoTramite")
            Integer vln_IdTipoTramite,

            @Param("vln_IdTipoDocumento")
            Integer vln_IdTipoDocumento,

            @Param("vlc_TipoExoneracion")
            String vlc_TipoExoneracion
    );

    /**
     * Descripcion: obtiene todos los requisitos activos
     * de un tipo de tramite.
     *
     * @param vln_IdTipoTramite identificador del tipo de tramite.
     * @return requisitos activos.
     */
    @Query("""
            SELECT r
            FROM EntRequisitoDocumental r
            WHERE r.vgo_EnTipoTramite.vgn_IdTipoTramite = :vln_IdTipoTramite
              AND r.vgc_Estado = 'ACT'
            ORDER BY r.vgc_TipoExoneracion ASC,
                     r.vgo_EnTipoDocumento.vgc_Nombre ASC
            """)
    List<EntRequisitoDocumental> ObtenerRequisitosActivosPorTipoTramite(
            @Param("vln_IdTipoTramite")
            Integer vln_IdTipoTramite
    );
}