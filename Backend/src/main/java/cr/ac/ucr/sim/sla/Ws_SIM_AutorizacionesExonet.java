package cr.ac.ucr.sim.sla;

import cr.ac.ucr.sim.seguridad.ReglasAcceso;
import org.springframework.security.access.prepost.PreAuthorize;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntAutorizacionExonet;
import cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones.BllAutorizacionExonet;
import cr.ac.ucr.sim.seguridad.UsuarioAutenticado;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloAnulacionAutorizacionSolicitud;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloAutorizacionExonetRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloAutorizacionExonetSolicitud;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloHistorialAutorizacionesRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloPrecargaAutorizacionRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloSustitucionAutorizacionSolicitud;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Servicio REST de las solicitudes / autorizaciones EXONET de un expediente de
 *   exoneración (HU-023). Comparte la ruta base del módulo de exoneraciones con
 *   Ws_SIM_Exoneraciones (HU-019 y HU-020), pero vive en su propia clase para no modificar ese
 *   servicio. Si la ruta base de Ws_SIM_Exoneraciones cambia, esta debe cambiar igual.
 *   - El usuario se obtiene siempre de la sesión autenticada; ningún servicio lo recibe en la
 *     solicitud.
 *   - Registrar, completar y sustituir viajan como multipart/form-data: los datos se enlazan con
 *     @ModelAttribute y el documento opcional llega aparte en la parte "archivo".
 *   - Toda operación devuelve el historial actualizado del expediente, para que la pantalla no
 *     necesite una segunda consulta.
 *   - Nunca devuelve entidades: las convierte en modelos de respuesta.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 *   19/10/2026 Equipo de Desarrollo SIM: restricciones por rol (HU-003). Consultar exige
 *     EXONERACION_CONSULTAR y registrar, completar, anular o sustituir exige EXONERACION_MODIFICAR.
 */
@RestController
@RequestMapping("/api/v1/exoneraciones")
public class Ws_SIM_AutorizacionesExonet {

    private final BllAutorizacionExonet vgo_BllAutorizacionExonet;

