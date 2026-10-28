package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.tramites.DalMoneda;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntArchivoExportado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntFiltroBusquedaTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntListadoTramites;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMoneda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntOpcionCatalogo;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntOpcionesBusquedaTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResultadoBusquedaTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.reportes.tramites.Rpt_SIM_ListadoTramites;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Reglas de negocio de la búsqueda de trámites (HU-034). Normaliza y valida los
 *   criterios, obtiene el listado desde la base de datos sin modificarla y lo exporta en Excel, PDF
 *   o Word con exactamente los mismos criterios y resultados que muestra la pantalla.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 30/09/2026
 *   Descripción: las monedas se obtienen con la consulta de monedas activas que agregó la HU-006,
 *     en lugar de una consulta propia equivalente.
 */
@Service
public class BllBusquedaTramite {

    private static final Map<String, String> CATEGORIAS_ESTADO = Map.of(
            Constantes.CATEGORIA_ESTADO_PENDIENTE_INGRESO, Constantes.NOMBRE_CATEGORIA_PENDIENTE_INGRESO,
            Constantes.CATEGORIA_ESTADO_TRAMITE_ACTIVO, Constantes.NOMBRE_CATEGORIA_TRAMITE_ACTIVO,
            Constantes.CATEGORIA_ESTADO_TRAMITE_FINALIZADO, Constantes.NOMBRE_CATEGORIA_TRAMITE_FINALIZADO);
    private static final List<String> ORDEN_CATEGORIAS_ESTADO = List.of(
            Constantes.CATEGORIA_ESTADO_PENDIENTE_INGRESO,
            Constantes.CATEGORIA_ESTADO_TRAMITE_ACTIVO,
            Constantes.CATEGORIA_ESTADO_TRAMITE_FINALIZADO);
    private static final Map<String, String> TIPOS_EXONERACION = Map.of(
            Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL, Constantes.NOMBRE_TIPO_EXONERACION_COMPRA_INSTITUCIONAL,
            Constantes.TIPO_EXONERACION_DONACION, Constantes.NOMBRE_TIPO_EXONERACION_DONACION,
            Constantes.TIPO_EXONERACION_CLIENTE_EXTERNO, Constantes.NOMBRE_TIPO_EXONERACION_CLIENTE_EXTERNO);
    private static final List<String> ORDEN_TIPOS_EXONERACION = List.of(
            Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL,
            Constantes.TIPO_EXONERACION_DONACION,
            Constantes.TIPO_EXONERACION_CLIENTE_EXTERNO);
    private static final List<String> ORDEN_FORMATOS_EXPORTACION = List.of(
            Constantes.FORMATO_EXPORTACION_EXCEL,
            Constantes.FORMATO_EXPORTACION_PDF,
            Constantes.FORMATO_EXPORTACION_WORD);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_NOMBRE_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private final DalTramite vgo_DalTramite;
    private final DalTipoTramite vgo_DalTipoTramite;
    private final DalMoneda vgo_DalMoneda;
    private final Map<String, Rpt_SIM_ListadoTramites> vgo_MpReportes;

