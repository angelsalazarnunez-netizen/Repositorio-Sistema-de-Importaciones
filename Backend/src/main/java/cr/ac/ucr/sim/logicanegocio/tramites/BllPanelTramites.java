package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.tramites.DalPanelTramites;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntFilaPanelTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.modelo.tramites.ModeloPanelTramitesRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.tramites.ModeloTramiteActivoRespuesta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Panel de trámites activos (HU-030). Reúne en una sola vista operativa los trámites
 *   de todos los tipos que están en proceso, pendientes de permisos, aprobaciones o exoneraciones,
 *   o por llegar.
 *   Reglas de negocio:
 *   - El grupo de cada trámite sale de GRUPO_PANEL de su estado. Un estado sin clasificar se ubica
 *     por su categoría: PEN = por llegar, ACT = en proceso.
 *   - Un trámite sale del panel en cuanto su estado es final, se desalmacena (grupo FUERA) o, si es
 *     un pedido a proveedor, registra su fecha de desalmacenaje. La consulta se hace en cada
 *     solicitud, así que la salida es automática: no hay que mover ni borrar nada.
 *   - Nada se elimina: los trámites que salieron se consultan en el histórico con su bitácora.
 *   - El vencimiento de los trámites temporales usa la misma regla que la HU-025 y la HU-027:
 *     próximo a vencer a 45 días o menos.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003). El panel puede limitarse a los tipos de trámite
 *     que el usuario puede consultar; los indicadores se calculan sobre esos mismos trámites.
 */
@Service
public class BllPanelTramites {

    private static final Map<String, String> NOMBRES_GRUPO = Map.of(
            Constantes.GRUPO_PANEL_POR_LLEGAR, Constantes.NOMBRE_GRUPO_PANEL_POR_LLEGAR,
            Constantes.GRUPO_PANEL_EN_PROCESO, Constantes.NOMBRE_GRUPO_PANEL_EN_PROCESO,
            Constantes.GRUPO_PANEL_PERMISOS, Constantes.NOMBRE_GRUPO_PANEL_PERMISOS,
            Constantes.GRUPO_PANEL_APROBACION, Constantes.NOMBRE_GRUPO_PANEL_APROBACION,
            Constantes.GRUPO_PANEL_EXONERACION, Constantes.NOMBRE_GRUPO_PANEL_EXONERACION,
            Constantes.GRUPO_PANEL_FUERA, Constantes.NOMBRE_GRUPO_PANEL_FUERA);

    private static final Map<String, String> NOMBRES_TIPO_DONACION = Map.of(
            Constantes.TIPO_DONACION_NACIONAL, "nacional",
            Constantes.TIPO_DONACION_INTERNACIONAL, "internacional",
            Constantes.TIPO_DONACION_FUNDACION_UCR, "Fundación UCR");

    private static final Map<String, String> NOMBRES_TIPO_EXONERACION = Map.of(
            Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL, Constantes.NOMBRE_TIPO_EXONERACION_COMPRA_INSTITUCIONAL,
            Constantes.TIPO_EXONERACION_DONACION, Constantes.NOMBRE_TIPO_EXONERACION_DONACION,
            Constantes.TIPO_EXONERACION_CLIENTE_EXTERNO, Constantes.NOMBRE_TIPO_EXONERACION_CLIENTE_EXTERNO,
            Constantes.TIPO_EXONERACION_VEHICULOS, "Vehículos",
            Constantes.TIPO_EXONERACION_MOVIMIENTO_TRASPASO, "Movimiento de traspaso");

    private final DalPanelTramites vgo_DalPanelTramites;
    private final BllAlertaTemporal vgo_BllAlertaTemporal;

