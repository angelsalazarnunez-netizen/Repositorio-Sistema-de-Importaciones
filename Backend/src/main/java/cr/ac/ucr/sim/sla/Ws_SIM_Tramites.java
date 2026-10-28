package cr.ac.ucr.sim.sla;

import cr.ac.ucr.sim.seguridad.ReglasAcceso;
import cr.ac.ucr.sim.seguridad.SrvAutorizacion;
import org.springframework.security.access.prepost.PreAuthorize;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntArchivoExportado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCambioEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCambioReferencia;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCambioEstadoDonacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntComentarioBitacora;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCorreccionDesalmacenaje;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCorreccionExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCostosTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoDocumental;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracionDetalle;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMovimientoBitacora;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaCambioReferencia;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExpedienteDetalle;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExpedienteDonacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExpedienteResumen;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntAgenteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntFiltroBusquedaTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDatosTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExpedienteTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntOpcionesTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntProrrogaTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntOpcionesBusquedaTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroDesalmacenaje;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroDonacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenAlertaTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaDesalmacenaje;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaDonacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResultadoBusquedaTramite;
import cr.ac.ucr.sim.logicanegocio.tramites.BllBusquedaTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCatalogosPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDetallePedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntNotificacionPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntFiltroReporte;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntReporteConsultas;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.logicanegocio.tramites.BllCatalogo;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenDonacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenExoneracion;
import cr.ac.ucr.sim.logicanegocio.tramites.BllDesalmacenajeEspecial;
import cr.ac.ucr.sim.logicanegocio.tramites.BllDonacion;
import cr.ac.ucr.sim.logicanegocio.tramites.BllExoneracion;
import cr.ac.ucr.sim.logicanegocio.tramites.BllPedidoImportacion;
import cr.ac.ucr.sim.logicanegocio.tramites.BllReporte;
import cr.ac.ucr.sim.logicanegocio.tramites.BllTramite;
import cr.ac.ucr.sim.logicanegocio.tramites.BllTramiteTemporal;
import cr.ac.ucr.sim.logicanegocio.tramites.BllAlertaTemporal;
import cr.ac.ucr.sim.utilerias.Enumerados.TipoTramiteTemporal;
import cr.ac.ucr.sim.seguridad.UsuarioAutenticado;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Servicio REST del módulo de trámites de importación. Expone el registro del
 *   desalmacenaje especial (HU-031), los pedidos de importación (HU-006) y sus seguimientos.
 *   No contiene reglas de negocio: delega en la capa Bll.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 10/09/2026
 *   Descripción: se expone el registro de donaciones (HU-024).
 *
 *   Responsable: Katherine Susana Valverde Fallas
 *   Fecha: 15/09/2026
 *   Descripción: se exponen el registro, el expediente, la documentación y la bitácora de la
 *     solicitud de exoneración (HU-018).
 *   Responsable: Katherine Susana Valverde Fallas
 *   Fecha: 15/09/2026
 *   Descripción: se exponen los estados disponibles y el cambio de estado del desalmacenaje
 *     especial, para su seguimiento (HU-032).
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 16/09/2026
 *   Descripción: se exponen la búsqueda de trámites por criterios combinables, las opciones de sus
 *     filtros y la exportación del listado en Excel, PDF y Word (HU-034).
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 15/09/2026
 *   Descripción: se expone la gestión de donaciones (HU-044): lista de expedientes, consulta del
 *     expediente, catálogo de estados, cambio de estado, documentación adicional y descarga.
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: se expone la eliminación de documentos del expediente de donación (HU-044).
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: se expone la importación temporal (HU-025): registro, lista de trámites activos,
 *     opciones, expediente, cambio de estado, datos de aduana, prórroga y documentación. Las
 *     alertas de vencimiento (HU-027) se atienden con BllAlertaTemporal.
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: se expone la exportación temporal (HU-026) con los mismos servicios de la
 *     importación temporal: el primer segmento de la ruta (importaciones-temporales o
 *     exportaciones-temporales) indica el tipo de trámite.
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 18/10/2026
 *   Descripción: el contrato SICOP de la exoneración es opcional y los permisos regulatorios
 *     se reemplazan por el expediente SICOP, la autorización EXONET, el DUA de aplicación, el
 *     comprobante de impuestos y otros documentos (comentarios de la Unidad).
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003). Cada servicio declara su permiso con
 *     @PreAuthorize (ver ReglasAcceso). La búsqueda, sus opciones y su exportación muestran solo
 *     los tipos de trámite que el usuario puede consultar (SrvAutorizacion); el cambio de
 *     referencia, los costos y la descarga de documentos exigen el permiso del tipo del trámite.
 */
@RestController
@RequestMapping("/api/v1/tramites")
public class Ws_SIM_Tramites {

    // Ruta de los trámites temporales: el segmento indica si es una importación o una exportación.
    private static final String RUTA_TRAMITE_TEMPORAL =
            "/{pvc_Tipo:importaciones-temporales|exportaciones-temporales}";

    private final BllDesalmacenajeEspecial vgo_BllDesalmacenajeEspecial;
    private final BllDonacion vgo_BllDonacion;
    private final BllExoneracion vgo_BllExoneracion;
    private final BllTramite vgo_BllTramite;
    private final BllBusquedaTramite vgo_BllBusquedaTramite;
    private final BllPedidoImportacion vgo_BllPedidoImportacion;
    private final BllCatalogo vgo_BllCatalogo;
    private final BllReporte vgo_BllReporte;
    private final BllTramiteTemporal vgo_BllTramiteTemporal;
    private final BllAlertaTemporal vgo_BllAlertaTemporal;
    private final SrvAutorizacion vgo_SrvAutorizacion;

