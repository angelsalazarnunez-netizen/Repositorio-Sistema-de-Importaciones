package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.tramites.DalReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntArchivoExportado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntBloqueReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntFiltroReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntLineaReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntReporteConsultas;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramiteReporte;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.reportes.tramites.Rpt_SIM_ReporteConsultas;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 25/09/2026
 * Descripción: Reglas de negocio del reporte de consultas internas (HU-036). Resume los trámites
 *   que cumplen los filtros: cuenta los expedientes, suma el valor CIF por moneda, cuenta los
 *   permisos regulatorios y desglosa por tipo de trámite y por régimen aduanero. Los montos de
 *   monedas distintas no se suman entre sí, porque el sistema no registra tipo de cambio.
 *
 * Historial de cambios:
 *   25/09/2026 Katherine Susana Valverde Fallas: creación de la clase.
 */
@Service
public class BllReporte {

    /** Criterios de agrupación que admite el reporte. */
    public static final Set<String> AGRUPACIONES = Set.of(
            Constantes.AGRUPACION_TIPO_TRAMITE,
            Constantes.AGRUPACION_REGIMEN_ADUANERO,
            Constantes.AGRUPACION_DEPENDENCIA,
            Constantes.AGRUPACION_PARTIDO_RESPONSABLE,
            Constantes.AGRUPACION_TIPO_EXONERACION,
            Constantes.AGRUPACION_MES);

    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("yyyy-MM");

    /** Separa los valores de los criterios dentro de una línea combinada. */
    private static final String SEPARADOR_COMBINADO = " · ";

    private final DalReporte vgo_DalReporte;
    private final List<Rpt_SIM_ReporteConsultas> vgo_LsFormatos;