    public Ws_SIM_AutorizacionesExonet(BllAutorizacionExonet pvo_BllAutorizacionExonet) {
        this.vgo_BllAutorizacionExonet = pvo_BllAutorizacionExonet;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene la autorización vigente, el historial completo y, mientras no exista
     *   historial, la precarga con los números EXONET registrados en la HU-018.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvo_Usuario usuario autenticado (la consulta no lo usa, pero exige sesión)
     * @return historial de autorizaciones del expediente
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_CONSULTAR)
    @GetMapping("/{vln_IdTramite}/autorizaciones")
    public ResponseEntity<ModeloHistorialAutorizacionesRespuesta> SIMT_AUTORIZACION_EXONET_ObtenerLista(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(this.ArmarHistorial(pvn_IdTramite));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Registra la autorización vigente de un expediente que no tiene ninguna.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvo_Solicitud datos de la solicitud / autorización EXONET
     * @param pvo_Archivo documento de respaldo, opcional
     * @param pvo_Usuario usuario autenticado, que queda como responsable del registro
     * @return historial actualizado
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @PostMapping(path = "/{vln_IdTramite}/autorizaciones", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModeloHistorialAutorizacionesRespuesta> SIMT_AUTORIZACION_EXONET_InsertarRegistro(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite,
            @ModelAttribute ModeloAutorizacionExonetSolicitud pvo_Solicitud,
            @RequestParam(value = "archivo", required = false) MultipartFile pvo_Archivo,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        this.vgo_BllAutorizacionExonet.RegistrarAutorizacion(
                pvn_IdTramite,
                this.ObtenerIdUsuario(pvo_Usuario),
                pvo_Solicitud.getNumeroSolicitudExonet(),
                pvo_Solicitud.getFechaEnvioExonet(),
                pvo_Solicitud.getNumeroAutorizacion(),
                pvo_Solicitud.getFechaAutorizacion(),
                pvo_Solicitud.getFechaComunicacion(),
                pvo_Solicitud.getObservaciones(),
                pvo_Archivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.ArmarHistorial(pvn_IdTramite));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Completa datos vacíos de la autorización vigente. Usa POST porque viaja como
     *   multipart, igual que el resto de servicios del proyecto que reciben archivos.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvn_IdAutorizacion autorización vigente que se completa
     * @param pvo_Solicitud datos que se completan; los vacíos se ignoran
     * @param pvo_Archivo documento de respaldo, si la autorización no tenía
     * @param pvo_Usuario usuario autenticado
     * @return historial actualizado
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @PostMapping(path = "/{vln_IdTramite}/autorizaciones/{vln_IdAutorizacion}/completar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModeloHistorialAutorizacionesRespuesta> SIMT_AUTORIZACION_EXONET_CompletarRegistro(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite,
            @PathVariable("vln_IdAutorizacion") Long pvn_IdAutorizacion,
            @ModelAttribute ModeloAutorizacionExonetSolicitud pvo_Solicitud,
            @RequestParam(value = "archivo", required = false) MultipartFile pvo_Archivo,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        this.vgo_BllAutorizacionExonet.CompletarAutorizacion(
                pvn_IdTramite,
                pvn_IdAutorizacion,
                this.ObtenerIdUsuario(pvo_Usuario),
                pvo_Solicitud.getNumeroSolicitudExonet(),
                pvo_Solicitud.getFechaEnvioExonet(),
                pvo_Solicitud.getNumeroAutorizacion(),
                pvo_Solicitud.getFechaAutorizacion(),
                pvo_Solicitud.getFechaComunicacion(),
                pvo_Solicitud.getObservaciones(),
                pvo_Archivo);
        return ResponseEntity.ok(this.ArmarHistorial(pvn_IdTramite));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Anula la autorización vigente sin borrarla.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvn_IdAutorizacion autorización vigente que se anula
     * @param pvo_Solicitud motivo de la anulación
     * @param pvo_Usuario usuario autenticado
     * @return historial actualizado
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @PostMapping(path = "/{vln_IdTramite}/autorizaciones/{vln_IdAutorizacion}/anulacion",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ModeloHistorialAutorizacionesRespuesta> SIMT_AUTORIZACION_EXONET_AnularRegistro(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite,
            @PathVariable("vln_IdAutorizacion") Long pvn_IdAutorizacion,
            @RequestBody ModeloAnulacionAutorizacionSolicitud pvo_Solicitud,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        this.vgo_BllAutorizacionExonet.AnularAutorizacion(
                pvn_IdTramite,
                pvn_IdAutorizacion,
                this.ObtenerIdUsuario(pvo_Usuario),
                pvo_Solicitud == null ? null : pvo_Solicitud.getMotivo());
        return ResponseEntity.ok(this.ArmarHistorial(pvn_IdTramite));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Sustituye la autorización vigente por una nueva, conservando ambas.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvn_IdAutorizacion autorización vigente que se sustituye
     * @param pvo_Solicitud motivo y datos completos de la nueva autorización
     * @param pvo_Archivo documento de respaldo de la nueva, opcional
     * @param pvo_Usuario usuario autenticado
     * @return historial actualizado
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @PostMapping(path = "/{vln_IdTramite}/autorizaciones/{vln_IdAutorizacion}/sustitucion",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModeloHistorialAutorizacionesRespuesta> SIMT_AUTORIZACION_EXONET_SustituirRegistro(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite,
            @PathVariable("vln_IdAutorizacion") Long pvn_IdAutorizacion,
            @ModelAttribute ModeloSustitucionAutorizacionSolicitud pvo_Solicitud,
            @RequestParam(value = "archivo", required = false) MultipartFile pvo_Archivo,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        this.vgo_BllAutorizacionExonet.SustituirAutorizacion(
                pvn_IdTramite,
                pvn_IdAutorizacion,
                this.ObtenerIdUsuario(pvo_Usuario),
                pvo_Solicitud.getMotivo(),
                pvo_Solicitud.getNumeroSolicitudExonet(),
                pvo_Solicitud.getFechaEnvioExonet(),
                pvo_Solicitud.getNumeroAutorizacion(),
                pvo_Solicitud.getFechaAutorizacion(),
                pvo_Solicitud.getFechaComunicacion(),
                pvo_Solicitud.getObservaciones(),
                pvo_Archivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.ArmarHistorial(pvn_IdTramite));
    }

    // -----------------------------------------------------------------------
    // Conversión a modelos de respuesta.
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Arma la respuesta de la consulta a partir de una sola lectura del historial:
     *   la vigente se toma del propio historial, la sustituta de cada fila se calcula en memoria
     *   y los usuarios y documentos se resuelven una sola vez por identificador. La precarga solo
     *   se consulta cuando el historial está vacío.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @return historial listo para la pantalla
     */
    private ModeloHistorialAutorizacionesRespuesta ArmarHistorial(Integer pvn_IdTramite) {
        List<EntAutorizacionExonet> vlo_LsAutorizaciones =
                this.vgo_BllAutorizacionExonet.ObtenerHistorial(pvn_IdTramite);
        Map<Integer, String> vlo_MpNombres =
                this.vgo_BllAutorizacionExonet.ObtenerNombresUsuarios(vlo_LsAutorizaciones);
        Map<Integer, EntDocumento> vlo_MpDocumentos =
                this.vgo_BllAutorizacionExonet.ObtenerDocumentos(vlo_LsAutorizaciones);

        // Sustituta de cada autorización: la fila cuyo ID_AUTORIZACION_ANTERIOR apunta a ella.
        Map<Long, Long> vlo_MpSustitutas = new HashMap<>();
        for (EntAutorizacionExonet vlo_EnAutorizacion : vlo_LsAutorizaciones) {
            if (vlo_EnAutorizacion.getIdAutorizacionAnterior() != null) {
                vlo_MpSustitutas.put(
                        vlo_EnAutorizacion.getIdAutorizacionAnterior(),
                        vlo_EnAutorizacion.getIdAutorizacionExonet());
            }
        }

        ModeloHistorialAutorizacionesRespuesta vlo_Respuesta = new ModeloHistorialAutorizacionesRespuesta();
        vlo_Respuesta.setIdTramite(pvn_IdTramite);
        List<ModeloAutorizacionExonetRespuesta> vlo_LsHistorial = new ArrayList<>();
        for (EntAutorizacionExonet vlo_EnAutorizacion : vlo_LsAutorizaciones) {
            ModeloAutorizacionExonetRespuesta vlo_Modelo = this.ConvertirAutorizacion(
                    vlo_EnAutorizacion, vlo_MpNombres, vlo_MpDocumentos, vlo_MpSustitutas);
            vlo_LsHistorial.add(vlo_Modelo);
            if (Boolean.TRUE.equals(vlo_Modelo.getVigente())) {
                vlo_Respuesta.setVigente(vlo_Modelo);
            }
        }
        vlo_Respuesta.setHistorial(vlo_LsHistorial);

        if (vlo_LsAutorizaciones.isEmpty()) {
            vlo_Respuesta.setPrecarga(this.ConvertirPrecarga(
                    this.vgo_BllAutorizacionExonet.ObtenerPrecargaHistorica(pvn_IdTramite)));
        }
        return vlo_Respuesta;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Convierte una autorización en su modelo de respuesta, con los nombres de
     *   usuario, el documento y la sustituta ya resueltos.
     */
    private ModeloAutorizacionExonetRespuesta ConvertirAutorizacion(
            EntAutorizacionExonet pvo_EnAutorizacion,
            Map<Integer, String> pvo_MpNombres,
            Map<Integer, EntDocumento> pvo_MpDocumentos,
            Map<Long, Long> pvo_MpSustitutas) {
        ModeloAutorizacionExonetRespuesta vlo_Modelo = new ModeloAutorizacionExonetRespuesta();
        vlo_Modelo.setIdAutorizacion(pvo_EnAutorizacion.getIdAutorizacionExonet());
        vlo_Modelo.setIdTramite(pvo_EnAutorizacion.getIdTramite());
        vlo_Modelo.setNumeroSolicitudExonet(pvo_EnAutorizacion.getNumeroSolicitudExonet());
        vlo_Modelo.setFechaEnvioExonet(pvo_EnAutorizacion.getFechaEnvioExonet());
        vlo_Modelo.setNumeroAutorizacion(pvo_EnAutorizacion.getNumeroAutorizacion());
        vlo_Modelo.setFechaAutorizacion(pvo_EnAutorizacion.getFechaAutorizacion());
        vlo_Modelo.setFechaComunicacion(pvo_EnAutorizacion.getFechaComunicacion());
        vlo_Modelo.setObservaciones(pvo_EnAutorizacion.getObservaciones());

        String vlc_Estado = pvo_EnAutorizacion.getEstadoAutorizacion();
        vlo_Modelo.setEstado(vlc_Estado);
        vlo_Modelo.setNombreEstado(this.NombrarEstado(vlc_Estado));
        vlo_Modelo.setVigente(Constantes.ESTADO_AUTORIZACION_VIGENTE.equals(vlc_Estado));

        vlo_Modelo.setIdAutorizacionAnterior(pvo_EnAutorizacion.getIdAutorizacionAnterior());
        vlo_Modelo.setIdAutorizacionSustituta(pvo_MpSustitutas.get(pvo_EnAutorizacion.getIdAutorizacionExonet()));

        vlo_Modelo.setUsuarioRegistro(pvo_MpNombres.get(pvo_EnAutorizacion.getIdUsuarioRegistro()));
        vlo_Modelo.setFechaRegistro(pvo_EnAutorizacion.getFechaRegistro());
        vlo_Modelo.setUsuarioCambioEstado(pvo_EnAutorizacion.getIdUsuarioCambioEstado() == null
                ? null
                : pvo_MpNombres.get(pvo_EnAutorizacion.getIdUsuarioCambioEstado()));
        vlo_Modelo.setFechaCambioEstado(pvo_EnAutorizacion.getFechaCambioEstado());
        vlo_Modelo.setMotivoCambioEstado(pvo_EnAutorizacion.getMotivoCambioEstado());

        EntDocumento vlo_EnDocumento = pvo_EnAutorizacion.getIdDocumento() == null
                ? null
                : pvo_MpDocumentos.get(pvo_EnAutorizacion.getIdDocumento());
        vlo_Modelo.setIdDocumento(pvo_EnAutorizacion.getIdDocumento());
        vlo_Modelo.setNombreArchivoDocumento(vlo_EnDocumento == null ? null : vlo_EnDocumento.getNombreArchivo());
        vlo_Modelo.setEstadoDocumento(vlo_EnDocumento == null ? null : vlo_EnDocumento.getEstadoDocumento());
        return vlo_Modelo;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Expone únicamente los dos números EXONET de la HU-018 que sirven de precarga.
     *
     * @param pvo_EnExoneracion datos de exoneración, o null si no aplica la precarga
     * @return precarga, o null
     */
    private ModeloPrecargaAutorizacionRespuesta ConvertirPrecarga(EntExoneracion pvo_EnExoneracion) {
        if (pvo_EnExoneracion == null) {
            return null;
        }
        ModeloPrecargaAutorizacionRespuesta vlo_Precarga = new ModeloPrecargaAutorizacionRespuesta();
        vlo_Precarga.setNumeroSolicitudExonet(
                this.NormalizarPrecarga(pvo_EnExoneracion.getNumeroExonet()));
        vlo_Precarga.setNumeroAutorizacion(
                this.NormalizarPrecarga(pvo_EnExoneracion.getNumeroAutorizacionExonet()));
        return vlo_Precarga;
    }

    private String NormalizarPrecarga(String pvc_Valor) {
        if (pvc_Valor == null) {
            return null;
        }
        String vlc_Valor = pvc_Valor.trim();

        if (vlc_Valor.isEmpty()
                || "Nulo".equalsIgnoreCase(vlc_Valor)
                || "No aplica".equalsIgnoreCase(vlc_Valor)) {
            return null;
        }
        return vlc_Valor.toUpperCase(java.util.Locale.ROOT);
    }

    private String NombrarEstado(String pvc_Estado) {
        if (Constantes.ESTADO_AUTORIZACION_VIGENTE.equals(pvc_Estado)) {
            return Constantes.NOMBRE_ESTADO_AUTORIZACION_VIGENTE;
        }
        if (Constantes.ESTADO_AUTORIZACION_ANULADA.equals(pvc_Estado)) {
            return Constantes.NOMBRE_ESTADO_AUTORIZACION_ANULADA;
        }
        if (Constantes.ESTADO_AUTORIZACION_SUSTITUIDA.equals(pvc_Estado)) {
            return Constantes.NOMBRE_ESTADO_AUTORIZACION_SUSTITUIDA;
        }
        return pvc_Estado;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene el identificador del usuario autenticado. Si no hay sesión devuelve
     *   null y la lógica de negocio rechaza la operación, igual que en la HU-020.
     */
    private Integer ObtenerIdUsuario(UsuarioAutenticado pvo_Usuario) {
        return pvo_Usuario == null ? null : pvo_Usuario.idUsuario();
    }
}