    public BllPanelTramites(DalPanelTramites pvo_DalPanelTramites, BllAlertaTemporal pvo_BllAlertaTemporal) {
        this.vgo_DalPanelTramites = pvo_DalPanelTramites;
        this.vgo_BllAlertaTemporal = pvo_BllAlertaTemporal;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene el panel de trámites. Los indicadores siempre resumen la vista activa;
     *   la lista corresponde al alcance pedido: los trámites activos o el histórico de los que
     *   salieron del panel.
     *
     * @param pvc_Alcance ACTIVOS (por defecto) o HISTORICO
     * @return panel con sus indicadores y trámites
     */
    @Transactional(readOnly = true)
    public ModeloPanelTramitesRespuesta ObtenerPanel(String pvc_Alcance) {
        return this.ObtenerPanel(pvc_Alcance, null);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Obtiene el panel de trámites limitado a los tipos de trámite indicados (HU-003).
     *
     * @param pvc_Alcance ACTIVOS (por defecto) o HISTORICO
     * @param pvo_CodigosTipoPermitidos códigos de SIMM_TIPO_TRAMITE que se pueden mostrar; null
     *   muestra todos
     * @return panel con sus indicadores y trámites
     */
    @Transactional(readOnly = true)
    public ModeloPanelTramitesRespuesta ObtenerPanel(String pvc_Alcance, Set<String> pvo_CodigosTipoPermitidos) {
        String vlc_Alcance = this.NormalizarAlcance(pvc_Alcance);
        LocalDate vld_Hoy = this.vgo_BllAlertaTemporal.Hoy();

        List<ModeloTramiteActivoRespuesta> vlo_LsActivos = this.vgo_DalPanelTramites
                .SIMT_TRAMITE_ObtenerListaActivos()
                .stream()
                .filter(vlo_Fila -> this.TipoPermitido(vlo_Fila, pvo_CodigosTipoPermitidos))
                .map(vlo_Fila -> this.ConvertirFila(vlo_Fila, vld_Hoy))
                .sorted(this.OrdenOperativo())
                .toList();

        ModeloPanelTramitesRespuesta vlo_Panel = new ModeloPanelTramitesRespuesta();
        vlo_Panel.setAlcance(vlc_Alcance);
        vlo_Panel.setFechaConsulta(LocalDateTime.now());
        vlo_Panel.setTotalActivos(vlo_LsActivos.size());
        vlo_Panel.setTotalPorLlegar(this.Contar(vlo_LsActivos, Constantes.GRUPO_PANEL_POR_LLEGAR));
        vlo_Panel.setTotalEnProceso(vlo_LsActivos.size() - vlo_Panel.getTotalPorLlegar());
        vlo_Panel.setTotalPermisos(this.Contar(vlo_LsActivos, Constantes.GRUPO_PANEL_PERMISOS));
        vlo_Panel.setTotalAprobacion(this.Contar(vlo_LsActivos, Constantes.GRUPO_PANEL_APROBACION));
        vlo_Panel.setTotalExoneracion(this.Contar(vlo_LsActivos, Constantes.GRUPO_PANEL_EXONERACION));
        vlo_Panel.setTotalProximosVencer((int) vlo_LsActivos.stream()
                .filter(vlo_Tramite -> Constantes.VENCIMIENTO_PROXIMO.equals(vlo_Tramite.getNivelVencimiento())
                        || Constantes.VENCIMIENTO_VENCIDO.equals(vlo_Tramite.getNivelVencimiento()))
                .count());

        if (Constantes.ALCANCE_PANEL_HISTORICO.equals(vlc_Alcance)) {
            vlo_Panel.setTramites(this.vgo_DalPanelTramites
                    .SIMT_TRAMITE_ObtenerListaHistorico(Constantes.LIMITE_HISTORICO_PANEL)
                    .stream()
                    .filter(vlo_Fila -> this.TipoPermitido(vlo_Fila, pvo_CodigosTipoPermitidos))
                    .map(vlo_Fila -> this.ConvertirFila(vlo_Fila, vld_Hoy))
                    .toList());
        } else {
            vlo_Panel.setTramites(vlo_LsActivos);
        }
        return vlo_Panel;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Indica si la fila es de un tipo de trámite que se puede mostrar.
     *
     * @param pvo_Fila fila consultada
     * @param pvo_CodigosTipoPermitidos códigos permitidos; null permite todos
     * @return verdadero si la fila se muestra
     */
    boolean TipoPermitido(EntFilaPanelTramite pvo_Fila, Set<String> pvo_CodigosTipoPermitidos) {
        return pvo_CodigosTipoPermitidos == null
                || (pvo_Fila.codigoTipoTramite() != null
                        && pvo_CodigosTipoPermitidos.contains(pvo_Fila.codigoTipoTramite()));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Convierte una fila de la base de datos en una fila del panel: resuelve su grupo,
     *   su motivo, su valor declarado y la cuenta regresiva del vencimiento.
     *
     * @param pvo_Fila fila consultada
     * @param pvd_Hoy fecha de la consulta en Costa Rica
     * @return fila del panel
     */
    ModeloTramiteActivoRespuesta ConvertirFila(EntFilaPanelTramite pvo_Fila, LocalDate pvd_Hoy) {
        String vlc_Grupo = this.ResolverGrupo(pvo_Fila);

        ModeloTramiteActivoRespuesta vlo_Tramite = new ModeloTramiteActivoRespuesta();
        vlo_Tramite.setIdTramite(pvo_Fila.idTramite());
        vlo_Tramite.setReferenciaInterna(pvo_Fila.referenciaInterna());
        vlo_Tramite.setCodigoTipoTramite(pvo_Fila.codigoTipoTramite());
        vlo_Tramite.setTipoTramite(pvo_Fila.tipoTramite());
        vlo_Tramite.setMotivo(this.ResolverMotivo(pvo_Fila));
        vlo_Tramite.setNumeroDua(this.EsTemporal(pvo_Fila) ? pvo_Fila.numeroDua() : null);
        vlo_Tramite.setValorDeclarado(pvo_Fila.valorEstimado() != null ? pvo_Fila.valorEstimado() : pvo_Fila.montoOrden());
        vlo_Tramite.setMoneda(pvo_Fila.valorEstimado() != null ? pvo_Fila.moneda() : pvo_Fila.monedaOrden());
        vlo_Tramite.setEstado(pvo_Fila.estado());
        vlo_Tramite.setGrupoPanel(vlc_Grupo);
        vlo_Tramite.setNombreGrupoPanel(NOMBRES_GRUPO.get(vlc_Grupo));
        vlo_Tramite.setFechaRegistro(pvo_Fila.fechaRegistro());
        vlo_Tramite.setResponsable(pvo_Fila.responsable());

        // La cuenta regresiva solo aplica a los temporales que siguen abiertos.
        if (this.EsTemporal(pvo_Fila) && !Constantes.GRUPO_PANEL_FUERA.equals(vlc_Grupo)) {
            vlo_Tramite.setFechaVencimiento(pvo_Fila.fechaVencimiento());
            if (pvo_Fila.fechaVencimiento() == null) {
                vlo_Tramite.setNivelVencimiento(Constantes.VENCIMIENTO_SIN_FECHA);
            } else {
                long vln_Dias = ChronoUnit.DAYS.between(pvd_Hoy, pvo_Fila.fechaVencimiento());
                vlo_Tramite.setDiasRestantes(vln_Dias);
                vlo_Tramite.setNivelVencimiento(vln_Dias < 0
                        ? Constantes.VENCIMIENTO_VENCIDO
                        : vln_Dias <= Constantes.DIAS_ANTICIPACION_ALERTA_TEMPORAL
                                ? Constantes.VENCIMIENTO_PROXIMO
                                : Constantes.VENCIMIENTO_EN_PLAZO);
            }
        }
        return vlo_Tramite;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Determina el grupo del trámite en el panel. Un estado final o un pedido
     *   desalmacenado quedan FUERA; un pedido que ya ingresó al país deja de estar por llegar.
     *
     * @param pvo_Fila fila consultada
     * @return grupo del panel
     */
    String ResolverGrupo(EntFilaPanelTramite pvo_Fila) {
        if (Boolean.TRUE.equals(pvo_Fila.esFinal())
                || Constantes.CATEGORIA_ESTADO_TRAMITE_FINALIZADO.equals(pvo_Fila.categoriaEstado())
                || Constantes.GRUPO_PANEL_FUERA.equals(pvo_Fila.grupoPanel())
                || pvo_Fila.fechaDesalmacenajePedido() != null) {
            return Constantes.GRUPO_PANEL_FUERA;
        }
        String vlc_Grupo = pvo_Fila.grupoPanel() != null
                ? pvo_Fila.grupoPanel()
                : Constantes.CATEGORIA_ESTADO_PENDIENTE_INGRESO.equals(pvo_Fila.categoriaEstado())
                        ? Constantes.GRUPO_PANEL_POR_LLEGAR
                        : Constantes.GRUPO_PANEL_EN_PROCESO;
        if (Constantes.GRUPO_PANEL_POR_LLEGAR.equals(vlc_Grupo) && pvo_Fila.fechaIngresoPais() != null) {
            return Constantes.GRUPO_PANEL_EN_PROCESO;
        }
        return vlc_Grupo;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Arma el motivo del trámite con los datos que cada tipo ya registra: la
     *   descripción del equipo en los temporales, el tipo de donación, la modalidad de exoneración
     *   o la dependencia del desalmacenaje especial.
     *
     * @param pvo_Fila fila consultada
     * @return motivo legible
     */
    String ResolverMotivo(EntFilaPanelTramite pvo_Fila) {
        String vlc_Codigo = pvo_Fila.codigoTipoTramite() == null ? "" : pvo_Fila.codigoTipoTramite();
        return switch (vlc_Codigo) {
            case Constantes.CODIGO_TIPO_TRAMITE_IMPORTACION_TEMPORAL,
                 Constantes.CODIGO_TIPO_TRAMITE_EXPORTACION_TEMPORAL ->
                    this.ConRespaldo(pvo_Fila.descripcion(), pvo_Fila.tipoTramite());
            case Constantes.CODIGO_TIPO_TRAMITE_DONACION -> pvo_Fila.tipoDonacion() == null
                    ? pvo_Fila.tipoTramite()
                    : "Donación " + NOMBRES_TIPO_DONACION.getOrDefault(pvo_Fila.tipoDonacion(), pvo_Fila.tipoDonacion());
            case Constantes.CODIGO_TIPO_TRAMITE_EXONERACION -> pvo_Fila.tipoExoneracion() == null
                    ? pvo_Fila.tipoTramite()
                    : "Exoneración · " + NOMBRES_TIPO_EXONERACION.getOrDefault(
                            pvo_Fila.tipoExoneracion(), pvo_Fila.tipoExoneracion());
            case Constantes.CODIGO_TIPO_TRAMITE_DESALMACENAJE -> pvo_Fila.origenDependencia() == null
                    ? pvo_Fila.tipoTramite()
                    : "Desalmacenaje especial · " + pvo_Fila.origenDependencia();
            default -> pvo_Fila.tipoTramite();
        };
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Orden de la vista operativa: primero los temporales vencidos o próximos a vencer,
     *   de menos a más días; después el resto, del más antiguo al más reciente.
     *
     * @return comparador de filas
     */
    private Comparator<ModeloTramiteActivoRespuesta> OrdenOperativo() {
        Comparator<ModeloTramiteActivoRespuesta> vlo_PorUrgencia = Comparator.comparing(
                vlo_Tramite -> this.EsUrgente(vlo_Tramite) ? 0 : 1);
        return vlo_PorUrgencia
                .thenComparing(vlo_Tramite -> this.EsUrgente(vlo_Tramite) ? vlo_Tramite.getDiasRestantes() : 0L)
                .thenComparing(ModeloTramiteActivoRespuesta::getFechaRegistro,
                        Comparator.nullsLast(Comparator.naturalOrder()));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Indica si el trámite temporal está vencido o próximo a vencer.
     */
    private boolean EsUrgente(ModeloTramiteActivoRespuesta pvo_Tramite) {
        return pvo_Tramite.getDiasRestantes() != null
                && (Constantes.VENCIMIENTO_PROXIMO.equals(pvo_Tramite.getNivelVencimiento())
                || Constantes.VENCIMIENTO_VENCIDO.equals(pvo_Tramite.getNivelVencimiento()));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Indica si la fila es una importación o exportación temporal.
     */
    private boolean EsTemporal(EntFilaPanelTramite pvo_Fila) {
        return Constantes.CODIGO_TIPO_TRAMITE_IMPORTACION_TEMPORAL.equals(pvo_Fila.codigoTipoTramite())
                || Constantes.CODIGO_TIPO_TRAMITE_EXPORTACION_TEMPORAL.equals(pvo_Fila.codigoTipoTramite());
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Cuenta los trámites de un grupo del panel.
     */
    private int Contar(List<ModeloTramiteActivoRespuesta> pvo_LsTramites, String pvc_Grupo) {
        return (int) pvo_LsTramites.stream().filter(vlo_Tramite -> pvc_Grupo.equals(vlo_Tramite.getGrupoPanel())).count();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Devuelve el texto, o el respaldo si el texto viene vacío.
     */
    private String ConRespaldo(String pvc_Texto, String pvc_Respaldo) {
        return pvc_Texto == null || pvc_Texto.isBlank() ? pvc_Respaldo : pvc_Texto.trim();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Valida el alcance pedido; vacío equivale a la vista activa.
     */
    private String NormalizarAlcance(String pvc_Alcance) {
        if (pvc_Alcance == null || pvc_Alcance.isBlank()) {
            return Constantes.ALCANCE_PANEL_ACTIVOS;
        }
        String vlc_Alcance = pvc_Alcance.trim().toUpperCase();
        if (!Constantes.ALCANCE_PANEL_ACTIVOS.equals(vlc_Alcance)
                && !Constantes.ALCANCE_PANEL_HISTORICO.equals(vlc_Alcance)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ALCANCE_PANEL_INVALIDO);
        }
        return vlc_Alcance;
    }
}