    public BllReporte(DalReporte pvo_DalReporte, List<Rpt_SIM_ReporteConsultas> pvo_LsFormatos) {
        this.vgo_DalReporte = pvo_DalReporte;
        this.vgo_LsFormatos = pvo_LsFormatos;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 25/09/2026
     * Descripción: Genera el reporte tabulado de consultas internas con los criterios indicados.
     *
     * @param pvo_Filtro rango de fechas, filtros y criterio de agrupación
     * @return reporte con la cantidad de trámites, los totales y los desgloses
     */
    @Transactional(readOnly = true)
    public EntReporteConsultas Generar(EntFiltroReporte pvo_Filtro) {
        if (pvo_Filtro == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }

        List<String> vlo_LsCriterios = this.DepurarCriterios(pvo_Filtro.getAgrupaciones());
        if (pvo_Filtro.getFechaDesde() != null && pvo_Filtro.getFechaHasta() != null
                && pvo_Filtro.getFechaHasta().isBefore(pvo_Filtro.getFechaDesde())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_RANGO_DE_FECHAS_INVALIDO);
        }

        List<EntTramiteReporte> vlo_LsTramites = this.vgo_DalReporte.SIMT_TRAMITE_ObtenerListaReporte(
                pvo_Filtro,
                Constantes.CODIGO_TIPO_DOCUMENTO_PERMISO_REGULATORIO,
                Constantes.ESTADO_DOCUMENTO_ACTIVO);

        EntReporteConsultas vlo_Reporte = new EntReporteConsultas();
        vlo_Reporte.setFechaGeneracion(LocalDateTime.now());
        vlo_Reporte.setCantidadTramites(vlo_LsTramites.size());
        vlo_Reporte.setIncluirTotalCif(pvo_Filtro.isIncluirTotalCif());
        vlo_Reporte.setIncluirTotalBodegaje(pvo_Filtro.isIncluirTotalBodegaje());
        vlo_Reporte.setCantidadPermisos(vlo_LsTramites.stream()
                .mapToLong(vlo_Tramite -> vlo_Tramite.getCantidadPermisos() == null
                        ? 0L : vlo_Tramite.getCantidadPermisos())
                .sum());
        vlo_Reporte.setBloques(pvo_Filtro.isCombinarCriterios()
                ? List.of(this.CombinarCriterios(vlo_LsTramites, vlo_LsCriterios))
                : this.SepararCriterios(vlo_LsTramites, vlo_LsCriterios));
        if (pvo_Filtro.isEnumerarTramites()) {
            vlo_Reporte.setTramites(vlo_LsTramites);
        }
        vlo_Reporte.setNotas(this.ArmarNotas());
        return vlo_Reporte;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 25/09/2026
     * Descripción: Resume los trámites por el criterio indicado y por moneda, de modo que cada
     *   línea del reporte lleva su cantidad de trámites, su total CIF y sus permisos.
     *
     * @param pvo_LsTramites trámites que cumplen los filtros
     * @param pvo_Clave función que obtiene el grupo de cada trámite
     * @return líneas del reporte, ordenadas por grupo y moneda
     */
    private List<EntLineaReporte> Resumir(
            List<EntTramiteReporte> pvo_LsTramites,
            Function<EntTramiteReporte, String> pvo_Clave) {

        Map<String, EntLineaReporte> vlo_MpLineas = new LinkedHashMap<>();
        for (EntTramiteReporte vlo_Tramite : pvo_LsTramites) {
            String vlc_Grupo = pvo_Clave.apply(vlo_Tramite);
            String vlc_Moneda = vlo_Tramite.getMoneda() == null
                    ? Constantes.MONEDA_NO_DECLARADA : vlo_Tramite.getMoneda();
            EntLineaReporte vlo_Linea = vlo_MpLineas.computeIfAbsent(vlc_Grupo + "|" + vlc_Moneda,
                    vlc_Llave -> {
                        EntLineaReporte vlo_Nueva = new EntLineaReporte();
                        vlo_Nueva.setGrupo(vlc_Grupo);
                        vlo_Nueva.setMoneda(vlc_Moneda);
                        vlo_Nueva.setTotalCif(BigDecimal.ZERO);
                        vlo_Nueva.setTotalImpuestosPagados(BigDecimal.ZERO);
                        vlo_Nueva.setTotalImpuestosExonerados(BigDecimal.ZERO);
                        vlo_Nueva.setTotalBodegaje(BigDecimal.ZERO);
                        return vlo_Nueva;
                    });
            vlo_Linea.setCantidadTramites(vlo_Linea.getCantidadTramites() + 1);
            vlo_Linea.setCantidadPermisos(vlo_Linea.getCantidadPermisos()
                    + (vlo_Tramite.getCantidadPermisos() == null ? 0L : vlo_Tramite.getCantidadPermisos()));
            if (vlo_Tramite.getValorCif() != null) {
                vlo_Linea.setTotalCif(vlo_Linea.getTotalCif().add(vlo_Tramite.getValorCif()));
            }
            if (vlo_Tramite.getImpuestosPagados() != null) {
                vlo_Linea.setTotalImpuestosPagados(
                        vlo_Linea.getTotalImpuestosPagados().add(vlo_Tramite.getImpuestosPagados()));
            }
            if (vlo_Tramite.getImpuestosExonerados() != null) {
                vlo_Linea.setTotalImpuestosExonerados(
                        vlo_Linea.getTotalImpuestosExonerados()
                                .add(vlo_Tramite.getImpuestosExonerados()));
            }
            if (vlo_Tramite.getMontoBodegaje() != null) {
                vlo_Linea.setTotalBodegaje(
                        vlo_Linea.getTotalBodegaje().add(vlo_Tramite.getMontoBodegaje()));
            }
        }

        List<EntLineaReporte> vlo_LsLineas = new ArrayList<>(vlo_MpLineas.values());
        vlo_LsLineas.sort(Comparator.comparing(EntLineaReporte::getGrupo)
                .thenComparing(EntLineaReporte::getMoneda));
        return vlo_LsLineas;
    }

    private Function<EntTramiteReporte, String> ObtenerClave(String pvc_AgruparPor) {
        return switch (pvc_AgruparPor) {
            case Constantes.AGRUPACION_REGIMEN_ADUANERO ->
                    vlo_Tramite -> this.Describir(vlo_Tramite.getRegimenAduanero());
            case Constantes.AGRUPACION_DEPENDENCIA ->
                    vlo_Tramite -> this.Describir(vlo_Tramite.getOrigenDependencia());
            case Constantes.AGRUPACION_PARTIDO_RESPONSABLE ->
                    vlo_Tramite -> this.Describir(vlo_Tramite.getPartidoResponsable());
            case Constantes.AGRUPACION_TIPO_EXONERACION ->
                    vlo_Tramite -> this.Describir(vlo_Tramite.getTipoExoneracion());
            case Constantes.AGRUPACION_MES ->
                    vlo_Tramite -> vlo_Tramite.getFechaCreacion() == null
                            ? Constantes.DATO_NO_REGISTRADO
                            : vlo_Tramite.getFechaCreacion().format(FORMATO_MES);
            default -> vlo_Tramite -> this.Describir(vlo_Tramite.getTipoTramite());
        };
    }

    private String Describir(String pvc_Dato) {
        return pvc_Dato == null || pvc_Dato.isBlank() ? Constantes.DATO_NO_REGISTRADO : pvc_Dato;
    }

    private List<String> ArmarNotas() {
        List<String> vlo_LsNotas = new ArrayList<>();
        vlo_LsNotas.add(Constantes.NOTA_REPORTE_MONEDAS);
        vlo_LsNotas.add(Constantes.NOTA_REPORTE_MONTOS_PENDIENTES);
        return vlo_LsNotas;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Genera el reporte y lo entrega como archivo descargable en el formato
     *   indicado, con el mismo contenido que muestra la pantalla.
     *
     * @param pvo_Filtro criterios con los que se genera el reporte
     * @param pvc_Formato formato pedido: EXCEL o PDF
     * @return archivo con su nombre y su tipo de contenido
     */
    @Transactional(readOnly = true)
    public EntArchivoExportado Exportar(EntFiltroReporte pvo_Filtro, String pvc_Formato) {
        String vlc_Formato = pvc_Formato == null ? "" : pvc_Formato.trim().toUpperCase();
        Rpt_SIM_ReporteConsultas vlo_Generador = this.vgo_LsFormatos.stream()
                .filter(vlo_Opcion -> vlo_Opcion.getFormato().equals(vlc_Formato))
                .findFirst()
                .orElseThrow(() -> new ExcepcionNegocio(
                        Constantes.MENSAJE_FORMATO_EXPORTACION_INVALIDO));

        EntReporteConsultas vlo_Reporte = this.Generar(pvo_Filtro);
        String vlc_Nombre = Constantes.NOMBRE_ARCHIVO_REPORTE + "-"
                + LocalDate.now() + "." + vlo_Generador.getExtension();
        return new EntArchivoExportado(vlc_Nombre, vlo_Generador.getTipoContenido(),
                vlo_Generador.Generar(vlo_Reporte));
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Revisa los criterios de agrupación pedidos, descarta los repetidos y
     *   conserva su orden. Cuando no se indica ninguno, el reporte se agrupa por tipo de
     *   trámite, que es la consulta más frecuente.
     *
     * @param pvo_LsCriterios criterios pedidos
     * @return criterios válidos, sin repetir
     */
    private List<String> DepurarCriterios(List<String> pvo_LsCriterios) {
        List<String> vlo_LsDepurados = new ArrayList<>();
        for (String vlo_Criterio : pvo_LsCriterios == null ? List.<String>of() : pvo_LsCriterios) {
            String vlc_Criterio = vlo_Criterio == null ? "" : vlo_Criterio.trim().toUpperCase();
            if (vlc_Criterio.isEmpty()) {
                continue;
            }
            if (!AGRUPACIONES.contains(vlc_Criterio)) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_AGRUPACION_INVALIDA);
            }
            if (!vlo_LsDepurados.contains(vlc_Criterio)) {
                vlo_LsDepurados.add(vlc_Criterio);
            }
        }
        if (vlo_LsDepurados.isEmpty()) {
            vlo_LsDepurados.add(Constantes.AGRUPACION_TIPO_TRAMITE);
        }
        return vlo_LsDepurados;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Da el título con el que se presenta el desglose de un criterio, tal como se
     *   lee en la pantalla y en el archivo descargado.
     *
     * @param pvc_Criterio criterio de agrupación
     * @return título del desglose
     */
    private String DescribirCriterio(String pvc_Criterio) {
        return switch (pvc_Criterio) {
            case Constantes.AGRUPACION_REGIMEN_ADUANERO -> "Desglose por régimen aduanero";
            case Constantes.AGRUPACION_PARTIDO_RESPONSABLE -> "Desglose por partido responsable del costo";
            case Constantes.AGRUPACION_DEPENDENCIA -> "Desglose por dependencia de origen";
            case Constantes.AGRUPACION_TIPO_EXONERACION -> "Desglose por tipo de exoneración";
            case Constantes.AGRUPACION_MES -> "Desglose por mes de registro";
            default -> "Desglose por tipo de trámite";
        };
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Obtiene los partidos responsables del costo que los expedientes tienen
     *   registrados, para ofrecerlos como filtro del reporte.
     *
     * @return partidos registrados, en orden alfabético
     */
    @Transactional(readOnly = true)
    public List<String> ObtenerPartidosResponsables() {
        return this.vgo_DalReporte.SIMT_TRAMITE_ObtenerPartidosResponsables();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Arma un bloque por cada criterio, que es la forma en la que el reporte se
     *   lee cuando se quiere revisar un criterio a la vez.
     *
     * @param pvo_LsTramites trámites que cumplen los filtros
     * @param pvo_LsCriterios criterios de agrupación pedidos
     * @return un bloque por criterio
     */
    private List<EntBloqueReporte> SepararCriterios(
            List<EntTramiteReporte> pvo_LsTramites,
            List<String> pvo_LsCriterios) {

        List<EntBloqueReporte> vlo_LsBloques = new ArrayList<>();
        for (String vlo_Criterio : pvo_LsCriterios) {
            List<EntLineaReporte> vlo_LsLineas =
                    this.Resumir(pvo_LsTramites, this.ObtenerClave(vlo_Criterio));
            EntBloqueReporte vlo_Bloque = new EntBloqueReporte(vlo_Criterio,
                    this.DescribirCriterio(vlo_Criterio), vlo_LsLineas);
            vlo_Bloque.setEncabezados(List.of(this.DescribirColumna(vlo_Criterio)));
            vlo_LsBloques.add(vlo_Bloque);
        }
        return vlo_LsBloques;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Arma un solo bloque que cruza todos los criterios elegidos: cada línea es una
     *   combinación de sus valores, con una columna por criterio. Sirve para leer de un vistazo,
     *   por ejemplo, cuánto suma cada tipo de trámite dentro de cada régimen aduanero.
     *
     * @param pvo_LsTramites trámites que cumplen los filtros
     * @param pvo_LsCriterios criterios de agrupación pedidos
     * @return bloque único con las combinaciones
     */
    private EntBloqueReporte CombinarCriterios(
            List<EntTramiteReporte> pvo_LsTramites,
            List<String> pvo_LsCriterios) {

        List<Function<EntTramiteReporte, String>> vlo_LsClaves = pvo_LsCriterios.stream()
                .map(this::ObtenerClave)
                .toList();
        List<EntLineaReporte> vlo_LsLineas = this.Resumir(pvo_LsTramites, vlo_Tramite ->
                vlo_LsClaves.stream()
                        .map(vlo_Clave -> vlo_Clave.apply(vlo_Tramite))
                        .collect(Collectors.joining(SEPARADOR_COMBINADO)));
        vlo_LsLineas.forEach(vlo_Linea ->
                vlo_Linea.setValores(List.of(vlo_Linea.getGrupo().split(SEPARADOR_COMBINADO, -1))));

        EntBloqueReporte vlo_Bloque = new EntBloqueReporte(String.join(",", pvo_LsCriterios),
                "Desglose combinado", vlo_LsLineas);
        vlo_Bloque.setEncabezados(pvo_LsCriterios.stream().map(this::DescribirColumna).toList());
        return vlo_Bloque;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Da el nombre de la columna con la que se presenta un criterio en la tabla.
     *
     * @param pvc_Criterio criterio de agrupación
     * @return nombre de la columna
     */
    private String DescribirColumna(String pvc_Criterio) {
        return switch (pvc_Criterio) {
            case Constantes.AGRUPACION_REGIMEN_ADUANERO -> "Régimen aduanero";
            case Constantes.AGRUPACION_PARTIDO_RESPONSABLE -> "Partido responsable";
            case Constantes.AGRUPACION_DEPENDENCIA -> "Dependencia de origen";
            case Constantes.AGRUPACION_TIPO_EXONERACION -> "Tipo de exoneración";
            case Constantes.AGRUPACION_MES -> "Mes de registro";
            default -> "Tipo de trámite";
        };
    }
}