    public BllBusquedaTramite(
            DalTramite pvo_DalTramite,
            DalTipoTramite pvo_DalTipoTramite,
            DalMoneda pvo_DalMoneda,
            List<Rpt_SIM_ListadoTramites> pvo_LsReportes) {
        this.vgo_DalTramite = pvo_DalTramite;
        this.vgo_DalTipoTramite = pvo_DalTipoTramite;
        this.vgo_DalMoneda = pvo_DalMoneda;
        this.vgo_MpReportes = new LinkedHashMap<>();
        pvo_LsReportes.forEach(vlo_Reporte -> this.vgo_MpReportes.put(vlo_Reporte.getFormato(), vlo_Reporte));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Obtiene los trámites que cumplen los criterios indicados. Los criterios vacíos no
     *   restringen la búsqueda; si no se indica ninguno, se listan todos los trámites.
     *
     * @param pvo_Filtro criterios de la búsqueda
     * @return trámites encontrados, del más reciente al más antiguo
     */
    @Transactional(readOnly = true)
    public List<EntResultadoBusquedaTramite> BuscarTramites(EntFiltroBusquedaTramite pvo_Filtro) {
        EntFiltroBusquedaTramite vlo_Filtro = this.NormalizarFiltro(pvo_Filtro);
        this.ValidarFiltro(vlo_Filtro);
        List<EntResultadoBusquedaTramite> vlo_LsResultados = this.vgo_DalTramite.SIMT_TRAMITE_ObtenerListaBusqueda(vlo_Filtro);
        vlo_LsResultados.forEach(vlo_Resultado -> {
            vlo_Resultado.setCategoriaEstadoNombre(this.NombreDeCodigo(CATEGORIAS_ESTADO, vlo_Resultado.getCategoriaEstado()));
            vlo_Resultado.setTipoExoneracionNombre(this.NombreDeCodigo(TIPOS_EXONERACION, vlo_Resultado.getTipoExoneracion()));
        });
        return vlo_LsResultados;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Obtiene el nombre de un código. Un trámite que no declara el dato, como el tipo de
     *   exoneración de una donación, no tiene nombre; un código sin nombre conocido se muestra tal cual.
     *
     * @param pvo_MpNombres nombres por código
     * @param pvc_Codigo código a nombrar, puede ser null
     * @return nombre del código o null si el trámite no declara el dato
     */
    private String NombreDeCodigo(Map<String, String> pvo_MpNombres, String pvc_Codigo) {
        return pvc_Codigo == null ? null : pvo_MpNombres.getOrDefault(pvc_Codigo, pvc_Codigo);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Obtiene los valores que ofrece cada filtro de la búsqueda, las columnas del
     *   listado y los formatos de exportación disponibles.
     *
     * @return opciones de la pantalla de búsqueda
     */
    @Transactional(readOnly = true)
    public EntOpcionesBusquedaTramite ObtenerOpciones() {
        EntOpcionesBusquedaTramite vlo_Opciones = new EntOpcionesBusquedaTramite();
        vlo_Opciones.setTiposTramite(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerLista(Constantes.ESTADO_CATALOGO_ACTIVO));
        vlo_Opciones.setCategoriasEstado(this.CrearOpciones(ORDEN_CATEGORIAS_ESTADO, CATEGORIAS_ESTADO));
        vlo_Opciones.setMonedas(this.vgo_DalMoneda.SIMM_MONEDA_ObtenerActivas());
        vlo_Opciones.setRegimenesAduaneros(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerRegimenesAduaneros());
        vlo_Opciones.setTiposExoneracion(this.CrearOpciones(ORDEN_TIPOS_EXONERACION, TIPOS_EXONERACION));
        vlo_Opciones.setAduanasControl(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerAduanasControl());
        vlo_Opciones.setColumnas(Rpt_SIM_ListadoTramites.ObtenerColumnas());
        vlo_Opciones.setFormatosExportacion(ORDEN_FORMATOS_EXPORTACION.stream()
                .map(this.vgo_MpReportes::get)
                .filter(Objects::nonNull)
                .map(vlo_Reporte -> new EntOpcionCatalogo(vlo_Reporte.getFormato(), vlo_Reporte.getNombreFormato()))
                .toList());
        return vlo_Opciones;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Exporta el listado de trámites en el formato indicado. Ejecuta la misma búsqueda
     *   que la pantalla, con los mismos criterios, e incluye esos criterios en el archivo.
     *
     * @param pvo_Filtro criterios de la búsqueda
     * @param pvc_Formato formato del archivo: EXCEL, PDF o WORD
     * @return archivo generado con su nombre y tipo de contenido
     */
    @Transactional(readOnly = true)
    public EntArchivoExportado ExportarTramites(EntFiltroBusquedaTramite pvo_Filtro, String pvc_Formato) {
        Rpt_SIM_ListadoTramites vlo_Reporte = pvc_Formato == null
                ? null
                : this.vgo_MpReportes.get(pvc_Formato.trim().toUpperCase(Locale.ROOT));
        if (vlo_Reporte == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_FORMATO_EXPORTACION_INVALIDO);
        }

        EntFiltroBusquedaTramite vlo_Filtro = this.NormalizarFiltro(pvo_Filtro);
        List<EntResultadoBusquedaTramite> vlo_LsResultados = this.BuscarTramites(vlo_Filtro);
        LocalDateTime vld_Ahora = LocalDateTime.now();
        EntListadoTramites vlo_Listado = new EntListadoTramites(
                Constantes.TITULO_LISTADO_TRAMITES,
                vld_Ahora,
                this.DescribirCriterios(vlo_Filtro),
                Rpt_SIM_ListadoTramites.ObtenerColumnas(),
                vlo_LsResultados);

        String vlc_NombreArchivo = Constantes.NOMBRE_ARCHIVO_LISTADO_TRAMITES + "-"
                + vld_Ahora.format(FORMATO_NOMBRE_ARCHIVO) + "." + vlo_Reporte.getExtension();
        return new EntArchivoExportado(vlc_NombreArchivo, vlo_Reporte.getTipoContenido(), vlo_Reporte.Generar(vlo_Listado));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Quita espacios, valores vacíos y repetidos de los criterios, y pasa los códigos a
     *   mayúsculas, para que un criterio sin valores equivalga a no filtrar.
     *
     * @param pvo_Filtro criterios recibidos
     * @return criterios normalizados
     */
    private EntFiltroBusquedaTramite NormalizarFiltro(EntFiltroBusquedaTramite pvo_Filtro) {
        EntFiltroBusquedaTramite vlo_Filtro = new EntFiltroBusquedaTramite();
        if (pvo_Filtro == null) {
            return vlo_Filtro;
        }
        vlo_Filtro.setFechaDesde(pvo_Filtro.getFechaDesde());
        vlo_Filtro.setFechaHasta(pvo_Filtro.getFechaHasta());
        String vlc_Referencia = pvo_Filtro.getReferenciaInterna();
        vlo_Filtro.setReferenciaInterna(vlc_Referencia == null || vlc_Referencia.isBlank() ? null : vlc_Referencia.trim());
        vlo_Filtro.setIdTiposTramite(this.SinRepetidos(pvo_Filtro.getIdTiposTramite()));
        vlo_Filtro.setIdMonedas(this.SinRepetidos(pvo_Filtro.getIdMonedas()));
        vlo_Filtro.setCategoriasEstado(this.NormalizarTextos(pvo_Filtro.getCategoriasEstado(), true));
        vlo_Filtro.setTiposExoneracion(this.NormalizarTextos(pvo_Filtro.getTiposExoneracion(), true));
        vlo_Filtro.setRegimenesAduaneros(this.NormalizarTextos(pvo_Filtro.getRegimenesAduaneros(), false));
        vlo_Filtro.setAduanasControl(this.NormalizarTextos(pvo_Filtro.getAduanasControl(), false));
        return vlo_Filtro;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Valida que el periodo sea coherente y que los códigos correspondan a valores
     *   admitidos por el sistema.
     *
     * @param pvo_Filtro criterios normalizados
     */
    private void ValidarFiltro(EntFiltroBusquedaTramite pvo_Filtro) {
        if (pvo_Filtro.getFechaDesde() != null && pvo_Filtro.getFechaHasta() != null
                && pvo_Filtro.getFechaDesde().isAfter(pvo_Filtro.getFechaHasta())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_RANGO_FECHAS_INVALIDO);
        }
        if (pvo_Filtro.getReferenciaInterna() != null
                && pvo_Filtro.getReferenciaInterna().length() > Constantes.LONGITUD_MAXIMA_REFERENCIA_INTERNA) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_BUSQUEDA_INVALIDA);
        }
        if (!CATEGORIAS_ESTADO.keySet().containsAll(pvo_Filtro.getCategoriasEstado())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_CATEGORIA_ESTADO_INVALIDA);
        }
        if (!TIPOS_EXONERACION.keySet().containsAll(pvo_Filtro.getTiposExoneracion())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_EXONERACION_INVALIDO);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Describe en texto los criterios aplicados, con los nombres de catálogo en lugar de
     *   sus identificadores, para encabezar el archivo exportado.
     *
     * @param pvo_Filtro criterios normalizados
     * @return un texto por criterio aplicado
     */
    private List<String> DescribirCriterios(EntFiltroBusquedaTramite pvo_Filtro) {
        List<String> vlo_LsCriterios = new ArrayList<>();
        if (pvo_Filtro.getFechaDesde() != null || pvo_Filtro.getFechaHasta() != null) {
            vlo_LsCriterios.add("Periodo: "
                    + (pvo_Filtro.getFechaDesde() == null ? "sin inicio" : pvo_Filtro.getFechaDesde().format(FORMATO_FECHA))
                    + " al "
                    + (pvo_Filtro.getFechaHasta() == null ? "sin fin" : pvo_Filtro.getFechaHasta().format(FORMATO_FECHA)));
        }
        if (pvo_Filtro.getReferenciaInterna() != null) {
            vlo_LsCriterios.add("Id de trámite: " + pvo_Filtro.getReferenciaInterna());
        }
        if (!pvo_Filtro.getIdTiposTramite().isEmpty()) {
            Map<Integer, String> vlo_MpNombres = this.vgo_DalTipoTramite
                    .SIMM_TIPO_TRAMITE_ObtenerLista(Constantes.ESTADO_CATALOGO_ACTIVO).stream()
                    .collect(Collectors.toMap(EntTipoTramite::getIdTipoTramite, EntTipoTramite::getNombre));
            vlo_LsCriterios.add("Tipo de contratación: "
                    + this.Nombrar(pvo_Filtro.getIdTiposTramite(), vlo_MpNombres::get));
        }
        if (!pvo_Filtro.getCategoriasEstado().isEmpty()) {
            vlo_LsCriterios.add("Estado: " + this.Nombrar(pvo_Filtro.getCategoriasEstado(), CATEGORIAS_ESTADO::get));
        }
        if (!pvo_Filtro.getIdMonedas().isEmpty()) {
            Map<Integer, String> vlo_MpCodigos = this.vgo_DalMoneda
                    .SIMM_MONEDA_ObtenerActivas().stream()
                    .collect(Collectors.toMap(EntMoneda::getIdMoneda, EntMoneda::getCodigoIso));
            vlo_LsCriterios.add("Moneda: " + this.Nombrar(pvo_Filtro.getIdMonedas(), vlo_MpCodigos::get));
        }
        if (!pvo_Filtro.getRegimenesAduaneros().isEmpty()) {
            vlo_LsCriterios.add("Régimen aduanero: " + String.join(", ", pvo_Filtro.getRegimenesAduaneros()));
        }
        if (!pvo_Filtro.getTiposExoneracion().isEmpty()) {
            vlo_LsCriterios.add("Tipo de exoneración: "
                    + this.Nombrar(pvo_Filtro.getTiposExoneracion(), TIPOS_EXONERACION::get));
        }
        if (!pvo_Filtro.getAduanasControl().isEmpty()) {
            vlo_LsCriterios.add("Aduana de control: " + String.join(", ", pvo_Filtro.getAduanasControl()));
        }
        if (vlo_LsCriterios.isEmpty()) {
            vlo_LsCriterios.add("Criterios: todos los trámites");
        }
        return vlo_LsCriterios;
    }

    private <T> String Nombrar(List<T> pvo_LsValores, Function<T, String> pvo_Nombre) {
        return pvo_LsValores.stream()
                .map(vlo_Valor -> Objects.requireNonNullElse(pvo_Nombre.apply(vlo_Valor), String.valueOf(vlo_Valor)))
                .collect(Collectors.joining(", "));
    }

    private List<EntOpcionCatalogo> CrearOpciones(List<String> pvo_LsCodigos, Map<String, String> pvo_MpNombres) {
        return pvo_LsCodigos.stream()
                .map(vlc_Codigo -> new EntOpcionCatalogo(vlc_Codigo, pvo_MpNombres.get(vlc_Codigo)))
                .toList();
    }

    private List<Integer> SinRepetidos(List<Integer> pvo_LsValores) {
        return new ArrayList<>(pvo_LsValores.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private List<String> NormalizarTextos(List<String> pvo_LsValores, boolean pvb_EsCodigo) {
        return new ArrayList<>(pvo_LsValores.stream()
                .filter(vlc_Valor -> vlc_Valor != null && !vlc_Valor.isBlank())
                .map(vlc_Valor -> pvb_EsCodigo ? vlc_Valor.trim().toUpperCase(Locale.ROOT) : vlc_Valor.trim())
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }
}
