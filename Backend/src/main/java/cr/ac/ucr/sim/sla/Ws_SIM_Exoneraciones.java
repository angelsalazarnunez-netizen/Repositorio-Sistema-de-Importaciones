package cr.ac.ucr.sim.sla;

import cr.ac.ucr.sim.seguridad.ReglasAcceso;
import org.springframework.security.access.prepost.PreAuthorize;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntObservacionExpediente;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntRevisionDocumento;
import cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones.BllAprobacionExoneracion;
import cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones.BllControlDocumental;
import cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones.BllObservacionExpediente;
import cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones.BllRevisionDocumento;
import cr.ac.ucr.sim.seguridad.UsuarioAutenticado;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 03/10/2026
 * Descripcion: servicio REST del modulo de exoneraciones.
 * Expone las operaciones correspondientes a la revision
 * documental de las solicitudes de exoneracion.
 *
 * Historial de cambios:
 *   04/10/2026 Equipo de Desarrollo SIM: se agregan los servicios de observaciones y
 *     aclaraciones del expediente (HU-020). El usuario se obtiene siempre de la sesion
 *     autenticada; ningun servicio de observaciones lo recibe en la solicitud.
 *   05/10/2026 Equipo de Desarrollo SIM: se agregan el control de documentos obligatorios y
 *     la continuacion del expediente a aprobacion (HU-021).
 *   05/10/2026 Equipo de Desarrollo SIM: se agregan la bandeja de aprobaciones y el registro de
 *     la decision de la jefatura (HU-022).
 *   19/10/2026 Equipo de Desarrollo SIM: restricciones por rol (HU-003). Cada servicio declara su
 *     permiso con @PreAuthorize (ver ReglasAcceso): consultar o modificar exoneraciones, o
 *     aprobarlas en la bandeja y la decision.
 */
@RestController
@RequestMapping("/api/v1/exoneraciones")
public class Ws_SIM_Exoneraciones {

    private final BllRevisionDocumento vgo_BllRevisionDocumento;
    private final BllObservacionExpediente vgo_BllObservacionExpediente;
    private final BllControlDocumental vgo_BllControlDocumental;
    private final BllAprobacionExoneracion vgo_BllAprobacionExoneracion;

