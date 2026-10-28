package cr.ac.ucr.sim.accesodatos.tramites.exoneraciones;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntRevisionDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 03/10/2026
 * Descripcion: acceso a datos para las revisiones documentales
 * asociadas a los expedientes de exoneracion.
 */
@Repository
public interface DalRevisionDocumento extends JpaRepository<EntRevisionDocumento, Integer> {

    /**
     * Descripcion: obtiene la revision activa actual
     * de un documento.
     *
     * @param vlo_EnDocumento documento consultado.
     * @return revision activa, si existe.
     */
    @Query("""
            SELECT r
            FROM EntRevisionDocumento r
            WHERE r.vgo_EnDocumento = :vlo_EnDocumento
              AND r.vgc_Estado = 'ACT'
            ORDER BY r.vgd_FechaHora DESC
            """)
    Optional<EntRevisionDocumento> ObtenerRevisionActivaPorDocumento(
            @Param("vlo_EnDocumento")
            EntDocumento vlo_EnDocumento
    );

    /**
     * Descripcion: obtiene el historial completo
     * de revisiones de un documento.
     *
     * @param vlo_EnDocumento documento consultado.
     * @return historial ordenado de forma descendente.
     */
    @Query("""
            SELECT r
            FROM EntRevisionDocumento r
            WHERE r.vgo_EnDocumento = :vlo_EnDocumento
            ORDER BY r.vgd_FechaHora DESC
            """)
    List<EntRevisionDocumento> ObtenerHistorialPorDocumento(
            @Param("vlo_EnDocumento")
            EntDocumento vlo_EnDocumento
    );

    /**
     * Descripcion: obtiene las revisiones activas asociadas
     * a los documentos de un tramite.
     *
     * @param vln_IdTramite identificador del tramite.
     * @return revisiones activas del expediente.
     */
    @Query("""
            SELECT r
            FROM EntRevisionDocumento r
            WHERE r.vgo_EnDocumento.vgn_IdTramite = :vln_IdTramite
              AND r.vgo_EnDocumento.vgc_EstadoDocumento = 'ACT'
              AND r.vgc_Estado = 'ACT'
            ORDER BY r.vgo_EnDocumento.vgn_IdDocumento ASC
            """)
    List<EntRevisionDocumento> ObtenerRevisionesActivasPorTramite(
            @Param("vln_IdTramite")
            Integer vln_IdTramite
    );
}