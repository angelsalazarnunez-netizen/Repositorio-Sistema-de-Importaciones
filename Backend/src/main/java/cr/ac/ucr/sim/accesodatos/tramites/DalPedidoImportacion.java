package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntOpcionImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTransferenciaPedidoImportacion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class DalPedidoImportacion {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    public void SIMT_PEDIDO_PROV_InsertarRegistro(EntPedidoImportacion pvo_EntPedidoImportacion) {
        this.vgo_EntityManager.persist(pvo_EntPedidoImportacion);
        this.vgo_EntityManager.flush();
    }

    public void SIMT_PEDIDO_PROV_ModificarRegistro(EntPedidoImportacion pvo_EntPedidoImportacion) {
        this.vgo_EntityManager.merge(pvo_EntPedidoImportacion);
        this.vgo_EntityManager.flush();
    }

    public EntPedidoImportacion SIMT_PEDIDO_PROV_ObtenerPorReferenciaYUsuario(
            String pvc_Referencia,
            Integer pvn_IdUsuario) {
        String vlc_Jpql = "SELECT p FROM EntPedidoImportacion p, EntTramite t "
                + "WHERE p.vgn_IdTramite = t.vgn_IdTramite "
                + "AND t.vgc_ReferenciaInterna = :pvc_Referencia "
                + "AND t.vgn_IdUsuarioResponsable = :pvn_IdUsuario";
        TypedQuery<EntPedidoImportacion> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntPedidoImportacion.class);
        vlo_Consulta.setParameter("pvc_Referencia", pvc_Referencia);
        vlo_Consulta.setParameter("pvn_IdUsuario", pvn_IdUsuario);
        try {
            return vlo_Consulta.getSingleResult();
        } catch (NoResultException vlo_Excepcion) {
            return null;
        }
    }

    public List<EntResumenPedidoImportacion> SIMT_PEDIDO_PROV_ObtenerListaPorUsuario(Integer pvn_IdUsuario) {
        String vlc_Jpql = "SELECT NEW cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenPedidoImportacion("
                + "t.vgc_ReferenciaInterna, e.vgc_Nombre, p.vgc_UnidadSolicitante, "
                + "p.vgc_CompaniaAdjudicada, p.vgc_NumeroContratacion, "
                + "p.vgd_FechaRecibidoExpediente, t.vgd_FechaCreacion) "
                + "FROM EntPedidoImportacion p, EntTramite t, EntEstadoTramite e "
                + "WHERE p.vgn_IdTramite = t.vgn_IdTramite "
                + "AND e.vgn_IdEstadoTramite = t.vgn_IdEstadoTramite "
                + "AND t.vgn_IdUsuarioResponsable = :pvn_IdUsuario "
                + "ORDER BY t.vgd_FechaCreacion DESC";
        TypedQuery<EntResumenPedidoImportacion> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntResumenPedidoImportacion.class);
        vlo_Consulta.setParameter("pvn_IdUsuario", pvn_IdUsuario);
        return vlo_Consulta.getResultList();
    }

    public void SIMT_PEDIDO_PROV_EliminarRegistro(Integer pvn_IdTramite) {
        EntPedidoImportacion vlo_EntPedido = this.vgo_EntityManager.find(
                EntPedidoImportacion.class,
                pvn_IdTramite);
        if (vlo_EntPedido != null) {
            this.vgo_EntityManager.remove(vlo_EntPedido);
            this.vgo_EntityManager.flush();
        }
    }

    public List<String> SIMM_OPCION_IMPORTACION_ObtenerPorTipo(String pvc_TipoOpcion) {
        String vlc_Jpql = "SELECT o.vgc_Valor FROM EntOpcionImportacion o "
                + "WHERE o.vgc_TipoOpcion = :pvc_TipoOpcion AND o.vgc_Estado = :pvc_Estado "
                + "ORDER BY UPPER(o.vgc_Valor)";
        TypedQuery<String> vlo_Consulta = this.vgo_EntityManager.createQuery(vlc_Jpql, String.class);
        vlo_Consulta.setParameter("pvc_TipoOpcion", pvc_TipoOpcion);
        vlo_Consulta.setParameter("pvc_Estado", "ACT");
        return vlo_Consulta.getResultList();
    }

    public void SIMM_OPCION_IMPORTACION_GuardarSiNoExiste(String pvc_TipoOpcion, String pvc_Valor) {
        String vlc_Jpql = "SELECT COUNT(o) FROM EntOpcionImportacion o "
                + "WHERE o.vgc_TipoOpcion = :pvc_TipoOpcion AND UPPER(o.vgc_Valor) = UPPER(:pvc_Valor)";
        TypedQuery<Long> vlo_Consulta = this.vgo_EntityManager.createQuery(vlc_Jpql, Long.class);
        vlo_Consulta.setParameter("pvc_TipoOpcion", pvc_TipoOpcion);
        vlo_Consulta.setParameter("pvc_Valor", pvc_Valor);
        if (vlo_Consulta.getSingleResult() == 0) {
            this.vgo_EntityManager.persist(new EntOpcionImportacion(pvc_TipoOpcion, pvc_Valor));
            this.vgo_EntityManager.flush();
        }
    }

    public List<EntTransferenciaPedidoImportacion> SIMT_PEDIDO_PROV_ObtenerTransferenciasAnterioresA(
            Integer pvn_IdUsuario,
            LocalDate pvd_FechaLimite) {
        String vlc_Jpql = "SELECT NEW cr.ac.ucr.sim.entidadnegocio.tramites.EntTransferenciaPedidoImportacion("
                + "t.vgc_ReferenciaInterna, p.vgd_FechaTransferencia) "
                + "FROM EntPedidoImportacion p, EntTramite t "
                + "WHERE p.vgn_IdTramite = t.vgn_IdTramite "
                + "AND t.vgn_IdUsuarioResponsable = :pvn_IdUsuario "
                + "AND p.vgd_FechaTransferencia IS NOT NULL "
                + "AND (p.vgn_MontoCanceladoCRC IS NULL OR p.vgn_MontoCanceladoCRC = 0) "
                + "AND p.vgd_FechaTransferencia <= :pvd_FechaLimite "
                + "ORDER BY p.vgd_FechaTransferencia";
        TypedQuery<EntTransferenciaPedidoImportacion> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntTransferenciaPedidoImportacion.class);
        vlo_Consulta.setParameter("pvn_IdUsuario", pvn_IdUsuario);
        vlo_Consulta.setParameter("pvd_FechaLimite", pvd_FechaLimite);
        return vlo_Consulta.getResultList();
    }
}