    public Ws_SIM_Exoneraciones(
            BllRevisionDocumento vlo_BllRevisionDocumento,
            BllObservacionExpediente vlo_BllObservacionExpediente,
            BllControlDocumental vlo_BllControlDocumental,
            BllAprobacionExoneracion vlo_BllAprobacionExoneracion
    ) {

        this.vgo_BllAprobacionExoneracion =
                vlo_BllAprobacionExoneracion;

        this.vgo_BllControlDocumental =
                vlo_BllControlDocumental;

        this.vgo_BllRevisionDocumento =
                vlo_BllRevisionDocumento;

        this.vgo_BllObservacionExpediente =
                vlo_BllObservacionExpediente;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: obtiene el checklist documental
     * correspondiente a una solicitud de exoneracion.
     *
     * @param vln_IdTramite identificador del tramite.
     * @return requisitos documentales junto con el
     * estado actual de sus documentos y revisiones.
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_CONSULTAR)
    @GetMapping("/{vln_IdTramite}/documentos/revision")
    public ResponseEntity<List<ModeloRevisionDocumentoRespuesta>>
    SIMT_REVISION_DOCUMENTO_ObtenerChecklist(
            @PathVariable("vln_IdTramite")
            Integer vln_IdTramite
    ) {

        return ResponseEntity.ok(
                vgo_BllRevisionDocumento
                        .ObtenerChecklistRevision(
                                vln_IdTramite
                        )
        );
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: registra o modifica el resultado
     * de revision del documento activo indicado.
     *
     * @param vln_IdTramite identificador del tramite.
     * @param vln_IdDocumento identificador del documento.
     * @param vlo_Solicitud estado y observacion de revision.
     * @param vlo_Usuario usuario autenticado responsable.
     * @return revision documental registrada.
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @PatchMapping(
            "/{vln_IdTramite}/documentos/"
                    + "{vln_IdDocumento}/revision"
    )
    public ResponseEntity<ModeloRevisionRegistradaRespuesta>
    SIMT_REVISION_DOCUMENTO_ModificarRegistro(
            @PathVariable("vln_IdTramite")
            Integer vln_IdTramite,

            @PathVariable("vln_IdDocumento")
            Integer vln_IdDocumento,

            @RequestBody
            ModeloRevisionDocumentoSolicitud vlo_Solicitud,

            @AuthenticationPrincipal
            UsuarioAutenticado vlo_Usuario
    ) {

        EntRevisionDocumento vlo_EnRevision =
                vgo_BllRevisionDocumento
                        .RegistrarRevision(
                                vln_IdTramite,
                                vln_IdDocumento,
                                Long.valueOf(
                                        vlo_Usuario.idUsuario()
                                ),
                                vlo_Solicitud.getEstadoRevision(),
                                vlo_Solicitud.getObservacion()
                        );

        ModeloRevisionRegistradaRespuesta vlo_Respuesta =
                new ModeloRevisionRegistradaRespuesta();

        vlo_Respuesta.setIdRevisionDocumento(
                vlo_EnRevision.getIdRevisionDocumento()
        );

        vlo_Respuesta.setIdDocumento(
                vlo_EnRevision
                        .getDocumento()
                        .getIdDocumento()
        );

        vlo_Respuesta.setEstadoRevision(
                vlo_EnRevision.getEstadoRevision()
        );

        vlo_Respuesta.setObservacion(
                vlo_EnRevision.getObservacion()
        );

        vlo_Respuesta.setFechaRevision(
                vlo_EnRevision.getFechaHora()
        );

        return ResponseEntity.ok(
                vlo_Respuesta
        );
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: obtiene el historial completo de
     * revisiones de un documento del expediente.
     *
     * @param vln_IdTramite identificador del tramite.
     * @param vln_IdDocumento identificador del documento.
     * @return revisiones historicas del documento.
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_CONSULTAR)
    @GetMapping(
            "/{vln_IdTramite}/documentos/"
                    + "{vln_IdDocumento}/revision/historial"
    )
    public ResponseEntity<List<ModeloHistorialRevisionRespuesta>>
    SIMT_REVISION_DOCUMENTO_ObtenerHistorial(
            @PathVariable("vln_IdTramite")
            Integer vln_IdTramite,

            @PathVariable("vln_IdDocumento")
            Integer vln_IdDocumento
    ) {

        return ResponseEntity.ok(
                vgo_BllRevisionDocumento
                        .ObtenerHistorial(
                                vln_IdTramite,
                                vln_IdDocumento
                        )
        );
    }

    // -----------------------------------------------------------------------
    // HU-021 Controlar documentos obligatorios del tramite.
    // Responsable: Equipo de Desarrollo SIM. Fecha: 05/10/2026.
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene el control de documentos obligatorios del expediente: el estado de cada
     *   documento exigido, la lista de pendientes y si hay cumplimiento documental.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return control documental del expediente
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_CONSULTA_O_APROBACION)
    @GetMapping("/{vln_IdTramite}/documentos/control")
    public ResponseEntity<ModeloControlDocumentalRespuesta> SIMT_REQUISITO_DOCUMENTAL_ObtenerControl(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite) {
        return ResponseEntity.ok(this.vgo_BllControlDocumental.ObtenerControlDocumental(pvn_IdTramite));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Continúa el expediente a aprobación. Se rechaza si la documentación obligatoria
     *   está incompleta. El usuario responsable se toma de la sesión autenticada.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvo_Usuario usuario autenticado
     * @return control documental del expediente ya en "Pendiente de aprobación"
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @PostMapping("/{vln_IdTramite}/aprobacion")
    public ResponseEntity<ModeloControlDocumentalRespuesta> SIMT_TRAMITE_ContinuarAprobacion(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(this.vgo_BllControlDocumental.ContinuarAprobacion(
                pvn_IdTramite, this.ObtenerIdUsuario(pvo_Usuario)));
    }

    // -----------------------------------------------------------------------
    // HU-022 Aprobar solicitud de exoneracion.
    // Responsable: Equipo de Desarrollo SIM. Fecha: 05/10/2026.
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene la bandeja de solicitudes pendientes de aprobación. Responde 403 si el
     *   usuario no tiene el permiso de aprobar exoneraciones.
     *
     * @param pvo_Usuario usuario autenticado
     * @return solicitudes pendientes de aprobación
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_APROBAR)
    @GetMapping("/aprobaciones/pendientes")
    public ResponseEntity<List<ModeloAprobacionPendienteRespuesta>> SIMT_DECISION_EXONERACION_ObtenerPendientes(
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(this.vgo_BllAprobacionExoneracion.ObtenerPendientes(this.ObtenerIdUsuario(pvo_Usuario)));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene el resumen del expediente para registrar la decisión, con el historial de
     *   decisiones y si el usuario autenticado puede decidir.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvo_Usuario usuario autenticado
     * @return resumen para la aprobación
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_CONSULTA_O_APROBACION)
    @GetMapping("/{vln_IdTramite}/decision")
    public ResponseEntity<ModeloAprobacionRespuesta> SIMT_DECISION_EXONERACION_ObtenerResumen(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(this.vgo_BllAprobacionExoneracion.ObtenerAprobacion(
                pvn_IdTramite, this.ObtenerIdUsuario(pvo_Usuario)));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Registra la decisión de la jefatura: aprobar, rechazar o solicitar modificación.
     *   El usuario responsable se toma de la sesión autenticada. Responde 403 si no está autorizado.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvo_Solicitud decisión y justificación
     * @param pvo_Usuario usuario autenticado
     * @return resumen del expediente con la decisión registrada
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_APROBAR)
    @PostMapping("/{vln_IdTramite}/decision")
    public ResponseEntity<ModeloAprobacionRespuesta> SIMT_DECISION_EXONERACION_InsertarRegistro(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite,
            @RequestBody ModeloDecisionSolicitud pvo_Solicitud,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.vgo_BllAprobacionExoneracion.RegistrarDecision(
                pvn_IdTramite,
                this.ObtenerIdUsuario(pvo_Usuario),
                pvo_Solicitud == null ? null : pvo_Solicitud.getDecision(),
                pvo_Solicitud == null ? null : pvo_Solicitud.getJustificacion()));
    }

    // -----------------------------------------------------------------------
    // HU-020 Registrar observaciones o aclaraciones del expediente.
    // Responsable: Equipo de Desarrollo SIM. Fecha: 04/10/2026.
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Obtiene las observaciones de un expediente de exoneración: los borradores
     *   propios del usuario autenticado y el historial confirmado, que es compartido.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvo_Usuario usuario autenticado
     * @return borradores propios e historial confirmado
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_CONSULTAR)
    @GetMapping("/{vln_IdTramite}/observaciones")
    public ResponseEntity<ModeloObservacionesExpedienteRespuesta> SIMT_OBSERVACION_EXPED_ObtenerLista(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(
                this.ArmarObservacionesExpediente(pvn_IdTramite, this.ObtenerIdUsuario(pvo_Usuario)));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Registra una observación en el expediente. Queda como borrador, salvo que la
     *   solicitud indique confirmar = true, en cuyo caso pasa directamente al historial.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvo_Solicitud motivo opcional, texto y si se confirma al registrarla
     * @param pvo_Usuario usuario autenticado, que queda como autor
     * @return observación registrada
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @PostMapping("/{vln_IdTramite}/observaciones")
    public ResponseEntity<ModeloObservacionRespuesta> SIMT_OBSERVACION_EXPED_InsertarRegistro(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite,
            @RequestBody ModeloObservacionSolicitud pvo_Solicitud,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        EntObservacionExpediente vlo_EnObservacion = this.vgo_BllObservacionExpediente.CrearObservacion(
                pvn_IdTramite,
                this.ObtenerIdUsuario(pvo_Usuario),
                pvo_Solicitud.getMotivo(),
                pvo_Solicitud.getObservacion(),
                Boolean.TRUE.equals(pvo_Solicitud.getConfirmar()));
        return ResponseEntity.status(HttpStatus.CREATED).body(this.ConvertirObservacion(vlo_EnObservacion));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Modifica el motivo y el texto de un borrador propio. El campo confirmar de la
     *   solicitud se ignora: un borrador se confirma con su propio servicio.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvn_IdObservacion identificador del borrador
     * @param pvo_Solicitud motivo opcional y texto nuevos
     * @param pvo_Usuario usuario autenticado
     * @return borrador modificado
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @PutMapping("/{vln_IdTramite}/observaciones/{vln_IdObservacion}")
    public ResponseEntity<ModeloObservacionRespuesta> SIMT_OBSERVACION_EXPED_ModificarRegistro(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite,
            @PathVariable("vln_IdObservacion") Long pvn_IdObservacion,
            @RequestBody ModeloObservacionSolicitud pvo_Solicitud,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        EntObservacionExpediente vlo_EnObservacion = this.vgo_BllObservacionExpediente.ModificarBorrador(
                pvn_IdTramite,
                pvn_IdObservacion,
                this.ObtenerIdUsuario(pvo_Usuario),
                pvo_Solicitud.getMotivo(),
                pvo_Solicitud.getObservacion());
        return ResponseEntity.ok(this.ConvertirObservacion(vlo_EnObservacion));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Elimina un borrador propio mediante baja lógica: deja de mostrarse, pero el
     *   registro se conserva. Una observación confirmada no puede eliminarse.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvn_IdObservacion identificador del borrador
     * @param pvo_Usuario usuario autenticado
     * @return borradores propios e historial confirmado, ya sin el borrador eliminado
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @DeleteMapping("/{vln_IdTramite}/observaciones/{vln_IdObservacion}")
    public ResponseEntity<ModeloObservacionesExpedienteRespuesta> SIMT_OBSERVACION_EXPED_EliminarRegistro(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite,
            @PathVariable("vln_IdObservacion") Long pvn_IdObservacion,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        Integer vln_IdUsuario = this.ObtenerIdUsuario(pvo_Usuario);
        this.vgo_BllObservacionExpediente.EliminarBorrador(pvn_IdTramite, pvn_IdObservacion, vln_IdUsuario);
        return ResponseEntity.ok(this.ArmarObservacionesExpediente(pvn_IdTramite, vln_IdUsuario));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Confirma un borrador propio: pasa al historial del expediente con la fecha y
     *   hora del servidor, queda anotado en la bitácora y ya no puede modificarse.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvn_IdObservacion identificador del borrador
     * @param pvo_Usuario usuario autenticado
     * @return observación confirmada
     */
    @PreAuthorize(ReglasAcceso.EXONERACION_MODIFICAR)
    @PostMapping("/{vln_IdTramite}/observaciones/{vln_IdObservacion}/confirmacion")
    public ResponseEntity<ModeloObservacionRespuesta> SIMT_OBSERVACION_EXPED_ConfirmarRegistro(
            @PathVariable("vln_IdTramite") Integer pvn_IdTramite,
            @PathVariable("vln_IdObservacion") Long pvn_IdObservacion,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        EntObservacionExpediente vlo_EnObservacion = this.vgo_BllObservacionExpediente.ConfirmarBorrador(
                pvn_IdTramite,
                pvn_IdObservacion,
                this.ObtenerIdUsuario(pvo_Usuario));
        return ResponseEntity.ok(this.ConvertirObservacion(vlo_EnObservacion));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Obtiene el identificador del usuario de la sesión autenticada. Si no hay
     *   sesión devuelve null, y la lógica de negocio rechaza la operación; en la práctica la
     *   configuración de seguridad ya responde 401 antes de llegar aquí.
     *
     * @param pvo_Usuario usuario autenticado
     * @return identificador del usuario, o null
     */
    private Integer ObtenerIdUsuario(UsuarioAutenticado pvo_Usuario) {
        return pvo_Usuario == null ? null : pvo_Usuario.idUsuario();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Reúne los borradores propios y el historial confirmado del expediente, con el
     *   nombre de cada responsable resuelto una sola vez por autor.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvn_IdUsuario usuario autenticado
     * @return observaciones del expediente listas para la pantalla
     */
    private ModeloObservacionesExpedienteRespuesta ArmarObservacionesExpediente(
            Integer pvn_IdTramite,
            Integer pvn_IdUsuario) {
        List<EntObservacionExpediente> vll_Borradores =
                this.vgo_BllObservacionExpediente.ObtenerBorradoresPropios(pvn_IdTramite, pvn_IdUsuario);
        List<EntObservacionExpediente> vll_Historial =
                this.vgo_BllObservacionExpediente.ObtenerHistorial(pvn_IdTramite);

        List<EntObservacionExpediente> vll_Todas = new ArrayList<>(vll_Borradores);
        vll_Todas.addAll(vll_Historial);
        Map<Integer, String> vlo_MpNombres = this.vgo_BllObservacionExpediente.ObtenerNombresResponsables(vll_Todas);

        ModeloObservacionesExpedienteRespuesta vlo_Respuesta = new ModeloObservacionesExpedienteRespuesta();
        vlo_Respuesta.setBorradores(vll_Borradores.stream()
                .map(vlo_EnObservacion -> this.ConvertirObservacion(vlo_EnObservacion, vlo_MpNombres))
                .toList());
        vlo_Respuesta.setHistorial(vll_Historial.stream()
                .map(vlo_EnObservacion -> this.ConvertirObservacion(vlo_EnObservacion, vlo_MpNombres))
                .toList());
        return vlo_Respuesta;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Convierte una sola observación al modelo de respuesta, resolviendo el nombre de
     *   su responsable.
     *
     * @param pvo_EnObservacion observación a convertir
     * @return modelo de respuesta
     */
    private ModeloObservacionRespuesta ConvertirObservacion(EntObservacionExpediente pvo_EnObservacion) {
        return this.ConvertirObservacion(
                pvo_EnObservacion,
                this.vgo_BllObservacionExpediente.ObtenerNombresResponsables(List.of(pvo_EnObservacion)));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Convierte una observación al modelo de respuesta. No expone el estado de
     *   registro (ACT/INA) ni el identificador interno del autor, solo su nombre. Una observación
     *   es editable mientras sea borrador; solo se devuelven los borradores del propio usuario.
     *
     * @param pvo_EnObservacion observación a convertir
     * @param pvo_MpNombres nombre del responsable por identificador de autor
     * @return modelo de respuesta
     */
    private ModeloObservacionRespuesta ConvertirObservacion(
            EntObservacionExpediente pvo_EnObservacion,
            Map<Integer, String> pvo_MpNombres) {
        ModeloObservacionRespuesta vlo_Modelo = new ModeloObservacionRespuesta();
        vlo_Modelo.setIdObservacion(pvo_EnObservacion.getIdObservacion());
        vlo_Modelo.setIdTramite(pvo_EnObservacion.getIdTramite());
        vlo_Modelo.setMotivo(pvo_EnObservacion.getMotivo());
        vlo_Modelo.setObservacion(pvo_EnObservacion.getObservacion());
        vlo_Modelo.setEstadoObservacion(pvo_EnObservacion.getEstadoObservacion());
        vlo_Modelo.setResponsable(pvo_MpNombres.getOrDefault(
                pvo_EnObservacion.getIdUsuarioAutor(), Constantes.TEXTO_SIN_DATO));
        vlo_Modelo.setFechaCreacion(pvo_EnObservacion.getFechaCreacion());
        vlo_Modelo.setFechaModificacion(pvo_EnObservacion.getFechaModificacion());
        vlo_Modelo.setFechaConfirmacion(pvo_EnObservacion.getFechaConfirmacion());
        vlo_Modelo.setEditable(
                Constantes.ESTADO_OBSERVACION_BORRADOR.equals(pvo_EnObservacion.getEstadoObservacion()));
        return vlo_Modelo;
    }
}