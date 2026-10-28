package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntFilaPanelTramite;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Acceso a datos del panel de trámites activos (HU-030). Consulta los trámites de
 *   todos los tipos con los datos propios de cada uno, unidos con LEFT JOIN porque cada tipo tiene
 *   su propia tabla. Es de solo lectura: el panel no modifica trámites.
 *   Un trámite está en la vista activa si su estado no es final, su grupo de panel no es FUERA y,
 *   si es un pedido a proveedor, todavía no tiene fecha de desalmacenaje. El histórico es el
 *   complemento exacto de esa condición.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
@Repository
public class DalPanelTramites {

    private static final String CONDICION_ACTIVO =
            "(e.ES_FINAL = 0 AND e.CATEGORIA_ESTADO <> 'FIN' "
                    + "AND COALESCE(e.GRUPO_PANEL, '') <> 'FUERA' "
                    + "AND p.FECHA_DESALMACENAJE IS NULL)";

    private static final String CONSULTA_BASE =
            "SELECT t.ID_TRAMITE, t.REFERENCIA_INTERNA, tt.CODIGO, tt.NOMBRE, e.NOMBRE, "
                    + "e.CATEGORIA_ESTADO, e.GRUPO_PANEL, e.ES_FINAL, t.DESCRIPCION, t.VALOR_ESTIMADO, "
                    + "m.CODIGO_ISO, tmp.NUMERO_DUA_INICIO, tmp.FECHA_VENCIMIENTO, d.TIPO_DONACION, "
                    + "ex.TIPO_EXONERACION, de.ORIGEN_DEPENDENCIA, p.DESCRIPCION_SOLICITUD, p.MONTO_ORDEN, "
                    + "p.MONEDA_ORDEN, p.FECHA_INGRESO_PAIS, p.FECHA_DESALMACENAJE, t.FECHA_CREACION, "
                    + "u.NOMBRE_COMPLETO "
                    + "FROM SIMT_TRAMITE t "
                    + "JOIN SIMM_TIPO_TRAMITE tt ON tt.ID_TIPO_TRAMITE = t.ID_TIPO_TRAMITE "
                    + "JOIN SIMM_ESTADO_TRAMITE e ON e.ID_ESTADO_TRAMITE = t.ID_ESTADO_TRAMITE "
                    + "LEFT JOIN SIMF_USUARIO u ON u.ID_USUARIO = t.ID_USUARIO_RESPONSABLE "
                    + "LEFT JOIN SIMM_MONEDA m ON m.ID_MONEDA = t.ID_MONEDA "
                    + "LEFT JOIN SIMT_TRAMITE_TEMPORAL tmp ON tmp.ID_TRAMITE = t.ID_TRAMITE "
                    + "LEFT JOIN SIMT_DONACION d ON d.ID_TRAMITE = t.ID_TRAMITE "
                    + "LEFT JOIN SIMT_EXONERACION ex ON ex.ID_TRAMITE = t.ID_TRAMITE "
                    + "LEFT JOIN SIMT_DESALMACENAJE_ESPEC de ON de.ID_TRAMITE = t.ID_TRAMITE "
                    + "LEFT JOIN SIMT_PEDIDO_PROV p ON p.ID_TRAMITE = t.ID_TRAMITE ";

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene los trámites de la vista activa: en proceso, pendientes o por llegar,
     *   del más antiguo al más reciente para atender primero lo que lleva más tiempo.
     *
     * @return trámites activos
     */
    public List<EntFilaPanelTramite> SIMT_TRAMITE_ObtenerListaActivos() {
        return this.Consultar(CONSULTA_BASE + "WHERE " + CONDICION_ACTIVO
                + " ORDER BY t.FECHA_CREACION, t.ID_TRAMITE", null);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene los trámites que salieron del panel (desalmacenados, finalizados o
     *   cancelados), del más reciente al más antiguo. Ninguno se elimina: se conservan con su
     *   bitácora para la consulta histórica.
     *
     * @param pvn_Limite cantidad máxima de trámites
     * @return trámites del histórico
     */
    public List<EntFilaPanelTramite> SIMT_TRAMITE_ObtenerListaHistorico(int pvn_Limite) {
        return this.Consultar(CONSULTA_BASE + "WHERE NOT " + CONDICION_ACTIVO
                + " ORDER BY t.FECHA_CREACION DESC, t.ID_TRAMITE DESC", pvn_Limite);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Ejecuta la consulta y convierte cada fila en la entidad del panel.
     *
     * @param pvc_Sql consulta completa, armada solo con fragmentos fijos
     * @param pvn_Limite cantidad máxima de filas, o null para no limitar
     * @return filas del panel
     */
    private List<EntFilaPanelTramite> Consultar(String pvc_Sql, Integer pvn_Limite) {
        var vlo_Consulta = this.vgo_EntityManager.createNativeQuery(pvc_Sql);
        if (pvn_Limite != null) {
            vlo_Consulta.setMaxResults(pvn_Limite);
        }
        List<?> vlo_Filas = vlo_Consulta.getResultList();
        return vlo_Filas.stream()
                .map(Object[].class::cast)
                .map(pvo_Fila -> new EntFilaPanelTramite(
                        this.Entero(pvo_Fila[0]),
                        this.Texto(pvo_Fila[1]),
                        this.Texto(pvo_Fila[2]),
                        this.Texto(pvo_Fila[3]),
                        this.Texto(pvo_Fila[4]),
                        this.Texto(pvo_Fila[5]),
                        this.Texto(pvo_Fila[6]),
                        this.Logico(pvo_Fila[7]),
                        this.Texto(pvo_Fila[8]),
                        this.Decimal(pvo_Fila[9]),
                        this.Texto(pvo_Fila[10]),
                        this.Texto(pvo_Fila[11]),
                        this.Fecha(pvo_Fila[12]),
                        this.Texto(pvo_Fila[13]),
                        this.Texto(pvo_Fila[14]),
                        this.Texto(pvo_Fila[15]),
                        this.Texto(pvo_Fila[16]),
                        this.Decimal(pvo_Fila[17]),
                        this.Texto(pvo_Fila[18]),
                        this.Fecha(pvo_Fila[19]),
                        this.Fecha(pvo_Fila[20]),
                        this.FechaHora(pvo_Fila[21]),
                        this.Texto(pvo_Fila[22])))
                .toList();
    }

    private Integer Entero(Object pvo_Valor) {
        return pvo_Valor == null ? null : ((Number) pvo_Valor).intValue();
    }

    private String Texto(Object pvo_Valor) {
        return pvo_Valor == null ? null : pvo_Valor.toString();
    }

    private Boolean Logico(Object pvo_Valor) {
        if (pvo_Valor == null) {
            return null;
        }
        return pvo_Valor instanceof Boolean vlb_Valor ? vlb_Valor : ((Number) pvo_Valor).intValue() == 1;
    }

    private BigDecimal Decimal(Object pvo_Valor) {
        if (pvo_Valor == null) {
            return null;
        }
        return pvo_Valor instanceof BigDecimal vln_Valor ? vln_Valor : new BigDecimal(pvo_Valor.toString());
    }

    // El controlador JDBC puede devolver java.sql.Date o LocalDate según la versión de Hibernate.
    private LocalDate Fecha(Object pvo_Valor) {
        if (pvo_Valor == null) {
            return null;
        }
        if (pvo_Valor instanceof LocalDate vld_Valor) {
            return vld_Valor;
        }
        if (pvo_Valor instanceof java.sql.Date vld_Valor) {
            return vld_Valor.toLocalDate();
        }
        return LocalDate.parse(pvo_Valor.toString().substring(0, 10));
    }

    private LocalDateTime FechaHora(Object pvo_Valor) {
        if (pvo_Valor == null) {
            return null;
        }
        if (pvo_Valor instanceof LocalDateTime vld_Valor) {
            return vld_Valor;
        }
        if (pvo_Valor instanceof Timestamp vld_Valor) {
            return vld_Valor.toLocalDateTime();
        }
        return LocalDateTime.parse(pvo_Valor.toString().replace(' ', 'T'));
    }
}
