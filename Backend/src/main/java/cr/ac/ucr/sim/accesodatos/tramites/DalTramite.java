package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntExpedienteResumen;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntFiltroBusquedaTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResultadoBusquedaTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenDonacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Acceso a datos de SIMT_TRAMITE. No implementa reglas de negocio.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 15/09/2026
 *   Descripción: se agregan la consulta por identificador, la actualización del expediente y la
 *     lista de expedientes de donación para la gestión de donaciones (HU-044).
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 16/09/2026
 *   Descripción: se agregan la búsqueda de trámites por criterios combinables y la consulta de los
 *     regímenes aduaneros y las aduanas de control registrados (HU-034).
 */
@Repository
public class DalTramite {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Inserta el expediente común del trámite.
     *
     * @param pvo_EntTramite entidad a persistir
     */
    public void SIMT_TRAMITE_InsertarRegistro(EntTramite pvo_EntTramite) {
        this.vgo_EntityManager.persist(pvo_EntTramite);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Obtiene un expediente por su identificador.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return expediente encontrado o null
     */
    public EntTramite SIMT_TRAMITE_ObtenerRegistro(Integer pvn_IdTramite) {
        return pvn_IdTramite == null
                ? null
                : this.vgo_EntityManager.find(EntTramite.class, pvn_IdTramite);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene un expediente por su identificador y bloquea su fila hasta que termine
     *   la transacción (SELECT ... FOR UPDATE). Evita que dos decisiones simultáneas sobre la misma
     *   exoneración se registren las dos (HU-022). Debe invocarse dentro de una transacción.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return expediente encontrado y bloqueado, o null
     */
    public EntTramite SIMT_TRAMITE_ObtenerRegistroParaModificar(Integer pvn_IdTramite) {
        return pvn_IdTramite == null
                ? null
                : this.vgo_EntityManager.find(EntTramite.class, pvn_IdTramite, LockModeType.PESSIMISTIC_WRITE);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene los expedientes que se encuentran en un estado, del más antiguo al más
     *   reciente. La bandeja de aprobaciones de exoneración (HU-022) la usa con el estado
     *   "Pendiente de aprobación".
     *
     * @param pvn_IdEstadoTramite identificador del estado
     * @return expedientes en ese estado
     */
    public List<EntTramite> SIMT_TRAMITE_ObtenerPorEstado(Integer pvn_IdEstadoTramite) {
        TypedQuery<EntTramite> vlo_Consulta = this.vgo_EntityManager.createQuery(
                "SELECT t FROM EntTramite t WHERE t.vgn_IdEstadoTramite = :pvn_IdEstadoTramite "
                        + "ORDER BY t.vgd_FechaCreacion, t.vgn_IdTramite",
                EntTramite.class);
        vlo_Consulta.setParameter("pvn_IdEstadoTramite", pvn_IdEstadoTramite);
        return vlo_Consulta.getResultList();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Actualiza un expediente, por ejemplo al cambiar su estado durante el seguimiento.
     *
     * @param pvo_EntTramite entidad a actualizar
     */
    public void SIMT_TRAMITE_ModificarRegistro(EntTramite pvo_EntTramite) {
        this.vgo_EntityManager.merge(pvo_EntTramite);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Indica si una referencia interna ya fue utilizada por cualquier tipo de trámite.
     *
     * @param pvc_ReferenciaInterna referencia interna a consultar
     * @return true si la referencia ya está registrada
     */
    public boolean SIMT_TRAMITE_ExistePorReferenciaInterna(String pvc_ReferenciaInterna) {
        String vlc_Jpql = "SELECT COUNT(t) FROM EntTramite t "
                + "WHERE t.vgc_ReferenciaInterna = :pvc_ReferenciaInterna";
        TypedQuery<Long> vlo_Consulta = this.vgo_EntityManager.createQuery(vlc_Jpql, Long.class);
        vlo_Consulta.setParameter("pvc_ReferenciaInterna", pvc_ReferenciaInterna);
        Long vln_Cantidad = vlo_Consulta.getSingleResult();
        return vln_Cantidad != null && vln_Cantidad > 0;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene un expediente por su referencia interna.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @return expediente encontrado o null
     */
    public EntTramite SIMT_TRAMITE_ObtenerPorReferenciaInterna(String pvc_ReferenciaInterna) {
        String vlc_Jpql = "SELECT t FROM EntTramite t "
                + "WHERE t.vgc_ReferenciaInterna = :pvc_ReferenciaInterna";
        TypedQuery<EntTramite> vlo_Consulta = this.vgo_EntityManager.createQuery(vlc_Jpql, EntTramite.class);
        vlo_Consulta.setParameter("pvc_ReferenciaInterna", pvc_ReferenciaInterna);
        try {
            return vlo_Consulta.getSingleResult();
        } catch (NoResultException vlo_Excepcion) {
            return null;
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene los expedientes de un tipo de trámite con los datos que muestra la
     *   pantalla de seguimiento, incluida la cantidad de documentos vigentes de cada uno.
     *
     * @param pvn_IdTipoTramite identificador del tipo de trámite
     * @param pvc_EstadoDocumento estado con el que se cuentan los documentos vigentes
     * @return expedientes ordenados del más reciente al más antiguo
     */
    public List<EntExpedienteResumen> SIMT_TRAMITE_ObtenerListaResumen(
            Integer pvn_IdTipoTramite,
            String pvc_EstadoDocumento) {
        String vlc_Jpql = "SELECT NEW cr.ac.ucr.sim.entidadnegocio.tramites.EntExpedienteResumen("
                + "t.vgc_ReferenciaInterna, e.vgc_Nombre, de.vgc_OrigenDependencia, "
                + "t.vgn_ValorEstimado, m.vgc_CodigoIso, u.vgc_NombreCompleto, t.vgd_FechaCreacion, "
                + "(SELECT COUNT(d) FROM EntDocumento d "
                + " WHERE d.vgn_IdTramite = t.vgn_IdTramite "
                + " AND d.vgc_EstadoDocumento = :pvc_EstadoDocumento)) "
                + "FROM EntTramite t "
                + "JOIN EntEstadoTramite e ON e.vgn_IdEstadoTramite = t.vgn_IdEstadoTramite "
                + "JOIN EntDesalmacenajeEspecial de ON de.vgn_IdTramite = t.vgn_IdTramite "
                + "JOIN EntUsuario u ON u.vgn_IdUsuario = t.vgn_IdUsuarioResponsable "
                // La moneda es opcional: un expediente reclasificado puede no traerla.
                + "LEFT JOIN EntMoneda m ON m.vgn_IdMoneda = t.vgn_IdMoneda "
                + "WHERE t.vgn_IdTipoTramite = :pvn_IdTipoTramite "
                + "ORDER BY t.vgd_FechaCreacion DESC";
        TypedQuery<EntExpedienteResumen> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntExpedienteResumen.class);
        vlo_Consulta.setParameter("pvn_IdTipoTramite", pvn_IdTipoTramite);
        vlo_Consulta.setParameter("pvc_EstadoDocumento", pvc_EstadoDocumento);
        return vlo_Consulta.getResultList();
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Obtiene los trámites de cualquier tipo que cumplen todos los criterios indicados.
     *   Un criterio vacío no restringe la búsqueda. La consulta se arma solo con fragmentos fijos y
     *   cada valor recibido viaja como parámetro con nombre, nunca concatenado.
     *   La moneda y la exoneración se unen con LEFT JOIN porque no todos los tipos de trámite las
     *   declaran. Los datos de DUA, gastos, despacho, envío de documentos y proveedor se unirán aquí
     *   de la misma forma cuando existan sus tablas.
     *
     * @param pvo_Filtro criterios normalizados de la búsqueda
     * @return trámites encontrados, del más reciente al más antiguo
     */
    public List<EntResultadoBusquedaTramite> SIMT_TRAMITE_ObtenerListaBusqueda(EntFiltroBusquedaTramite pvo_Filtro) {
        StringBuilder vlc_Jpql = new StringBuilder(
                "SELECT NEW cr.ac.ucr.sim.entidadnegocio.tramites.EntResultadoBusquedaTramite("
                        + "t.vgn_IdTramite, t.vgc_ReferenciaInterna, t.vgd_FechaCreacion, tt.vgc_Nombre, "
                        + "t.vgc_RegimenAduanero, u.vgc_NombreCompleto, e.vgc_CategoriaEstado, e.vgc_Nombre, "
                        + "t.vgn_ValorEstimado, m.vgc_CodigoIso, ex.vgc_TipoExoneracion) "
                        + "FROM EntTramite t "
                        + "JOIN EntTipoTramite tt ON tt.vgn_IdTipoTramite = t.vgn_IdTipoTramite "
                        + "JOIN EntEstadoTramite e ON e.vgn_IdEstadoTramite = t.vgn_IdEstadoTramite "
                        + "JOIN EntUsuario u ON u.vgn_IdUsuario = t.vgn_IdUsuarioResponsable "
                        + "LEFT JOIN EntMoneda m ON m.vgn_IdMoneda = t.vgn_IdMoneda "
                        + "LEFT JOIN EntExoneracion ex ON ex.vgn_IdTramite = t.vgn_IdTramite "
                        + "WHERE 1 = 1");
        Map<String, Object> vlo_MpParametros = new LinkedHashMap<>();

        if (pvo_Filtro.getFechaDesde() != null) {
            vlc_Jpql.append(" AND t.vgd_FechaCreacion >= :pvd_FechaDesde");
            vlo_MpParametros.put("pvd_FechaDesde", pvo_Filtro.getFechaDesde().atStartOfDay());
        }
        if (pvo_Filtro.getFechaHasta() != null) {
            // La fecha final incluye el día completo.
            vlc_Jpql.append(" AND t.vgd_FechaCreacion < :pvd_FechaHastaExclusiva");
            vlo_MpParametros.put("pvd_FechaHastaExclusiva", pvo_Filtro.getFechaHasta().plusDays(1).atStartOfDay());
        }
        if (pvo_Filtro.getReferenciaInterna() != null && !pvo_Filtro.getReferenciaInterna().isEmpty()) {
            vlc_Jpql.append(" AND UPPER(t.vgc_ReferenciaInterna) LIKE :pvc_ReferenciaInterna ESCAPE '!'");
            vlo_MpParametros.put("pvc_ReferenciaInterna",
                    "%" + this.EscaparComodines(pvo_Filtro.getReferenciaInterna().toUpperCase(Locale.ROOT)) + "%");
        }
        if (!pvo_Filtro.getIdTiposTramite().isEmpty()) {
            vlc_Jpql.append(" AND t.vgn_IdTipoTramite IN :pvo_LsIdTiposTramite");
            vlo_MpParametros.put("pvo_LsIdTiposTramite", pvo_Filtro.getIdTiposTramite());
        }
        if (!pvo_Filtro.getCategoriasEstado().isEmpty()) {
            vlc_Jpql.append(" AND e.vgc_CategoriaEstado IN :pvo_LsCategoriasEstado");
            vlo_MpParametros.put("pvo_LsCategoriasEstado", pvo_Filtro.getCategoriasEstado());
        }
        if (!pvo_Filtro.getIdMonedas().isEmpty()) {
            vlc_Jpql.append(" AND t.vgn_IdMoneda IN :pvo_LsIdMonedas");
            vlo_MpParametros.put("pvo_LsIdMonedas", pvo_Filtro.getIdMonedas());
        }
        if (!pvo_Filtro.getRegimenesAduaneros().isEmpty()) {
            vlc_Jpql.append(" AND t.vgc_RegimenAduanero IN :pvo_LsRegimenesAduaneros");
            vlo_MpParametros.put("pvo_LsRegimenesAduaneros", pvo_Filtro.getRegimenesAduaneros());
        }
        if (!pvo_Filtro.getTiposExoneracion().isEmpty()) {
            vlc_Jpql.append(" AND ex.vgc_TipoExoneracion IN :pvo_LsTiposExoneracion");
            vlo_MpParametros.put("pvo_LsTiposExoneracion", pvo_Filtro.getTiposExoneracion());
        }
        if (!pvo_Filtro.getAduanasControl().isEmpty()) {
            vlc_Jpql.append(" AND t.vgc_AduanaControl IN :pvo_LsAduanasControl");
            vlo_MpParametros.put("pvo_LsAduanasControl", pvo_Filtro.getAduanasControl());
        }
        vlc_Jpql.append(" ORDER BY t.vgd_FechaCreacion DESC, t.vgc_ReferenciaInterna");

        TypedQuery<EntResultadoBusquedaTramite> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql.toString(), EntResultadoBusquedaTramite.class);
        vlo_MpParametros.forEach(vlo_Consulta::setParameter);
        return vlo_Consulta.getResultList();
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Obtiene los regímenes aduaneros distintos registrados en los trámites, para
     *   ofrecerlos como opciones del filtro de búsqueda.
     *
     * @return regímenes aduaneros ordenados alfabéticamente
     */
    public List<String> SIMT_TRAMITE_ObtenerRegimenesAduaneros() {
        String vlc_Jpql = "SELECT DISTINCT t.vgc_RegimenAduanero FROM EntTramite t "
                + "WHERE t.vgc_RegimenAduanero IS NOT NULL AND TRIM(t.vgc_RegimenAduanero) <> '' "
                + "ORDER BY t.vgc_RegimenAduanero";
        return this.vgo_EntityManager.createQuery(vlc_Jpql, String.class).getResultList();
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Obtiene las aduanas de control distintas registradas en los trámites, para
     *   ofrecerlas como opciones del filtro de búsqueda.
     *
     * @return aduanas de control ordenadas alfabéticamente
     */
    public List<String> SIMT_TRAMITE_ObtenerAduanasControl() {
        String vlc_Jpql = "SELECT DISTINCT t.vgc_AduanaControl FROM EntTramite t "
                + "WHERE t.vgc_AduanaControl IS NOT NULL AND TRIM(t.vgc_AduanaControl) <> '' "
                + "ORDER BY t.vgc_AduanaControl";
        return this.vgo_EntityManager.createQuery(vlc_Jpql, String.class).getResultList();
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Escapa los comodines de LIKE para que el texto digitado se busque literalmente.
     *
     * @param pvc_Texto texto digitado por la persona usuaria
     * @return texto con los comodines escapados con el carácter !
     */
    private String EscaparComodines(String pvc_Texto) {
        return pvc_Texto.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Obtiene los expedientes de donación con los datos que muestra la pantalla de
     *   gestión, incluida la cantidad de documentos vigentes de cada uno. La consulta se separa de
     *   la del desalmacenaje especial porque cada tipo de trámite guarda sus datos propios en su
     *   propia tabla.
     *
     * @param pvn_IdTipoTramite identificador del tipo de trámite de donación
     * @param pvc_EstadoDocumento estado con el que se cuentan los documentos vigentes
     * @return expedientes ordenados del más reciente al más antiguo
     */
    public List<EntResumenDonacion> SIMT_TRAMITE_ObtenerListaDonaciones(
            Integer pvn_IdTipoTramite,
            String pvc_EstadoDocumento) {
        String vlc_Jpql = "SELECT NEW cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenDonacion("
                + "t.vgc_ReferenciaInterna, e.vgc_Nombre, dn.vgc_TipoDonacion, dn.vgc_NombreDonante, "
                + "t.vgn_ValorEstimado, m.vgc_CodigoIso, u.vgc_NombreCompleto, t.vgd_FechaCreacion, "
                + "(SELECT COUNT(d) FROM EntDocumento d "
                + " WHERE d.vgn_IdTramite = t.vgn_IdTramite "
                + " AND d.vgc_EstadoDocumento = :pvc_EstadoDocumento)) "
                + "FROM EntTramite t, EntEstadoTramite e, EntDonacion dn, "
                + "EntMoneda m, EntUsuario u "
                + "WHERE e.vgn_IdEstadoTramite = t.vgn_IdEstadoTramite "
                + "AND dn.vgn_IdTramite = t.vgn_IdTramite "
                + "AND m.vgn_IdMoneda = t.vgn_IdMoneda "
                + "AND u.vgn_IdUsuario = t.vgn_IdUsuarioResponsable "
                + "AND t.vgn_IdTipoTramite = :pvn_IdTipoTramite "
                + "ORDER BY t.vgd_FechaCreacion DESC";
        TypedQuery<EntResumenDonacion> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntResumenDonacion.class);
        vlo_Consulta.setParameter("pvn_IdTipoTramite", pvn_IdTipoTramite);
        vlo_Consulta.setParameter("pvc_EstadoDocumento", pvc_EstadoDocumento);
        return vlo_Consulta.getResultList();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 25/10/2026
     * Descripción: Obtiene los expedientes de exoneración con los datos que muestra la pantalla de
     *   entrada del módulo, incluida la cantidad de documentos vigentes de cada uno. Se separa de las
     *   demás listas porque la exoneración guarda sus datos propios en SIMT_EXONERACION.
     *
     * @param pvn_IdTipoTramite identificador del tipo de trámite de exoneración
     * @param pvc_EstadoDocumento estado con el que se cuentan los documentos vigentes
     * @return expedientes ordenados del más reciente al más antiguo
     */
    public List<EntResumenExoneracion> SIMT_TRAMITE_ObtenerListaExoneraciones(
            Integer pvn_IdTipoTramite,
            String pvc_EstadoDocumento) {
        String vlc_Jpql = "SELECT NEW cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenExoneracion("
                + "t.vgn_IdTramite, t.vgc_ReferenciaInterna, e.vgc_Nombre, ex.vgc_TipoExoneracion, "
                + "ex.vgc_NumeroContratacion, u.vgc_NombreCompleto, t.vgd_FechaCreacion, "
                + "(SELECT COUNT(d) FROM EntDocumento d "
                + " WHERE d.vgn_IdTramite = t.vgn_IdTramite "
                + " AND d.vgc_EstadoDocumento = :pvc_EstadoDocumento)) "
                + "FROM EntTramite t, EntEstadoTramite e, EntExoneracion ex, EntUsuario u "
                + "WHERE e.vgn_IdEstadoTramite = t.vgn_IdEstadoTramite "
                + "AND ex.vgn_IdTramite = t.vgn_IdTramite "
                + "AND u.vgn_IdUsuario = t.vgn_IdUsuarioResponsable "
                + "AND t.vgn_IdTipoTramite = :pvn_IdTipoTramite "
                + "ORDER BY t.vgd_FechaCreacion DESC";
        TypedQuery<EntResumenExoneracion> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntResumenExoneracion.class);
        vlo_Consulta.setParameter("pvn_IdTipoTramite", pvn_IdTipoTramite);
        vlo_Consulta.setParameter("pvc_EstadoDocumento", pvc_EstadoDocumento);
        return vlo_Consulta.getResultList();
    }
}
