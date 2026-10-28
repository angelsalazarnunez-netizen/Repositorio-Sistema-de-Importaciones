package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntFiltroReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramiteReporte;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 25/09/2026
 * Descripción: Acceso a datos del reporte de consultas internas (HU-036). No implementa reglas
 *   de negocio: recupera los trámites que cumplen los filtros con los datos que el reporte
 *   resume, y la suma y el desglose los hace la capa de lógica.
 */
@Repository
public class DalReporte {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 25/09/2026
     * Descripción: Obtiene los trámites que cumplen los filtros del reporte, con su tipo, su
     *   régimen aduanero, su estado, su valor CIF y la cantidad de permisos regulatorios
     *   vigentes que tiene adjuntos.
     *
     * @param pvo_Filtro criterios de fecha, tipo, régimen, dependencia y tipo de exoneración
     * @param pvc_CodigoPermiso código del tipo de documento que cuenta como permiso regulatorio
     * @param pvc_EstadoDocumento estado con el que se cuentan los documentos vigentes
     * @return trámites del reporte, del más reciente al más antiguo
     */
    public List<EntTramiteReporte> SIMT_TRAMITE_ObtenerListaReporte(
            EntFiltroReporte pvo_Filtro,
            String pvc_CodigoPermiso,
            String pvc_EstadoDocumento) {

        StringBuilder vlo_Jpql = new StringBuilder()
                .append("SELECT NEW cr.ac.ucr.sim.entidadnegocio.tramites.EntTramiteReporte(")
                .append("t.vgc_ReferenciaInterna, tt.vgc_Nombre, t.vgc_RegimenAduanero, e.vgc_Nombre, ")
                .append("m.vgc_CodigoIso, t.vgn_ValorEstimado, de.vgc_OrigenDependencia, ")
                .append("t.vgc_PartidoResponsable, t.vgn_ImpuestosPagados, ")
                .append("t.vgn_ImpuestosExonerados, t.vgn_MontoBodegaje, ")
                .append("ex.vgc_TipoExoneracion, t.vgd_FechaCreacion, ")
                .append("(SELECT COUNT(d) FROM EntDocumento d, EntTipoDocumento td ")
                .append(" WHERE d.vgn_IdTramite = t.vgn_IdTramite ")
                .append(" AND td.vgn_IdTipoDocumento = d.vgn_IdTipoDocumento ")
                .append(" AND td.vgc_Codigo = :pvc_CodigoPermiso ")
                .append(" AND d.vgc_EstadoDocumento = :pvc_EstadoDocumento)) ")
                .append("FROM EntTramite t ")
                .append("JOIN EntTipoTramite tt ON tt.vgn_IdTipoTramite = t.vgn_IdTipoTramite ")
                .append("JOIN EntEstadoTramite e ON e.vgn_IdEstadoTramite = t.vgn_IdEstadoTramite ")
                .append("LEFT JOIN EntMoneda m ON m.vgn_IdMoneda = t.vgn_IdMoneda ")
                .append("LEFT JOIN EntDesalmacenajeEspecial de ON de.vgn_IdTramite = t.vgn_IdTramite ")
                .append("LEFT JOIN EntExoneracion ex ON ex.vgn_IdTramite = t.vgn_IdTramite ")
                .append("WHERE 1 = 1 ");

        List<String> vlo_LsCondiciones = new ArrayList<>();
        if (pvo_Filtro.getFechaDesde() != null) {
            vlo_LsCondiciones.add("AND t.vgd_FechaCreacion >= :pvd_FechaDesde ");
        }
        if (pvo_Filtro.getFechaHasta() != null) {
            vlo_LsCondiciones.add("AND t.vgd_FechaCreacion < :pvd_FechaHasta ");
        }
        if (pvo_Filtro.getCodigoTipoTramite() != null) {
            vlo_LsCondiciones.add("AND tt.vgc_Codigo = :pvc_CodigoTipoTramite ");
        }
        if (pvo_Filtro.getRegimenAduanero() != null) {
            vlo_LsCondiciones.add("AND LOWER(t.vgc_RegimenAduanero) LIKE :pvc_RegimenAduanero ");
        }
        if (pvo_Filtro.getTipoExoneracion() != null) {
            vlo_LsCondiciones.add("AND ex.vgc_TipoExoneracion = :pvc_TipoExoneracion ");
        }
        if (pvo_Filtro.getOrigenDependencia() != null) {
            // La consulta busca tanto en la dependencia de origen como en el partido responsable,
            // porque el costo se carga a uno u otro según el tipo de trámite.
            vlo_LsCondiciones.add("AND (LOWER(de.vgc_OrigenDependencia) LIKE :pvc_OrigenDependencia "
                    + "OR LOWER(t.vgc_PartidoResponsable) LIKE :pvc_OrigenDependencia) ");
        }
        vlo_LsCondiciones.forEach(vlo_Jpql::append);
        vlo_Jpql.append("ORDER BY t.vgd_FechaCreacion DESC");

        TypedQuery<EntTramiteReporte> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlo_Jpql.toString(), EntTramiteReporte.class);
        vlo_Consulta.setParameter("pvc_CodigoPermiso", pvc_CodigoPermiso);
        vlo_Consulta.setParameter("pvc_EstadoDocumento", pvc_EstadoDocumento);
        if (pvo_Filtro.getFechaDesde() != null) {
            vlo_Consulta.setParameter("pvd_FechaDesde", pvo_Filtro.getFechaDesde().atStartOfDay());
        }
        if (pvo_Filtro.getFechaHasta() != null) {
            // El rango incluye el día indicado: se compara contra el inicio del día siguiente.
            LocalDate vld_Siguiente = pvo_Filtro.getFechaHasta().plusDays(1);
            vlo_Consulta.setParameter("pvd_FechaHasta", vld_Siguiente.atStartOfDay());
        }
        if (pvo_Filtro.getCodigoTipoTramite() != null) {
            vlo_Consulta.setParameter("pvc_CodigoTipoTramite", pvo_Filtro.getCodigoTipoTramite());
        }
        if (pvo_Filtro.getRegimenAduanero() != null) {
            vlo_Consulta.setParameter("pvc_RegimenAduanero",
                    "%" + pvo_Filtro.getRegimenAduanero().toLowerCase() + "%");
        }
        if (pvo_Filtro.getTipoExoneracion() != null) {
            vlo_Consulta.setParameter("pvc_TipoExoneracion", pvo_Filtro.getTipoExoneracion());
        }
        if (pvo_Filtro.getOrigenDependencia() != null) {
            vlo_Consulta.setParameter("pvc_OrigenDependencia",
                    "%" + pvo_Filtro.getOrigenDependencia().toLowerCase() + "%");
        }
        return vlo_Consulta.getResultList();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Obtiene los partidos responsables del costo que ya están registrados en los
     *   expedientes, sin repetirlos, para ofrecerlos como filtro del reporte.
     *
     * @return partidos registrados, en orden alfabético
     */
    public List<String> SIMT_TRAMITE_ObtenerPartidosResponsables() {
        String vlc_Jpql = "SELECT DISTINCT t.vgc_PartidoResponsable FROM EntTramite t "
                + "WHERE t.vgc_PartidoResponsable IS NOT NULL "
                + "ORDER BY t.vgc_PartidoResponsable";
        return this.vgo_EntityManager.createQuery(vlc_Jpql, String.class).getResultList();
    }
}