    public Ws_SIM_Tramites(
            BllDesalmacenajeEspecial pvo_BllDesalmacenajeEspecial,
            BllDonacion pvo_BllDonacion,
            BllExoneracion pvo_BllExoneracion,
            BllTramite pvo_BllTramite,
            BllBusquedaTramite pvo_BllBusquedaTramite,
            BllPedidoImportacion pvo_BllPedidoImportacion,
            BllCatalogo pvo_BllCatalogo,
            BllReporte pvo_BllReporte,
            BllTramiteTemporal pvo_BllTramiteTemporal,
            BllAlertaTemporal pvo_BllAlertaTemporal,
            SrvAutorizacion pvo_SrvAutorizacion) {
        this.vgo_BllDesalmacenajeEspecial = pvo_BllDesalmacenajeEspecial;
        this.vgo_BllDonacion = pvo_BllDonacion;
        this.vgo_BllExoneracion = pvo_BllExoneracion;
        this.vgo_BllTramite = pvo_BllTramite;
        this.vgo_BllBusquedaTramite = pvo_BllBusquedaTramite;
        this.vgo_BllPedidoImportacion = pvo_BllPedidoImportacion;
        this.vgo_BllCatalogo = pvo_BllCatalogo;
        this.vgo_BllReporte = pvo_BllReporte;
        this.vgo_BllTramiteTemporal = pvo_BllTramiteTemporal;
        this.vgo_BllAlertaTemporal = pvo_BllAlertaTemporal;
        this.vgo_SrvAutorizacion = pvo_SrvAutorizacion;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Busca trámites de cualquier tipo por uno o varios criterios combinables. Los
     *   criterios de lista admiten varios valores repitiendo el parámetro, por ejemplo
     *   ?categoriasEstado=PEN&categoriasEstado=ACT. Un criterio omitido no restringe la búsqueda.
     *
     * @param pvd_FechaDesde fecha inicial del periodo de registro, en formato AAAA-MM-DD
     * @param pvd_FechaHasta fecha final del periodo de registro, incluida, en formato AAAA-MM-DD
     * @param pvc_ReferenciaInterna texto contenido en el identificador del trámite
     * @param pvo_LsIdTiposTramite tipos de contratación (tipos de trámite)
     * @param pvo_LsCategoriasEstado estados: PEN, ACT o FIN
     * @param pvo_LsIdMonedas monedas
     * @param pvo_LsRegimenesAduaneros regímenes aduaneros
     * @param pvo_LsTiposExoneracion tipos de exoneración
     * @param pvo_LsAduanasControl aduanas de control
     * @return trámites encontrados
     */
    @PreAuthorize(ReglasAcceso.CONSULTA_TRAMITES)
    @GetMapping("/busqueda")
    public ResponseEntity<List<EntResultadoBusquedaTramite>> SIMT_TRAMITE_ObtenerListaBusqueda(
            @RequestParam(value = "fechaDesde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pvd_FechaDesde,
            @RequestParam(value = "fechaHasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pvd_FechaHasta,
            @RequestParam(value = "referenciaInterna", required = false) String pvc_ReferenciaInterna,
            @RequestParam(value = "tiposTramite", required = false) List<Integer> pvo_LsIdTiposTramite,
            @RequestParam(value = "categoriasEstado", required = false) List<String> pvo_LsCategoriasEstado,
            @RequestParam(value = "monedas", required = false) List<Integer> pvo_LsIdMonedas,
            @RequestParam(value = "regimenesAduaneros", required = false) List<String> pvo_LsRegimenesAduaneros,
            @RequestParam(value = "tiposExoneracion", required = false) List<String> pvo_LsTiposExoneracion,
            @RequestParam(value = "aduanasControl", required = false) List<String> pvo_LsAduanasControl) {
        EntFiltroBusquedaTramite vlo_Filtro = this.CrearFiltroBusqueda(pvd_FechaDesde, pvd_FechaHasta,
                pvc_ReferenciaInterna, pvo_LsIdTiposTramite, pvo_LsCategoriasEstado, pvo_LsIdMonedas,
                pvo_LsRegimenesAduaneros, pvo_LsTiposExoneracion, pvo_LsAduanasControl);
        return ResponseEntity.ok(this.vgo_BllBusquedaTramite.BuscarTramites(vlo_Filtro));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Obtiene los valores de los filtros de la búsqueda de trámites, las columnas del
     *   listado y los formatos de exportación disponibles.
     *
     * @return opciones de la pantalla de búsqueda
     */
    @PreAuthorize(ReglasAcceso.CONSULTA_TRAMITES)
    @GetMapping("/busqueda/opciones")
    public ResponseEntity<EntOpcionesBusquedaTramite> SIMT_TRAMITE_ObtenerOpcionesBusqueda() {
        EntOpcionesBusquedaTramite vlo_Opciones = this.vgo_BllBusquedaTramite.ObtenerOpciones();
        // Solo se ofrecen los tipos de trámite que el usuario puede consultar (HU-003).
        if (vlo_Opciones.getTiposTramite() != null) {
            vlo_Opciones.setTiposTramite(vlo_Opciones.getTiposTramite()
                    .stream()
                    .filter(vlo_Tipo -> this.vgo_SrvAutorizacion.PuedeConsultarTipo(vlo_Tipo.getCodigo()))
                    .toList());
        }
        return ResponseEntity.ok(vlo_Opciones);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Exporta el listado de la búsqueda de trámites con los mismos criterios de la
     *   búsqueda, en el formato indicado.
     *
     * @param pvc_Formato formato del archivo: EXCEL, PDF o WORD
     * @param pvd_FechaDesde fecha inicial del periodo de registro, en formato AAAA-MM-DD
     * @param pvd_FechaHasta fecha final del periodo de registro, incluida, en formato AAAA-MM-DD
     * @param pvc_ReferenciaInterna texto contenido en el identificador del trámite
     * @param pvo_LsIdTiposTramite tipos de contratación (tipos de trámite)
     * @param pvo_LsCategoriasEstado estados: PEN, ACT o FIN
     * @param pvo_LsIdMonedas monedas
     * @param pvo_LsRegimenesAduaneros regímenes aduaneros
     * @param pvo_LsTiposExoneracion tipos de exoneración
     * @param pvo_LsAduanasControl aduanas de control
     * @return archivo del listado para su descarga
     */
    @PreAuthorize(ReglasAcceso.CONSULTA_TRAMITES)
    @GetMapping("/busqueda/exportar/{pvc_Formato}")
    public ResponseEntity<byte[]> SIMT_TRAMITE_ExportarListaBusqueda(
            @PathVariable("pvc_Formato") String pvc_Formato,
            @RequestParam(value = "fechaDesde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pvd_FechaDesde,
            @RequestParam(value = "fechaHasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pvd_FechaHasta,
            @RequestParam(value = "referenciaInterna", required = false) String pvc_ReferenciaInterna,
            @RequestParam(value = "tiposTramite", required = false) List<Integer> pvo_LsIdTiposTramite,
            @RequestParam(value = "categoriasEstado", required = false) List<String> pvo_LsCategoriasEstado,
            @RequestParam(value = "monedas", required = false) List<Integer> pvo_LsIdMonedas,
            @RequestParam(value = "regimenesAduaneros", required = false) List<String> pvo_LsRegimenesAduaneros,
            @RequestParam(value = "tiposExoneracion", required = false) List<String> pvo_LsTiposExoneracion,
            @RequestParam(value = "aduanasControl", required = false) List<String> pvo_LsAduanasControl) {
        EntFiltroBusquedaTramite vlo_Filtro = this.CrearFiltroBusqueda(pvd_FechaDesde, pvd_FechaHasta,
                pvc_ReferenciaInterna, pvo_LsIdTiposTramite, pvo_LsCategoriasEstado, pvo_LsIdMonedas,
                pvo_LsRegimenesAduaneros, pvo_LsTiposExoneracion, pvo_LsAduanasControl);
        EntArchivoExportado vlo_Archivo = this.vgo_BllBusquedaTramite.ExportarTramites(vlo_Filtro, pvc_Formato);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(vlo_Archivo.getNombreArchivo(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentType(MediaType.parseMediaType(vlo_Archivo.getTipoContenido()))
                .body(vlo_Archivo.getContenido());
    }

    @PreAuthorize(ReglasAcceso.IMPORTACION_CREAR)
    @PostMapping("/importaciones")
    public ResponseEntity<EntRespuestaPedidoImportacion> SIMT_PEDIDO_PROV_InsertarRegistro(
            @Valid @RequestBody EntRegistroPedidoImportacion pvo_Registro,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        EntRespuestaPedidoImportacion vlo_Respuesta = this.vgo_BllPedidoImportacion.InsertarRegistro(
                pvo_Registro,
                pvo_Usuario.idUsuario(),
                pvo_Usuario.nombreCompleto());
        return ResponseEntity.status(HttpStatus.CREATED).body(vlo_Respuesta);
    }

    @PreAuthorize(ReglasAcceso.IMPORTACION_CONSULTAR)
    @GetMapping("/importaciones")
    public ResponseEntity<List<EntResumenPedidoImportacion>> SIMT_PEDIDO_PROV_ObtenerLista(
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(this.vgo_BllPedidoImportacion.ObtenerLista(pvo_Usuario.idUsuario()));
    }

    @PreAuthorize(ReglasAcceso.IMPORTACION_CONSULTAR)
    @GetMapping("/importaciones/{pvc_Referencia}")
    public ResponseEntity<EntDetallePedidoImportacion> SIMT_PEDIDO_PROV_ObtenerRegistro(
            @PathVariable("pvc_Referencia") String pvc_Referencia,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(
                this.vgo_BllPedidoImportacion.ObtenerRegistro(
                        pvc_Referencia,
                        pvo_Usuario.idUsuario(),
                        pvo_Usuario.nombreCompleto()));
    }

    @PreAuthorize(ReglasAcceso.IMPORTACION_MODIFICAR)
    @PutMapping("/importaciones/{pvc_Referencia}")
    public ResponseEntity<EntRespuestaPedidoImportacion> SIMT_PEDIDO_PROV_ModificarRegistro(
            @PathVariable("pvc_Referencia") String pvc_Referencia,
            @Valid @RequestBody EntRegistroPedidoImportacion pvo_Registro,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(this.vgo_BllPedidoImportacion.ModificarRegistro(
                pvc_Referencia,
                pvo_Registro,
                pvo_Usuario.idUsuario(),
                pvo_Usuario.nombreCompleto()));
    }

    @PreAuthorize(ReglasAcceso.IMPORTACION_ACCESO)
    @GetMapping("/importaciones/catalogos")
    public ResponseEntity<EntCatalogosPedidoImportacion> SIMM_PEDIDO_PROV_ObtenerCatalogos() {
        return ResponseEntity.ok(this.vgo_BllPedidoImportacion.ObtenerCatalogos());
    }

    @PreAuthorize(ReglasAcceso.IMPORTACION_NOTIFICACIONES)
    @GetMapping("/importaciones/notificaciones")
    public ResponseEntity<List<EntNotificacionPedidoImportacion>> SIMT_PEDIDO_PROV_ObtenerNotificaciones(
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(
                this.vgo_BllPedidoImportacion.ObtenerNotificaciones(pvo_Usuario.idUsuario()));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Registra una importación temporal (HU-025). La petición viaja como
     *   multipart/form-data: los datos del formulario y, si se adjuntan, los documentos, cada uno
     *   en un campo cuyo nombre es el código de su tipo de documento (por ejemplo OFICIO_ADUANA).
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: registra también la exportación temporal (HU-026): la ruta indica el tipo.
     *
     * @param pvc_Tipo importaciones-temporales o exportaciones-temporales
     * @param pvo_Registro datos del formulario
     * @param pvo_MpArchivos documentos agrupados por código de tipo de documento
     * @param pvo_Usuario usuario de la sesión, que queda como responsable
     * @return confirmación del registro
     */
    @PreAuthorize(ReglasAcceso.ADUANERO_TEMPORAL_CREAR)
    @PostMapping(path = RUTA_TRAMITE_TEMPORAL, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntRespuestaTramiteTemporal> SIMT_TRAMITE_TEMPORAL_InsertarRegistro(
            @PathVariable("pvc_Tipo") String pvc_Tipo,
            @ModelAttribute EntRegistroTramiteTemporal pvo_Registro,
            @RequestParam MultiValueMap<String, MultipartFile> pvo_MpArchivos,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.vgo_BllTramiteTemporal.InsertarRegistro(
                TipoTramiteTemporal.ObtenerPorRuta(pvc_Tipo), pvo_Registro, pvo_MpArchivos, pvo_Usuario.idUsuario()));
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Obtiene la lista de trámites temporales.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: lista las importaciones temporales según su alcance; por defecto, la vista de
     *     trámites activos (HU-025).
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: lista también las exportaciones temporales (HU-026): la ruta indica el tipo.
     *
     * @param pvc_Tipo importaciones-temporales o exportaciones-temporales
     * @param pvc_Alcance ACTIVOS, CERRADOS o TODOS
     * @return expedientes con su cuenta regresiva
     */
    @PreAuthorize(ReglasAcceso.ADUANERO_TEMPORAL_CONSULTAR)
    @GetMapping(RUTA_TRAMITE_TEMPORAL)
    public ResponseEntity<List<EntResumenTramiteTemporal>> SIMT_TRAMITE_TEMPORAL_ObtenerLista(
            @PathVariable("pvc_Tipo") String pvc_Tipo,
            @RequestParam(value = "alcance", required = false) String pvc_Alcance) {
        return ResponseEntity.ok(this.vgo_BllTramiteTemporal.ObtenerLista(
                TipoTramiteTemporal.ObtenerPorRuta(pvc_Tipo), pvc_Alcance));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene las opciones de las pantallas de importación temporal: sugerencias,
     *   agentes, monedas y estados (HU-025).
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal, que además recibe los países (HU-026).
     *
     * @param pvc_Tipo importaciones-temporales o exportaciones-temporales
     * @return opciones de las pantallas
     */
    @PreAuthorize(ReglasAcceso.ADUANERO_TEMPORAL_ACCESO)
    @GetMapping(RUTA_TRAMITE_TEMPORAL + "/opciones")
    public ResponseEntity<EntOpcionesTramiteTemporal> SIMT_TRAMITE_TEMPORAL_ObtenerOpciones(
            @PathVariable("pvc_Tipo") String pvc_Tipo) {
        return ResponseEntity.ok(this.vgo_BllTramiteTemporal.ObtenerOpciones(
                TipoTramiteTemporal.ObtenerPorRuta(pvc_Tipo)));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene el expediente completo de una importación temporal (HU-025).
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026).
     *
     * @param pvc_Tipo importaciones-temporales o exportaciones-temporales
     * @param pvc_ReferenciaInterna referencia del expediente
     * @return expediente completo
     */
    @PreAuthorize(ReglasAcceso.ADUANERO_TEMPORAL_CONSULTAR)
    @GetMapping(RUTA_TRAMITE_TEMPORAL + "/{pvc_ReferenciaInterna}")
    public ResponseEntity<EntExpedienteTramiteTemporal> SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(
            @PathVariable("pvc_Tipo") String pvc_Tipo,
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna) {
        return ResponseEntity.ok(this.vgo_BllTramiteTemporal.ObtenerExpediente(
                TipoTramiteTemporal.ObtenerPorRuta(pvc_Tipo), pvc_ReferenciaInterna));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Cambia el estado de una importación temporal sin editar el resto de sus datos
     *   (HU-025).
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026).
     *
     * @param pvc_Tipo importaciones-temporales o exportaciones-temporales
     * @param pvc_ReferenciaInterna referencia del expediente
     * @param pvo_Cambio nuevo estado y justificación
     * @param pvo_Usuario usuario de la sesión
     * @return expediente actualizado
     */
    @PreAuthorize(ReglasAcceso.ADUANERO_TEMPORAL_MODIFICAR)
    @PutMapping(path = RUTA_TRAMITE_TEMPORAL + "/{pvc_ReferenciaInterna}/estado",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntExpedienteTramiteTemporal> SIMT_TRAMITE_TEMPORAL_ModificarEstado(
            @PathVariable("pvc_Tipo") String pvc_Tipo,
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestBody EntCambioEstado pvo_Cambio,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(this.vgo_BllTramiteTemporal.ActualizarEstado(
                TipoTramiteTemporal.ObtenerPorRuta(pvc_Tipo), pvc_ReferenciaInterna, pvo_Cambio,
                pvo_Usuario.idUsuario()));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Actualiza los datos de aduana, de salida y las observaciones de una importación
     *   temporal (HU-025).
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal y los datos de su reimportación
     *     (HU-026).
     *
     * @param pvc_Tipo importaciones-temporales o exportaciones-temporales
     * @param pvc_ReferenciaInterna referencia del expediente
     * @param pvo_Datos datos completos del expediente
     * @param pvo_Usuario usuario de la sesión
     * @return expediente actualizado
     */
    @PreAuthorize(ReglasAcceso.ADUANERO_TEMPORAL_MODIFICAR)
    @PutMapping(path = RUTA_TRAMITE_TEMPORAL + "/{pvc_ReferenciaInterna}/datos",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntExpedienteTramiteTemporal> SIMT_TRAMITE_TEMPORAL_ModificarRegistro(
            @PathVariable("pvc_Tipo") String pvc_Tipo,
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestBody EntDatosTramiteTemporal pvo_Datos,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(this.vgo_BllTramiteTemporal.ModificarDatos(
                TipoTramiteTemporal.ObtenerPorRuta(pvc_Tipo), pvc_ReferenciaInterna, pvo_Datos,
                pvo_Usuario.idUsuario()));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Registra una prórroga autorizada de un trámite temporal (HU-028).
     *
     * @param pvc_Tipo importaciones-temporales o exportaciones-temporales
     * @param pvc_ReferenciaInterna referencia del expediente
     * @param pvo_Prorroga datos de la prórroga
     * @param pvo_OficioSolicitud oficio enviado a la aduana
     * @param pvo_Resolucion resolución emitida por la aduana
     * @param pvo_Usuario usuario de la sesión
     * @return expediente actualizado
     */
    @PreAuthorize(ReglasAcceso.ADUANERO_TEMPORAL_MODIFICAR)
    @PutMapping(path = RUTA_TRAMITE_TEMPORAL + "/{pvc_ReferenciaInterna}/prorroga",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntExpedienteTramiteTemporal> SIMT_TRAMITE_TEMPORAL_RegistrarProrroga(
            @PathVariable("pvc_Tipo") String pvc_Tipo,
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestPart("prorroga") EntProrrogaTemporal pvo_Prorroga,
            @RequestPart("oficioSolicitud") MultipartFile pvo_OficioSolicitud,
            @RequestPart("resolucion") MultipartFile pvo_Resolucion,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(this.vgo_BllTramiteTemporal.RegistrarProrroga(
                TipoTramiteTemporal.ObtenerPorRuta(pvc_Tipo), pvc_ReferenciaInterna, pvo_Prorroga,
                pvo_OficioSolicitud, pvo_Resolucion,
                pvo_Usuario.idUsuario()));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Agrega documentación a una importación temporal. Cada archivo viaja en un campo
     *   cuyo nombre es el código de su tipo de documento (HU-025).
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026).
     *
     * @param pvc_Tipo importaciones-temporales o exportaciones-temporales
     * @param pvc_ReferenciaInterna referencia del expediente
     * @param pvo_MpArchivos documentos agrupados por código de tipo de documento
     * @param pvo_Usuario usuario de la sesión
     * @return expediente actualizado
     */
    @PreAuthorize(ReglasAcceso.ADUANERO_TEMPORAL_MODIFICAR)
    @PostMapping(path = RUTA_TRAMITE_TEMPORAL + "/{pvc_ReferenciaInterna}/documentos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntExpedienteTramiteTemporal> SIMF_DOCUMENTO_InsertarRegistroTemporal(
            @PathVariable("pvc_Tipo") String pvc_Tipo,
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestParam MultiValueMap<String, MultipartFile> pvo_MpArchivos,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.vgo_BllTramiteTemporal.InsertarDocumentos(
                TipoTramiteTemporal.ObtenerPorRuta(pvc_Tipo), pvc_ReferenciaInterna, pvo_MpArchivos,
                pvo_Usuario.idUsuario()));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Retira un documento cargado por error de una importación temporal abierta
     *   (HU-025).
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026).
     *
     * @param pvc_Tipo importaciones-temporales o exportaciones-temporales
     * @param pvc_ReferenciaInterna referencia del expediente
     * @param pvn_IdDocumento documento que se retira
     * @param pvo_Usuario usuario de la sesión
     * @return expediente actualizado
     */
    @PreAuthorize(ReglasAcceso.ADUANERO_TEMPORAL_MODIFICAR)
    @DeleteMapping(RUTA_TRAMITE_TEMPORAL + "/{pvc_ReferenciaInterna}/documentos/{pvn_IdDocumento}")
    public ResponseEntity<EntExpedienteTramiteTemporal> SIMF_DOCUMENTO_EliminarRegistroTemporal(
            @PathVariable("pvc_Tipo") String pvc_Tipo,
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @PathVariable("pvn_IdDocumento") Integer pvn_IdDocumento,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(this.vgo_BllTramiteTemporal.EliminarDocumento(
                TipoTramiteTemporal.ObtenerPorRuta(pvc_Tipo), pvc_ReferenciaInterna, pvn_IdDocumento,
                pvo_Usuario.idUsuario()));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Entrega el archivo de un documento de una importación temporal (HU-025).
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026).
     *
     * @param pvc_Tipo importaciones-temporales o exportaciones-temporales
     * @param pvn_IdDocumento identificador del documento
     * @return contenido del archivo
     */
    @PreAuthorize(ReglasAcceso.CONSULTAR_DOCUMENTO)
    @GetMapping(RUTA_TRAMITE_TEMPORAL + "/documentos/{pvn_IdDocumento}")
    public ResponseEntity<byte[]> SIMF_DOCUMENTO_ObtenerArchivoTemporal(
            @PathVariable("pvc_Tipo") String pvc_Tipo,
            @PathVariable("pvn_IdDocumento") Integer pvn_IdDocumento) {
        EntDocumento vlo_EnDocumento = this.vgo_BllTramiteTemporal.ObtenerDocumento(
                TipoTramiteTemporal.ObtenerPorRuta(pvc_Tipo), pvn_IdDocumento);
        byte[] vlo_Contenido = this.vgo_BllTramiteTemporal.ObtenerContenidoDocumento(vlo_EnDocumento);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(vlo_EnDocumento.getNombreArchivo(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(vlo_Contenido);
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Obtiene los agentes de aduanas que se pueden asignar a un trámite temporal.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: los agentes se obtienen sin depender del tipo de trámite temporal (HU-026).
     *
     * @return agentes disponibles
     */
    @PreAuthorize(ReglasAcceso.ADUANERO_TEMPORAL_ACCESO)
    @GetMapping("/temporales/agentes")
    public ResponseEntity<List<EntAgenteTemporal>> SIMF_USUARIO_ObtenerAgentesTemporales() {
        return ResponseEntity.ok(this.vgo_BllTramiteTemporal.ObtenerAgentes());
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Obtiene la bandeja de alertas de vencimiento del usuario de la sesión (HU-027).
     *
     * @param pvo_Usuario usuario de la sesión
     * @return alertas del usuario
     */
    @PreAuthorize(ReglasAcceso.ADUANERO_TEMPORAL_CONSULTAR)
    @GetMapping("/temporales/alertas")
    public ResponseEntity<List<EntResumenAlertaTemporal>> SIMT_ALERTA_TEMPORAL_ObtenerLista(
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(this.vgo_BllAlertaTemporal.ObtenerAlertas(pvo_Usuario.idUsuario()));
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Marca como atendida una alerta del usuario de la sesión (HU-027).
     *
     * @param pvn_IdAlerta identificador de la alerta
     * @param pvo_Usuario usuario de la sesión
     * @return alerta actualizada
     */
    @PreAuthorize(ReglasAcceso.ADUANERO_TEMPORAL_MODIFICAR)
    @PutMapping("/temporales/alertas/{pvn_IdAlerta}/atendida")
    public ResponseEntity<EntResumenAlertaTemporal> SIMT_ALERTA_TEMPORAL_MarcarAtendida(
            @PathVariable("pvn_IdAlerta") Integer pvn_IdAlerta,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(
                this.vgo_BllAlertaTemporal.MarcarAtendida(pvn_IdAlerta, pvo_Usuario.idUsuario()));
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Registra un trámite de desalmacenaje especial y devuelve la confirmación con la
     *   referencia interna y el estado inicial del expediente. La petición viaja como
     *   multipart/form-data porque incluye la documentación de respaldo.
     *
     * @param pvc_ReferenciaInterna referencia interna con el prefijo del tipo de trámite
     * @param pvc_OrigenDependencia origen o dependencia responsable del trámite
     * @param pvc_PaisOrigen país de origen de la mercancía, si aplica
     * @param pvc_Moneda código ISO de la moneda del valor estimado
     * @param pvn_ValorEstimado valor estimado de la mercancía
     * @param pvc_Justificacion justificación del trámite
     * @param pvn_IdUsuarioResponsable usuario responsable del expediente
     * @param pvo_LsRespaldo justificaciones, actas históricas o documentos atípicos de respaldo
     * @param pvo_PackingList lista de empaque, si aplica
     * @param pvo_Factura factura o documento equivalente, si aplica
     * @param pvo_DocumentoEmbarque documento de embarque, si aplica
     * @return confirmación del registro del expediente
     */
    @PreAuthorize(ReglasAcceso.DESALMACENAJE_CREAR)
    @PostMapping(path = "/desalmacenaje-especial", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntRespuestaDesalmacenaje> SIMT_DESALMACENAJE_ESPEC_InsertarRegistro(
            @RequestParam("referenciaInterna") String pvc_ReferenciaInterna,
            @RequestParam("origenDependencia") String pvc_OrigenDependencia,
            @RequestParam(value = "paisOrigen", required = false) String pvc_PaisOrigen,
            @RequestParam("moneda") String pvc_Moneda,
            @RequestParam(value = "valorEstimado", required = false) BigDecimal pvn_ValorEstimado,
            @RequestParam("justificacion") String pvc_Justificacion,
            @RequestParam("idUsuarioResponsable") Integer pvn_IdUsuarioResponsable,
            @RequestParam(value = "documentosRespaldo", required = false) MultipartFile[] pvo_LsRespaldo,
            @RequestParam(value = "packingList", required = false) MultipartFile pvo_PackingList,
            @RequestParam(value = "factura", required = false) MultipartFile pvo_Factura,
            @RequestParam(value = "documentoEmbarque", required = false) MultipartFile pvo_DocumentoEmbarque) {

        EntRegistroDesalmacenaje vlo_Registro = new EntRegistroDesalmacenaje();
        vlo_Registro.setReferenciaInterna(pvc_ReferenciaInterna);
        vlo_Registro.setOrigenDependencia(pvc_OrigenDependencia);
        vlo_Registro.setPaisOrigen(pvc_PaisOrigen);
        vlo_Registro.setMoneda(pvc_Moneda);
        vlo_Registro.setValorEstimado(pvn_ValorEstimado);
        vlo_Registro.setJustificacion(pvc_Justificacion);
        vlo_Registro.setIdUsuarioResponsable(pvn_IdUsuarioResponsable);

        List<MultipartFile> vlo_LsRespaldo =
                pvo_LsRespaldo == null ? List.of() : Arrays.asList(pvo_LsRespaldo);

        EntRespuestaDesalmacenaje vlo_Respuesta = this.vgo_BllDesalmacenajeEspecial.InsertarRegistro(
                vlo_Registro,
                vlo_LsRespaldo,
                pvo_PackingList,
                pvo_Factura,
                pvo_DocumentoEmbarque);

        return ResponseEntity.status(HttpStatus.CREATED).body(vlo_Respuesta);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene los expedientes de desalmacenaje especial registrados, para la
     *   pantalla de seguimiento y la generación de reportes.
     *
     * @return lista de expedientes
     */
    @PreAuthorize(ReglasAcceso.DESALMACENAJE_CONSULTAR)
    @GetMapping("/desalmacenaje-especial")
    public ResponseEntity<List<EntExpedienteResumen>> SIMT_TRAMITE_ObtenerLista() {
        return ResponseEntity.ok(this.vgo_BllDesalmacenajeEspecial.ObtenerListaExpedientes());
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene un expediente completo con su documentación y su historial de estados.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @return expediente completo
     */
    @PreAuthorize(ReglasAcceso.DESALMACENAJE_CONSULTAR)
    @GetMapping("/desalmacenaje-especial/{pvc_ReferenciaInterna}")
    public ResponseEntity<EntExpedienteDetalle> SIMT_TRAMITE_ObtenerRegistro(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna) {
        return ResponseEntity.ok(this.vgo_BllDesalmacenajeEspecial.ObtenerExpediente(pvc_ReferenciaInterna));
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Agrega documentación a un expediente ya registrado. La documentación de un
     *   desalmacenaje especial suele llegar después del registro del trámite.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvn_IdUsuarioCarga usuario que adjunta la documentación
     * @param pvo_LsRespaldo documentos de respaldo adicionales
     * @param pvo_PackingList lista de empaque, si aplica
     * @param pvo_Factura factura o documento equivalente, si aplica
     * @param pvo_DocumentoEmbarque documento de embarque, si aplica
     * @return expediente actualizado con toda su documentación
     */
    @PreAuthorize(ReglasAcceso.DESALMACENAJE_MODIFICAR)
    @PostMapping(
            path = "/desalmacenaje-especial/{pvc_ReferenciaInterna}/documentos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntExpedienteDetalle> SIMF_DOCUMENTO_InsertarRegistro(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestParam("idUsuarioResponsable") Integer pvn_IdUsuarioCarga,
            @RequestParam(value = "documentosRespaldo", required = false) MultipartFile[] pvo_LsRespaldo,
            @RequestParam(value = "packingList", required = false) MultipartFile pvo_PackingList,
            @RequestParam(value = "factura", required = false) MultipartFile pvo_Factura,
            @RequestParam(value = "documentoEmbarque", required = false) MultipartFile pvo_DocumentoEmbarque) {

        List<MultipartFile> vlo_LsRespaldo =
                pvo_LsRespaldo == null ? List.of() : Arrays.asList(pvo_LsRespaldo);

        EntExpedienteDetalle vlo_Detalle = this.vgo_BllDesalmacenajeEspecial.InsertarDocumentos(
                pvc_ReferenciaInterna,
                pvn_IdUsuarioCarga,
                vlo_LsRespaldo,
                pvo_PackingList,
                pvo_Factura,
                pvo_DocumentoEmbarque);

        return ResponseEntity.status(HttpStatus.CREATED).body(vlo_Detalle);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene los estados que el catálogo admite para un desalmacenaje especial, para
     *   ofrecerlos en la pantalla de seguimiento (HU-032).
     *
     * @return estados disponibles
     */
    @PreAuthorize(ReglasAcceso.DESALMACENAJE_ACCESO)
    @GetMapping("/desalmacenaje-especial/estados")
    public ResponseEntity<List<EntEstadoTramite>> SIMM_ESTADO_TRAMITE_ObtenerLista() {
        return ResponseEntity.ok(this.vgo_BllDesalmacenajeEspecial.ObtenerEstadosDisponibles());
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Actualiza el estado de un expediente de desalmacenaje especial. Exige la
     *   justificación del cambio y devuelve el expediente con su historial actualizado (HU-032).
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvo_Cambio estado nuevo, justificación y usuario responsable
     * @return expediente actualizado
     */
    @PreAuthorize(ReglasAcceso.DESALMACENAJE_MODIFICAR)
    @PutMapping(path = "/desalmacenaje-especial/{pvc_ReferenciaInterna}/estado",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntExpedienteDetalle> SIMT_TRAMITE_ModificarEstado(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestBody EntCambioEstado pvo_Cambio) {
        return ResponseEntity.ok(this.vgo_BllDesalmacenajeEspecial.ActualizarEstado(pvc_ReferenciaInterna, pvo_Cambio));
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Entrega el archivo de un documento del expediente para su descarga.
     *
     * @param pvn_IdDocumento identificador del documento
     * @return contenido del archivo
     */
    @PreAuthorize(ReglasAcceso.CONSULTAR_DOCUMENTO)
    @GetMapping("/desalmacenaje-especial/documentos/{pvn_IdDocumento}")
    public ResponseEntity<byte[]> SIMF_DOCUMENTO_ObtenerArchivo(
            @PathVariable("pvn_IdDocumento") Integer pvn_IdDocumento) {
        EntDocumento vlo_EnDocumento = this.vgo_BllDesalmacenajeEspecial.ObtenerDocumento(pvn_IdDocumento);
        byte[] vlo_Contenido = this.vgo_BllDesalmacenajeEspecial.ObtenerContenidoDocumento(vlo_EnDocumento);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(vlo_EnDocumento.getNombreArchivo())
                                .build()
                                .toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(vlo_Contenido);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 10/09/2026
     * Descripción: Registra una donación y devuelve la confirmación con la referencia interna y el
     *   estado inicial del expediente. La petición viaja como multipart/form-data porque incluye la
     *   resolución o acta de aceptación y la documentación de respaldo.
     *
     * @param pvc_ReferenciaInterna referencia interna con la sigla D- o DF-
     * @param pvc_TipoDonacion tipo de donación: Nacional, Internacional o Fundación UCR
     * @param pvc_NombreDonante persona o entidad donante
     * @param pvc_PaisOrigen país de origen de la mercancía, si aplica
     * @param pvc_Moneda código ISO de la moneda del valor estimado
     * @param pvn_ValorEstimado valor estimado de la donación
     * @param pvc_ReferenciaFundacionUcr referencia de la Fundación UCR, si aplica
     * @param pvc_Observaciones observaciones del expediente, si aplica
     * @param pvn_IdUsuarioResponsable usuario responsable del expediente
     * @param pvo_ResolucionAceptacion resolución o acta de aceptación de la donación
     * @param pvo_LsRespaldo documentos de respaldo adicionales, si aplica
     * @param pvo_PackingList lista de empaque, si aplica
     * @param pvo_Factura factura o documento equivalente, si aplica
     * @param pvo_DocumentoEmbarque documento de embarque, si aplica
     * @return confirmación del registro del expediente
     */
    @PreAuthorize(ReglasAcceso.DONACION_CREAR)
    @PostMapping(path = "/donaciones", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntRespuestaDonacion> SIMT_DONACION_InsertarRegistro(
            @RequestParam("referenciaInterna") String pvc_ReferenciaInterna,
            @RequestParam("tipoDonacion") String pvc_TipoDonacion,
            @RequestParam("nombreDonante") String pvc_NombreDonante,
            @RequestParam(value = "paisOrigen", required = false) String pvc_PaisOrigen,
            @RequestParam("moneda") String pvc_Moneda,
            @RequestParam(value = "valorEstimado", required = false) BigDecimal pvn_ValorEstimado,
            @RequestParam(value = "referenciaFundacionUcr", required = false) String pvc_ReferenciaFundacionUcr,
            @RequestParam(value = "observaciones", required = false) String pvc_Observaciones,
            @RequestParam("idUsuarioResponsable") Integer pvn_IdUsuarioResponsable,
            @RequestParam(value = "resolucionAceptacion", required = false)
            MultipartFile pvo_ResolucionAceptacion,
            @RequestParam(value = "documentosRespaldo", required = false) MultipartFile[] pvo_LsRespaldo,
            @RequestParam(value = "packingList", required = false) MultipartFile pvo_PackingList,
            @RequestParam(value = "factura", required = false) MultipartFile pvo_Factura,
            @RequestParam(value = "documentoEmbarque", required = false) MultipartFile pvo_DocumentoEmbarque) {

        EntRegistroDonacion vlo_Registro = new EntRegistroDonacion();
        vlo_Registro.setReferenciaInterna(pvc_ReferenciaInterna);
        vlo_Registro.setTipoDonacion(pvc_TipoDonacion);
        vlo_Registro.setNombreDonante(pvc_NombreDonante);
        vlo_Registro.setPaisOrigen(pvc_PaisOrigen);
        vlo_Registro.setMoneda(pvc_Moneda);
        vlo_Registro.setValorEstimado(pvn_ValorEstimado);
        vlo_Registro.setReferenciaFundacionUcr(pvc_ReferenciaFundacionUcr);
        vlo_Registro.setObservaciones(pvc_Observaciones);
        vlo_Registro.setIdUsuarioResponsable(pvn_IdUsuarioResponsable);

        List<MultipartFile> vlo_LsRespaldo =
                pvo_LsRespaldo == null ? List.of() : Arrays.asList(pvo_LsRespaldo);

        EntRespuestaDonacion vlo_Respuesta = this.vgo_BllDonacion.InsertarRegistro(
                vlo_Registro,
                pvo_ResolucionAceptacion,
                vlo_LsRespaldo,
                pvo_PackingList,
                pvo_Factura,
                pvo_DocumentoEmbarque);

        return ResponseEntity.status(HttpStatus.CREATED).body(vlo_Respuesta);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Obtiene los expedientes de donación registrados, para la pantalla desde la que
     *   se consultan y se gestionan los trámites (HU-044).
     *
     * @return lista de expedientes de donación
     */
    @PreAuthorize(ReglasAcceso.DONACION_CONSULTAR)
    @GetMapping("/donaciones")
    public ResponseEntity<List<EntResumenDonacion>> SIMT_DONACION_ObtenerLista() {
        return ResponseEntity.ok(this.vgo_BllDonacion.ObtenerListaExpedientes());
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Obtiene los estados que el catálogo define para los trámites de donación, para
     *   que la pantalla de gestión cargue las opciones de cambio de estado (HU-044).
     *
     * @return estados disponibles del trámite de donación
     */
    @PreAuthorize(ReglasAcceso.DONACION_ACCESO)
    @GetMapping("/donaciones/estados")
    public ResponseEntity<List<EntEstadoTramite>> SIMM_ESTADO_TRAMITE_ObtenerListaDonacion() {
        return ResponseEntity.ok(this.vgo_BllDonacion.ObtenerEstadosDisponibles());
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Obtiene un expediente de donación completo, con su estado actual, su
     *   documentación y su historial de estados (HU-044).
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @return expediente completo
     */
    @PreAuthorize(ReglasAcceso.DONACION_CONSULTAR)
    @GetMapping("/donaciones/{pvc_ReferenciaInterna}")
    public ResponseEntity<EntExpedienteDonacion> SIMT_DONACION_ObtenerRegistro(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna) {
        return ResponseEntity.ok(this.vgo_BllDonacion.ObtenerExpediente(pvc_ReferenciaInterna));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Actualiza el estado de un expediente de donación y deja el movimiento en el
     *   historial, con su fecha, su hora y el usuario responsable (HU-044).
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvo_Cambio nuevo estado, justificación, observaciones y usuario responsable
     * @return expediente actualizado con su documentación y su historial
     */
    @PreAuthorize(ReglasAcceso.DONACION_MODIFICAR)
    @PutMapping(path = "/donaciones/{pvc_ReferenciaInterna}/estado",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntExpedienteDonacion> SIMT_DONACION_ModificarEstado(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestBody EntCambioEstadoDonacion pvo_Cambio) {
        return ResponseEntity.ok(
                this.vgo_BllDonacion.ActualizarEstado(pvc_ReferenciaInterna, pvo_Cambio));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Agrega documentación de respaldo a un expediente de donación ya registrado. La
     *   documentación anterior se conserva en el expediente (HU-044).
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvn_IdUsuarioCarga usuario que adjunta la documentación
     * @param pvo_ResolucionAceptacion resolución o acta de aceptación posterior, si aplica
     * @param pvo_LsRespaldo documentos de respaldo adicionales
     * @param pvo_PackingList lista de empaque, si aplica
     * @param pvo_Factura factura o documento equivalente, si aplica
     * @param pvo_DocumentoEmbarque documento de embarque, si aplica
     * @return expediente actualizado con toda su documentación
     */
    @PreAuthorize(ReglasAcceso.DONACION_MODIFICAR)
    @PostMapping(
            path = "/donaciones/{pvc_ReferenciaInterna}/documentos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntExpedienteDonacion> SIMF_DOCUMENTO_InsertarRegistroDonacion(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestParam("idUsuarioResponsable") Integer pvn_IdUsuarioCarga,
            @RequestParam(value = "resolucionAceptacion", required = false)
            MultipartFile pvo_ResolucionAceptacion,
            @RequestParam(value = "documentosRespaldo", required = false) MultipartFile[] pvo_LsRespaldo,
            @RequestParam(value = "packingList", required = false) MultipartFile pvo_PackingList,
            @RequestParam(value = "factura", required = false) MultipartFile pvo_Factura,
            @RequestParam(value = "documentoEmbarque", required = false) MultipartFile pvo_DocumentoEmbarque) {

        List<MultipartFile> vlo_LsRespaldo =
                pvo_LsRespaldo == null ? List.of() : Arrays.asList(pvo_LsRespaldo);

        EntExpedienteDonacion vlo_Detalle = this.vgo_BllDonacion.InsertarDocumentos(
                pvc_ReferenciaInterna,
                pvn_IdUsuarioCarga,
                pvo_ResolucionAceptacion,
                vlo_LsRespaldo,
                pvo_PackingList,
                pvo_Factura,
                pvo_DocumentoEmbarque);

        return ResponseEntity.status(HttpStatus.CREATED).body(vlo_Detalle);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 03/10/2026
     * Descripción: Retira del expediente de donación un documento cargado por error, mientras el
     *   expediente siga abierto. El archivo se conserva como inactivo y la eliminación queda en la
     *   bitácora (HU-044).
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvn_IdDocumento documento que se retira
     * @param pvn_IdUsuarioResponsable usuario que realiza la acción
     * @return expediente con su documentación actualizada
     */
    @PreAuthorize(ReglasAcceso.DONACION_MODIFICAR)
    @DeleteMapping("/donaciones/{pvc_ReferenciaInterna}/documentos/{pvn_IdDocumento}")
    public ResponseEntity<EntExpedienteDonacion> SIMF_DOCUMENTO_EliminarRegistroDonacion(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @PathVariable("pvn_IdDocumento") Integer pvn_IdDocumento,
            @RequestParam("idUsuarioResponsable") Integer pvn_IdUsuarioResponsable) {

        EntExpedienteDonacion vlo_Detalle = this.vgo_BllDonacion.EliminarDocumento(
                pvc_ReferenciaInterna, pvn_IdDocumento, pvn_IdUsuarioResponsable);
        return ResponseEntity.ok(vlo_Detalle);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 15/09/2026
     * Descripción: Entrega el archivo de un documento del expediente de donación para su
     *   descarga (HU-044).
     *
     * @param pvn_IdDocumento identificador del documento
     * @return contenido del archivo
     */
    @PreAuthorize(ReglasAcceso.CONSULTAR_DOCUMENTO)
    @GetMapping("/donaciones/documentos/{pvn_IdDocumento}")
    public ResponseEntity<byte[]> SIMF_DOCUMENTO_ObtenerArchivoDonacion(
            @PathVariable("pvn_IdDocumento") Integer pvn_IdDocumento) {
        EntDocumento vlo_EnDocumento = this.vgo_BllDonacion.ObtenerDocumento(pvn_IdDocumento);
        byte[] vlo_Contenido = this.vgo_BllDonacion.ObtenerContenidoDocumento(vlo_EnDocumento);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(vlo_EnDocumento.getNombreArchivo())
                                .build()
                                .toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(vlo_Contenido);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Registra una solicitud de exoneración. La documentación obligatoria faltante no
     *   impide el registro: la respuesta informa lo pendiente. La petición viaja como
     *   multipart/form-data porque incluye la documentación.
     *
     * @param pvc_ReferenciaInterna referencia interna con el prefijo EXO-
     * @param pvc_TipoExoneracion código del tipo de exoneración
     * @param pvc_NumeroContratacion número de contratación
     * @param pvc_OrdenCompraGeco orden de compra registrada en GECO, opcional
     * @param pvc_NumeroExonet número de solicitud que devuelve EXONET, opcional
     * @param pvc_NumeroAutorizacionExonet número de autorización de EXONET, opcional
     * @param pvc_ContratoSicop contrato SICOP, opcional porque hay exoneraciones que no van por SICOP
     * @param pvc_Observaciones observaciones de la solicitud, opcionales
     * @param pvn_IdUsuarioResponsable usuario responsable de la solicitud
     * @param pvo_DocumentoEmbarque documento de embarque
     * @param pvo_FacturaComercial factura comercial
     * @param pvo_FormularioS1 formulario S1
     * @param pvo_ListaEmpaque lista de empaque
     * @param pvo_Exonet EXONET, si aplica
     * @param pvo_TraduccionFactura traducción de la factura, si aplica
     * @param pvo_LsExpedienteSicop expediente de SICOP en PDF, si aplica
     * @param pvo_LsAutorizacionExonet autorización EXONET, si ya se tiene
     * @param pvo_LsDuaAplicacion DUA de aplicación, si ya se tiene
     * @param pvo_LsComprobanteImpuestos comprobante de los impuestos, si ya se tiene
     * @param pvo_LsOtrosDocumentos otros documentos de respaldo
     * @return confirmación del registro con la documentación pendiente
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_CREAR)
    @PostMapping(path = "/exoneraciones", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntRespuestaExoneracion> SIMT_EXONERACION_InsertarRegistro(
            @RequestParam("referenciaInterna") String pvc_ReferenciaInterna,
            @RequestParam("tipoExoneracion") String pvc_TipoExoneracion,
            @RequestParam("numeroContratacion") String pvc_NumeroContratacion,
            @RequestParam(value = "ordenCompraGeco", required = false) String pvc_OrdenCompraGeco,
            @RequestParam(value = "contratoSicop", required = false) String pvc_ContratoSicop,
            @RequestParam(value = "numeroExonet", required = false) String pvc_NumeroExonet,
            @RequestParam(value = "numeroAutorizacionExonet", required = false)
                    String pvc_NumeroAutorizacionExonet,
            @RequestParam(value = "observaciones", required = false) String pvc_Observaciones,
            @RequestParam("idUsuarioResponsable") Integer pvn_IdUsuarioResponsable,
            @RequestParam(value = "documentoEmbarque", required = false) MultipartFile[] pvo_DocumentoEmbarque,
            @RequestParam(value = "facturaComercial", required = false) MultipartFile[] pvo_FacturaComercial,
            @RequestParam(value = "formularioS1", required = false) MultipartFile[] pvo_FormularioS1,
            @RequestParam(value = "listaEmpaque", required = false) MultipartFile[] pvo_ListaEmpaque,
            @RequestParam(value = "exonet", required = false) MultipartFile[] pvo_Exonet,
            @RequestParam(value = "traduccionFactura", required = false) MultipartFile[] pvo_TraduccionFactura,
            @RequestParam(value = "expedienteSicop", required = false) MultipartFile[] pvo_LsExpedienteSicop,
            @RequestParam(value = "autorizacionExonet", required = false) MultipartFile[] pvo_LsAutorizacionExonet,
            @RequestParam(value = "duaAplicacion", required = false) MultipartFile[] pvo_LsDuaAplicacion,
            @RequestParam(value = "comprobanteImpuestos", required = false) MultipartFile[] pvo_LsComprobanteImpuestos,
            @RequestParam(value = "otrosDocumentos", required = false) MultipartFile[] pvo_LsOtrosDocumentos) {

        EntRegistroExoneracion vlo_Registro = new EntRegistroExoneracion();
        vlo_Registro.setReferenciaInterna(pvc_ReferenciaInterna);
        vlo_Registro.setTipoExoneracion(pvc_TipoExoneracion);
        vlo_Registro.setNumeroContratacion(pvc_NumeroContratacion);
        vlo_Registro.setOrdenCompraGeco(pvc_OrdenCompraGeco);
        vlo_Registro.setContratoSicop(pvc_ContratoSicop);
        vlo_Registro.setNumeroExonet(pvc_NumeroExonet);
        vlo_Registro.setNumeroAutorizacionExonet(pvc_NumeroAutorizacionExonet);
        vlo_Registro.setObservaciones(pvc_Observaciones);
        vlo_Registro.setIdUsuarioResponsable(pvn_IdUsuarioResponsable);

        EntRespuestaExoneracion vlo_Respuesta = this.vgo_BllExoneracion.InsertarRegistro(
                vlo_Registro,
                this.AgruparDocumentosExoneracion(pvo_DocumentoEmbarque, pvo_FacturaComercial, pvo_FormularioS1,
                        pvo_ListaEmpaque, pvo_Exonet, pvo_TraduccionFactura, pvo_LsExpedienteSicop,
                        pvo_LsAutorizacionExonet, pvo_LsDuaAplicacion, pvo_LsComprobanteImpuestos,
                        pvo_LsOtrosDocumentos));
        return ResponseEntity.status(HttpStatus.CREATED).body(vlo_Respuesta);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 25/10/2026
     * Descripción: Obtiene los expedientes de exoneración registrados, para la pantalla de entrada
     *   del módulo (comentario de las pruebas de usabilidad: era la única funcionalidad que no
     *   mostraba primero la lista de sus trámites).
     *
     * @return lista de expedientes de exoneración
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_CONSULTA_O_APROBACION)
    @GetMapping("/exoneraciones")
    public ResponseEntity<List<EntResumenExoneracion>> SIMT_EXONERACION_ObtenerLista() {
        return ResponseEntity.ok(this.vgo_BllExoneracion.ObtenerListaExpedientes());
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene el expediente de una solicitud de exoneración con su documentación, lo
     *   pendiente y su bitácora.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @return expediente completo
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_CONSULTA_O_APROBACION)
    @GetMapping("/exoneraciones/{pvc_ReferenciaInterna}")
    public ResponseEntity<EntExoneracionDetalle> SIMT_EXONERACION_ObtenerRegistro(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna) {
        return ResponseEntity.ok(this.vgo_BllExoneracion.ObtenerExpediente(pvc_ReferenciaInterna));
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Adjunta documentación a una solicitud de exoneración ya registrada.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @param pvn_IdUsuario usuario que adjunta la documentación
     * @param pvo_DocumentoEmbarque documento de embarque
     * @param pvo_FacturaComercial factura comercial
     * @param pvo_FormularioS1 formulario S1
     * @param pvo_ListaEmpaque lista de empaque
     * @param pvo_Exonet EXONET, si aplica
     * @param pvo_TraduccionFactura traducción de la factura, si aplica
     * @param pvo_LsExpedienteSicop expediente de SICOP en PDF, si aplica
     * @param pvo_LsAutorizacionExonet autorización EXONET, si ya se tiene
     * @param pvo_LsDuaAplicacion DUA de aplicación, si ya se tiene
     * @param pvo_LsComprobanteImpuestos comprobante de los impuestos, si ya se tiene
     * @param pvo_LsOtrosDocumentos otros documentos de respaldo
     * @return expediente actualizado
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @PostMapping(path = "/exoneraciones/{pvc_ReferenciaInterna}/documentos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntExoneracionDetalle> SIMF_DOCUMENTO_InsertarRegistroExoneracion(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestParam("idUsuarioResponsable") Integer pvn_IdUsuario,
            @RequestParam(value = "documentoEmbarque", required = false) MultipartFile[] pvo_DocumentoEmbarque,
            @RequestParam(value = "facturaComercial", required = false) MultipartFile[] pvo_FacturaComercial,
            @RequestParam(value = "formularioS1", required = false) MultipartFile[] pvo_FormularioS1,
            @RequestParam(value = "listaEmpaque", required = false) MultipartFile[] pvo_ListaEmpaque,
            @RequestParam(value = "exonet", required = false) MultipartFile[] pvo_Exonet,
            @RequestParam(value = "traduccionFactura", required = false) MultipartFile[] pvo_TraduccionFactura,
            @RequestParam(value = "expedienteSicop", required = false) MultipartFile[] pvo_LsExpedienteSicop,
            @RequestParam(value = "autorizacionExonet", required = false) MultipartFile[] pvo_LsAutorizacionExonet,
            @RequestParam(value = "duaAplicacion", required = false) MultipartFile[] pvo_LsDuaAplicacion,
            @RequestParam(value = "comprobanteImpuestos", required = false) MultipartFile[] pvo_LsComprobanteImpuestos,
            @RequestParam(value = "otrosDocumentos", required = false) MultipartFile[] pvo_LsOtrosDocumentos) {

        EntExoneracionDetalle vlo_Detalle = this.vgo_BllExoneracion.InsertarDocumentos(
                pvc_ReferenciaInterna,
                pvn_IdUsuario,
                this.AgruparDocumentosExoneracion(pvo_DocumentoEmbarque, pvo_FacturaComercial, pvo_FormularioS1,
                        pvo_ListaEmpaque, pvo_Exonet, pvo_TraduccionFactura, pvo_LsExpedienteSicop,
                        pvo_LsAutorizacionExonet, pvo_LsDuaAplicacion, pvo_LsComprobanteImpuestos,
                        pvo_LsOtrosDocumentos));
        return ResponseEntity.status(HttpStatus.CREATED).body(vlo_Detalle);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene la bitácora de una solicitud de exoneración, lista para imprimirse.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @return registros vigentes de la bitácora
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_CONSULTA_O_APROBACION)
    @GetMapping("/exoneraciones/{pvc_ReferenciaInterna}/bitacora")
    public ResponseEntity<List<EntMovimientoBitacora>> SIML_BITACORA_ObtenerLista(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna) {
        return ResponseEntity.ok(this.vgo_BllExoneracion.ObtenerBitacora(pvc_ReferenciaInterna));
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Agrega un comentario a la bitácora de una solicitud de exoneración.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @param pvo_Comentario texto del comentario y usuario que lo registra
     * @return bitácora vigente de la solicitud
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @PostMapping(path = "/exoneraciones/{pvc_ReferenciaInterna}/bitacora/comentarios",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntMovimientoBitacora>> SIML_BITACORA_InsertarComentario(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestBody EntComentarioBitacora pvo_Comentario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.vgo_BllExoneracion.InsertarComentario(pvc_ReferenciaInterna, pvo_Comentario));
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Elimina un comentario registrado por error en la bitácora de una solicitud.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @param pvn_IdBitacora identificador del comentario
     * @param pvn_IdUsuario usuario que elimina el comentario
     * @return bitácora vigente de la solicitud
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @DeleteMapping("/exoneraciones/{pvc_ReferenciaInterna}/bitacora/comentarios/{pvn_IdBitacora}")
    public ResponseEntity<List<EntMovimientoBitacora>> SIML_BITACORA_EliminarComentario(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @PathVariable("pvn_IdBitacora") Integer pvn_IdBitacora,
            @RequestParam("idUsuario") Integer pvn_IdUsuario) {
        return ResponseEntity.ok(
                this.vgo_BllExoneracion.EliminarComentario(pvc_ReferenciaInterna, pvn_IdBitacora, pvn_IdUsuario));
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Agrupa los archivos del formulario de exoneración por el código de tipo de
     *   documento con el que se registran en el catálogo.
     *
     * @return archivos agrupados por código de tipo de documento
     */
    private Map<String, List<MultipartFile>> AgruparDocumentosExoneracion(
            MultipartFile[] pvo_DocumentoEmbarque,
            MultipartFile[] pvo_FacturaComercial,
            MultipartFile[] pvo_FormularioS1,
            MultipartFile[] pvo_ListaEmpaque,
            MultipartFile[] pvo_Exonet,
            MultipartFile[] pvo_TraduccionFactura,
            MultipartFile[] pvo_LsExpedienteSicop,
            MultipartFile[] pvo_LsAutorizacionExonet,
            MultipartFile[] pvo_LsDuaAplicacion,
            MultipartFile[] pvo_LsComprobanteImpuestos,
            MultipartFile[] pvo_LsOtrosDocumentos) {
        Map<String, List<MultipartFile>> vlo_MpDocumentos = new LinkedHashMap<>();
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_EMBARQUE, this.ComoLista(pvo_DocumentoEmbarque));
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_FACTURA, this.ComoLista(pvo_FacturaComercial));
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_FORMULARIO_S1, this.ComoLista(pvo_FormularioS1));
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_PACKING_LIST, this.ComoLista(pvo_ListaEmpaque));
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_EXONET, this.ComoLista(pvo_Exonet));
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_TRADUCCION_FACTURA,
                this.ComoLista(pvo_TraduccionFactura));
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_EXPEDIENTE_SICOP, this.ComoLista(pvo_LsExpedienteSicop));
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_AUTORIZACION_EXONET,
                this.ComoLista(pvo_LsAutorizacionExonet));
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_DUA_APLICACION, this.ComoLista(pvo_LsDuaAplicacion));
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_COMPROBANTE_IMPUESTOS,
                this.ComoLista(pvo_LsComprobanteImpuestos));
        vlo_MpDocumentos.put(Constantes.CODIGO_TIPO_DOCUMENTO_OTROS, this.ComoLista(pvo_LsOtrosDocumentos));
        return vlo_MpDocumentos;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 16/09/2026
     * Descripción: Reúne los criterios recibidos de la búsqueda de trámites en su entidad.
     *
     * @return criterios de la búsqueda
     */
    private EntFiltroBusquedaTramite CrearFiltroBusqueda(
            LocalDate pvd_FechaDesde,
            LocalDate pvd_FechaHasta,
            String pvc_ReferenciaInterna,
            List<Integer> pvo_LsIdTiposTramite,
            List<String> pvo_LsCategoriasEstado,
            List<Integer> pvo_LsIdMonedas,
            List<String> pvo_LsRegimenesAduaneros,
            List<String> pvo_LsTiposExoneracion,
            List<String> pvo_LsAduanasControl) {
        EntFiltroBusquedaTramite vlo_Filtro = new EntFiltroBusquedaTramite();
        vlo_Filtro.setFechaDesde(pvd_FechaDesde);
        vlo_Filtro.setFechaHasta(pvd_FechaHasta);
        vlo_Filtro.setReferenciaInterna(pvc_ReferenciaInterna);
        // La búsqueda se limita a los tipos de trámite que el usuario puede consultar (HU-003).
        vlo_Filtro.setIdTiposTramite(this.vgo_SrvAutorizacion.RestringirTiposTramite(pvo_LsIdTiposTramite));
        vlo_Filtro.setCategoriasEstado(pvo_LsCategoriasEstado);
        vlo_Filtro.setIdMonedas(pvo_LsIdMonedas);
        vlo_Filtro.setRegimenesAduaneros(pvo_LsRegimenesAduaneros);
        vlo_Filtro.setTiposExoneracion(pvo_LsTiposExoneracion);
        vlo_Filtro.setAduanasControl(pvo_LsAduanasControl);
        return vlo_Filtro;
    }

    private List<MultipartFile> ComoLista(MultipartFile[] pvo_Archivos) {
        return pvo_Archivos == null ? List.of() : Arrays.asList(pvo_Archivos);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 22/09/2026
     * Descripción: Corrige los datos contractuales de una solicitud de exoneración. El valor
     *   anterior de cada dato queda anotado en la bitácora.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @param pvo_Correccion datos corregidos y usuario responsable
     * @return expediente con sus datos actualizados
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @PutMapping(path = "/exoneraciones/{pvc_ReferenciaInterna}/datos",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntExoneracionDetalle> SIMT_EXONERACION_ModificarRegistro(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestBody EntCorreccionExoneracion pvo_Correccion) {

        EntExoneracionDetalle vlo_Detalle = this.vgo_BllExoneracion.ModificarDatosContractuales(
                pvc_ReferenciaInterna, pvo_Correccion);
        return ResponseEntity.ok(vlo_Detalle);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 22/09/2026
     * Descripción: Retira del expediente de exoneración un documento cargado por error. El
     *   archivo se conserva como inactivo y la eliminación queda en la bitácora.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @param pvn_IdDocumento documento que se retira
     * @param pvn_IdUsuarioResponsable usuario que realiza la acción
     * @return expediente con su documentación actualizada
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @DeleteMapping("/exoneraciones/{pvc_ReferenciaInterna}/documentos/{pvn_IdDocumento}")
    public ResponseEntity<EntExoneracionDetalle> SIMF_DOCUMENTO_EliminarRegistroExoneracion(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @PathVariable("pvn_IdDocumento") Integer pvn_IdDocumento,
            @RequestParam("idUsuarioResponsable") Integer pvn_IdUsuarioResponsable) {

        EntExoneracionDetalle vlo_Detalle = this.vgo_BllExoneracion.EliminarDocumento(
                pvc_ReferenciaInterna, pvn_IdDocumento, pvn_IdUsuarioResponsable);
        return ResponseEntity.ok(vlo_Detalle);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 23/09/2026
     * Descripción: Cambia la referencia interna de un expediente. Cuando la sigla nueva
     *   pertenece a otro tipo de trámite, el expediente se reclasifica conservando su
     *   documentación y su bitácora.
     *
     * @param pvc_ReferenciaInterna referencia interna actual del expediente
     * @param pvo_Cambio referencia nueva, justificación y datos del tipo destino
     * @return referencia anterior y nueva, tipo resultante y si hubo reclasificación
     */
    @PreAuthorize(ReglasAcceso.MODIFICAR_TRAMITE_POR_REFERENCIA)
    @PutMapping(path = "/{pvc_ReferenciaInterna}/referencia",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntRespuestaCambioReferencia> SIMT_TRAMITE_ModificarReferencia(
            @PathVariable String pvc_ReferenciaInterna,
            @RequestBody EntCambioReferencia pvo_Cambio) {

        EntRespuestaCambioReferencia vlo_Respuesta =
                this.vgo_BllTramite.CambiarReferencia(pvc_ReferenciaInterna, pvo_Cambio);
        return ResponseEntity.ok(vlo_Respuesta);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 25/09/2026
     * Descripción: Clasifica un documento del expediente de desalmacenaje especial (HU-045).
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvn_IdDocumento documento que se clasifica
     * @param pvo_Clasificacion tipo de documento y usuario responsable
     * @return expediente con su documentación actualizada
     */
    @PreAuthorize(ReglasAcceso.DESALMACENAJE_MODIFICAR)
    @PutMapping(path = "/desalmacenaje-especial/{pvc_ReferenciaInterna}/documentos/"
            + "{pvn_IdDocumento}/clasificacion", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntExpedienteDetalle> SIMF_DOCUMENTO_ModificarClasificacion(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @PathVariable("pvn_IdDocumento") Integer pvn_IdDocumento,
            @RequestBody EntEstadoDocumental pvo_Clasificacion) {

        EntExpedienteDetalle vlo_Detalle = this.vgo_BllDesalmacenajeEspecial.ClasificarDocumento(
                pvc_ReferenciaInterna,
                pvn_IdDocumento,
                pvo_Clasificacion.getTipoDocumento(),
                pvo_Clasificacion.getIdUsuarioResponsable());
        return ResponseEntity.ok(vlo_Detalle);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 26/09/2026
     * Descripción: Registra los datos de costo del expediente que totaliza el reporte de
     *   consultas internas (HU-036).
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvo_Costos partido responsable, impuestos y bodegaje
     * @return confirmación del registro
     */
    @PreAuthorize(ReglasAcceso.MODIFICAR_TRAMITE_POR_REFERENCIA)
    @PutMapping(path = "/{pvc_ReferenciaInterna}/costos", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> SIMT_TRAMITE_ModificarCostos(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestBody EntCostosTramite pvo_Costos) {

        EntTramite vlo_EnTramite = this.vgo_BllTramite.RegistrarCostos(pvc_ReferenciaInterna, pvo_Costos);
        Map<String, Object> vlo_Respuesta = new LinkedHashMap<>();
        vlo_Respuesta.put("mensaje", Constantes.MENSAJE_COSTOS_MODIFICADOS);
        vlo_Respuesta.put("referenciaInterna", vlo_EnTramite.getReferenciaInterna());
        vlo_Respuesta.put("tipoPartidoResponsable", vlo_EnTramite.getTipoPartidoResponsable());
        vlo_Respuesta.put("partidoResponsable", vlo_EnTramite.getPartidoResponsable());
        vlo_Respuesta.put("impuestosPagados", vlo_EnTramite.getImpuestosPagados());
        vlo_Respuesta.put("impuestosExonerados", vlo_EnTramite.getImpuestosExonerados());
        vlo_Respuesta.put("montoBodegaje", vlo_EnTramite.getMontoBodegaje());
        return ResponseEntity.ok(vlo_Respuesta);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 26/09/2026
     * Descripción: Entrega el catálogo de tipos de trámite vigentes, con su sigla, para que las
     *   pantallas no repitan los códigos.
     *
     * @return tipos de trámite activos
     */
    @PreAuthorize(ReglasAcceso.CON_SESION)
    @GetMapping("/catalogos/tipos-tramite")
    public ResponseEntity<List<EntTipoTramite>> SIMM_TIPO_TRAMITE_ObtenerLista() {
        return ResponseEntity.ok(this.vgo_BllCatalogo.ObtenerTiposTramite());
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 26/09/2026
     * Descripción: Entrega el catálogo de tipos de documento vigentes, que la pantalla de
     *   gestión documental usa para clasificar los archivos del expediente.
     *
     * @return tipos de documento activos
     */
    @PreAuthorize(ReglasAcceso.CON_SESION)
    @GetMapping("/catalogos/tipos-documento")
    public ResponseEntity<List<EntTipoDocumento>> SIMM_TIPO_DOCUMENTO_ObtenerLista() {
        return ResponseEntity.ok(this.vgo_BllCatalogo.ObtenerTiposDocumento());
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 25/09/2026
     * Descripción: Guarda el estado documental del expediente de desalmacenaje especial y sus
     *   observaciones (HU-045).
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvo_Estado estado documental, observaciones y usuario responsable
     * @return expediente con su estado documental actualizado
     */
    @PreAuthorize(ReglasAcceso.DESALMACENAJE_MODIFICAR)
    @PutMapping(path = "/desalmacenaje-especial/{pvc_ReferenciaInterna}/estado-documental",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntExpedienteDetalle> SIMT_DESALMACENAJE_ESPEC_ModificarEstadoDocumental(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestBody EntEstadoDocumental pvo_Estado) {

        EntExpedienteDetalle vlo_Detalle = this.vgo_BllDesalmacenajeEspecial
                .GuardarEstadoDocumental(pvc_ReferenciaInterna, pvo_Estado);
        return ResponseEntity.ok(vlo_Detalle);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 25/09/2026
     * Descripción: Genera el reporte de consultas internas (HU-036) con los criterios de
     *   filtrado y el criterio de agrupación indicados.
     *
     * @param pvd_FechaDesde inicio del rango de fechas, opcional
     * @param pvd_FechaHasta fin del rango de fechas, opcional e incluido en el reporte
     * @param pvc_TipoTramite código del tipo de trámite, opcional
     * @param pvc_RegimenAduanero régimen aduanero, opcional
     * @param pvc_TipoExoneracion tipo de exoneración, opcional
     * @param pvc_Dependencia partido o dependencia responsable del costo, opcional
     * @param pvo_LsAgrupaciones criterios de agrupación del reporte
     * @param pvb_EnumerarTramites true para incluir la lista de trámites
     * @param pvb_CombinarCriterios true para cruzar los criterios en una sola tabla
     * @param pvb_IncluirTotalCif true para incluir la columna del monto CIF
     * @param pvb_IncluirTotalBodegaje true para incluir la columna de bodegaje
     * @param pvb_IncluirTotalCif true para incluir la columna del monto CIF
     * @param pvb_IncluirTotalBodegaje true para incluir la columna de bodegaje
     * @param pvb_CombinarCriterios true para cruzar los criterios en una sola tabla
     * @return reporte tabulado con sus totales y desgloses
     */
    @PreAuthorize(ReglasAcceso.REPORTE_CONSULTAR)
    @GetMapping("/reportes/consultas-internas")
    public ResponseEntity<EntReporteConsultas> SIMT_TRAMITE_ObtenerReporteConsultas(
            @RequestParam(value = "fechaDesde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pvd_FechaDesde,
            @RequestParam(value = "fechaHasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pvd_FechaHasta,
            @RequestParam(value = "tipoTramite", required = false) String pvc_TipoTramite,
            @RequestParam(value = "regimenAduanero", required = false) String pvc_RegimenAduanero,
            @RequestParam(value = "tipoExoneracion", required = false) String pvc_TipoExoneracion,
            @RequestParam(value = "dependencia", required = false) String pvc_Dependencia,
            @RequestParam(value = "agruparPor", required = false) List<String> pvo_LsAgrupaciones,
            @RequestParam(value = "enumerarTramites", required = false, defaultValue = "false")
            boolean pvb_EnumerarTramites,
            @RequestParam(value = "combinarCriterios", required = false, defaultValue = "false")
            boolean pvb_CombinarCriterios,
            @RequestParam(value = "incluirTotalCif", required = false, defaultValue = "true")
            boolean pvb_IncluirTotalCif,
            @RequestParam(value = "incluirTotalBodegaje", required = false, defaultValue = "true")
            boolean pvb_IncluirTotalBodegaje) {

        EntFiltroReporte vlo_Filtro = new EntFiltroReporte();
        vlo_Filtro.setFechaDesde(pvd_FechaDesde);
        vlo_Filtro.setFechaHasta(pvd_FechaHasta);
        vlo_Filtro.setCodigoTipoTramite(this.NormalizarFiltroReporte(pvc_TipoTramite));
        vlo_Filtro.setRegimenAduanero(this.NormalizarFiltroReporte(pvc_RegimenAduanero));
        vlo_Filtro.setTipoExoneracion(this.NormalizarFiltroReporte(pvc_TipoExoneracion));
        vlo_Filtro.setOrigenDependencia(this.NormalizarFiltroReporte(pvc_Dependencia));
        vlo_Filtro.setAgrupaciones(pvo_LsAgrupaciones);
        vlo_Filtro.setEnumerarTramites(pvb_EnumerarTramites);
        vlo_Filtro.setCombinarCriterios(pvb_CombinarCriterios);
        vlo_Filtro.setIncluirTotalCif(pvb_IncluirTotalCif);
        vlo_Filtro.setIncluirTotalBodegaje(pvb_IncluirTotalBodegaje);

        return ResponseEntity.ok(this.vgo_BllReporte.Generar(vlo_Filtro));
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 25/09/2026
     * Descripción: Descarta los filtros del reporte que llegan vacíos, para que no lo acoten.
     *
     * @param pvc_Filtro valor recibido en la consulta
     * @return el valor sin espacios o null cuando viene vacío
     */
    private String NormalizarFiltroReporte(String pvc_Filtro) {
        return pvc_Filtro == null || pvc_Filtro.isBlank() ? null : pvc_Filtro.trim();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Descarga el reporte de consultas internas (HU-036) en el formato indicado,
     *   con los mismos criterios con los que se generó en la pantalla.
     *
     * @param pvc_Formato formato del archivo: EXCEL o PDF
     * @param pvd_FechaDesde inicio del rango de fechas, opcional
     * @param pvd_FechaHasta fin del rango de fechas, opcional e incluido en el reporte
     * @param pvc_TipoTramite código del tipo de trámite, opcional
     * @param pvc_RegimenAduanero régimen aduanero, opcional
     * @param pvc_TipoExoneracion tipo de exoneración, opcional
     * @param pvc_Dependencia partido o dependencia responsable del costo, opcional
     * @param pvo_LsAgrupaciones criterios de agrupación del reporte
     * @param pvb_EnumerarTramites true para incluir la lista de trámites
     * @return archivo del reporte listo para descargar
     */
    @PreAuthorize(ReglasAcceso.REPORTE_EXPORTAR)
    @GetMapping("/reportes/consultas-internas/exportar/{pvc_Formato}")
    public ResponseEntity<byte[]> SIMT_TRAMITE_ExportarReporteConsultas(
            @PathVariable("pvc_Formato") String pvc_Formato,
            @RequestParam(value = "fechaDesde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pvd_FechaDesde,
            @RequestParam(value = "fechaHasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pvd_FechaHasta,
            @RequestParam(value = "tipoTramite", required = false) String pvc_TipoTramite,
            @RequestParam(value = "regimenAduanero", required = false) String pvc_RegimenAduanero,
            @RequestParam(value = "tipoExoneracion", required = false) String pvc_TipoExoneracion,
            @RequestParam(value = "dependencia", required = false) String pvc_Dependencia,
            @RequestParam(value = "agruparPor", required = false) List<String> pvo_LsAgrupaciones,
            @RequestParam(value = "enumerarTramites", required = false, defaultValue = "false")
            boolean pvb_EnumerarTramites,
            @RequestParam(value = "combinarCriterios", required = false, defaultValue = "false")
            boolean pvb_CombinarCriterios,
            @RequestParam(value = "incluirTotalCif", required = false, defaultValue = "true")
            boolean pvb_IncluirTotalCif,
            @RequestParam(value = "incluirTotalBodegaje", required = false, defaultValue = "true")
            boolean pvb_IncluirTotalBodegaje) {

        EntFiltroReporte vlo_Filtro = new EntFiltroReporte();
        vlo_Filtro.setFechaDesde(pvd_FechaDesde);
        vlo_Filtro.setFechaHasta(pvd_FechaHasta);
        vlo_Filtro.setCodigoTipoTramite(this.NormalizarFiltroReporte(pvc_TipoTramite));
        vlo_Filtro.setRegimenAduanero(this.NormalizarFiltroReporte(pvc_RegimenAduanero));
        vlo_Filtro.setTipoExoneracion(this.NormalizarFiltroReporte(pvc_TipoExoneracion));
        vlo_Filtro.setOrigenDependencia(this.NormalizarFiltroReporte(pvc_Dependencia));
        vlo_Filtro.setAgrupaciones(pvo_LsAgrupaciones);
        vlo_Filtro.setEnumerarTramites(pvb_EnumerarTramites);
        vlo_Filtro.setCombinarCriterios(pvb_CombinarCriterios);
        vlo_Filtro.setIncluirTotalCif(pvb_IncluirTotalCif);
        vlo_Filtro.setIncluirTotalBodegaje(pvb_IncluirTotalBodegaje);

        EntArchivoExportado vlo_Archivo = this.vgo_BllReporte.Exportar(vlo_Filtro, pvc_Formato);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(vlo_Archivo.getNombreArchivo(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentType(MediaType.parseMediaType(vlo_Archivo.getTipoContenido()))
                .body(vlo_Archivo.getContenido());
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Corrige los datos ya guardados del expediente de desalmacenaje especial
     *   (HU-032). Cada cambio queda anotado en la bitácora con su valor anterior.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvo_Correccion datos corregidos y usuario responsable
     * @return expediente con sus datos actualizados
     */
    @PreAuthorize(ReglasAcceso.DESALMACENAJE_MODIFICAR)
    @PutMapping(path = "/desalmacenaje-especial/{pvc_ReferenciaInterna}/datos",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntExpedienteDetalle> SIMT_DESALMACENAJE_ESPEC_ModificarDatos(
            @PathVariable("pvc_ReferenciaInterna") String pvc_ReferenciaInterna,
            @RequestBody EntCorreccionDesalmacenaje pvo_Correccion) {

        EntExpedienteDetalle vlo_Detalle = this.vgo_BllDesalmacenajeEspecial.ModificarDatos(
                pvc_ReferenciaInterna, pvo_Correccion);
        return ResponseEntity.ok(vlo_Detalle);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Entrega los partidos responsables del costo que los expedientes tienen
     *   registrados, para ofrecerlos como filtro del reporte (HU-036).
     *
     * @return partidos registrados
     */
    @PreAuthorize(ReglasAcceso.REPORTE_CONSULTAR)
    @GetMapping("/reportes/consultas-internas/partidos")
    public ResponseEntity<List<String>> SIMT_TRAMITE_ObtenerPartidosResponsables() {
        return ResponseEntity.ok(this.vgo_BllReporte.ObtenerPartidosResponsables());
    }
}
