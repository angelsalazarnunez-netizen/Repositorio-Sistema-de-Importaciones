package cr.ac.ucr.sim.sla;

import cr.ac.ucr.sim.seguridad.ReglasAcceso;
import org.springframework.security.access.prepost.PreAuthorize;
import cr.ac.ucr.sim.entidadnegocio.seguridad.EntPermiso;
import cr.ac.ucr.sim.entidadnegocio.seguridad.EntRol;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.seguridad.EntUsuarioRol;
import cr.ac.ucr.sim.logicanegocio.seguridad.BllPermiso;
import cr.ac.ucr.sim.logicanegocio.seguridad.BllRol;
import cr.ac.ucr.sim.logicanegocio.seguridad.BllRolPermiso;
import cr.ac.ucr.sim.logicanegocio.seguridad.BllUsuarioRol;
import cr.ac.ucr.sim.seguridad.UsuarioAutenticado;
import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import cr.ac.ucr.sim.utilerias.Enumerados;
import cr.ac.ucr.sim.utilerias.modelo.seguridad.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Descripcion: controlador REST de
 * Autenticacion y Seguridad.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main. La clase pasa al paquete sla con los demás servicios y
 *     queda protegida por el inicio de sesión de main (CfgSeguridad exige token en /api/**). El
 *     usuario que asigna o retira un rol se toma de la sesión y ya no del cuerpo de la
 *     solicitud. El identificador de usuario es Integer.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: heurística H2 (EH5-01). Se agrega la consulta del efecto de inactivar un rol,
 *     para advertir cuántas personas quedarían sin roles activos.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: cada permiso se devuelve con el código y el nombre de su módulo de acceso
 *     (HU-003), para agruparlos en la pantalla de roles.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003). Solo quien tenga los permisos de administración
 *     de usuarios puede registrar, modificar o asignar roles; cualquier otro usuario recibe 403.
 */
@RestController
@RequestMapping("/api/v1/seguridad")
public class Ws_SIM_Seguridad {

    private final BllRol vgo_BllRol;
    private final BllRolPermiso vgo_BllRolPermiso;
    private final BllPermiso vgo_BllPermiso;
    private final BllUsuarioRol vgo_BllUsuarioRol;

    public Ws_SIM_Seguridad(
            BllRol vlo_BllRol,
            BllRolPermiso vlo_BllRolPermiso,
            BllPermiso vlo_BllPermiso,
            BllUsuarioRol vlo_BllUsuarioRol
    ) {
        this.vgo_BllRol = vlo_BllRol;
        this.vgo_BllRolPermiso = vlo_BllRolPermiso;
        this.vgo_BllPermiso = vlo_BllPermiso;
        this.vgo_BllUsuarioRol = vlo_BllUsuarioRol;
    }

    /**
     * Descripcion: registra un nuevo rol.
     * @param vlo_Solicitud datos del rol.
     * @return rol registrado.
     */
    @PreAuthorize(ReglasAcceso.ROL_CREAR)
    @PostMapping("/roles")
    public ResponseEntity<ModeloRolRespuesta>
    SIMM_ROL_InsertarRegistro(
            @RequestBody ModeloRolSolicitud vlo_Solicitud
    ) {

        EntRol vlo_EnRol = vgo_BllRol.CrearRol(
                vlo_Solicitud.getNombre(),
                vlo_Solicitud.getDescripcion(),
                vlo_Solicitud.getIdPermisos()
        );

        ModeloRolRespuesta vlo_Respuesta =
                ConvertirRolARespuesta(vlo_EnRol);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vlo_Respuesta);
    }

    /**
     * Descripcion: obtiene todos los roles registrados.
     * @return lista de roles.
     */
    @PreAuthorize(ReglasAcceso.ROL_CONSULTA)
    @GetMapping("/roles")
    public ResponseEntity<List<ModeloRolRespuesta>>
    SIMM_ROL_ObtenerRegistros() {

        List<ModeloRolRespuesta> vlo_LsRoles =
                vgo_BllRol.ListarRoles()
                        .stream()
                        .map(this::ConvertirRolARespuesta)
                        .toList();

        return ResponseEntity.ok(vlo_LsRoles);
    }

    /**
     * Descripcion: obtiene un rol por su id.
     * @param vln_IdRol id del rol.
     * @return rol encontrado.
     */
    @PreAuthorize(ReglasAcceso.ROL_CONSULTA)
    @GetMapping("/roles/{vln_IdRol}")
    public ResponseEntity<ModeloRolRespuesta>
    SIMM_ROL_ObtenerRegistro(
            @PathVariable Long vln_IdRol
    ) {

        EntRol vlo_EnRol =
                vgo_BllRol.ObtenerPorId(vln_IdRol);

        return ResponseEntity.ok(
                ConvertirRolARespuesta(vlo_EnRol)
        );
    }

    /**
     * Descripcion: actualiza los datos de un rol.
     * @param vln_IdRol identificador del rol.
     * @param vlo_Solicitud datos actualizados.
     * @return rol actualizado.
     */
    @PreAuthorize(ReglasAcceso.ROL_MODIFICAR)
    @PutMapping("/roles/{vln_IdRol}")
    public ResponseEntity<ModeloRolRespuesta>
    SIMM_ROL_ModificarRegistro(
            @PathVariable Long vln_IdRol,
            @RequestBody ModeloRolSolicitud vlo_Solicitud
    ) {

        EntRol vlo_EnRol =
                vgo_BllRol.ActualizarRol(
                        vln_IdRol,
                        vlo_Solicitud.getNombre(),
                        vlo_Solicitud.getDescripcion()
                );

        return ResponseEntity.ok(
                ConvertirRolARespuesta(vlo_EnRol)
        );
    }

    /**
     * Descripcion: modifica el estado de un rol registrado.
     * @param vln_IdRol identificador del rol.
     * @param vlo_Solicitud nuevo estado del rol.
     * @return rol con su estado actualizado.
     */
    @PreAuthorize(ReglasAcceso.ROL_MODIFICAR)
    @PatchMapping("/roles/{vln_IdRol}/estado")
    public ResponseEntity<ModeloRolRespuesta>
    SIMM_ROL_ModificarEstado(
            @PathVariable Long vln_IdRol,
            @RequestBody ModeloEstadoSolicitud vlo_Solicitud
    ) {

        EntRol vlo_EnRol = vgo_BllRol.CambiarEstado(
                vln_IdRol,
                vlo_Solicitud.getEstado()
        );

        return ResponseEntity.ok(
                ConvertirRolARespuesta(vlo_EnRol)
        );
    }

    /**
     * Descripcion: indica cuantas personas tienen asignado el rol
     * y cuantas quedarian sin roles activos si se inactiva.
     * @param vln_IdRol identificador del rol.
     * @return efecto de la inactivacion.
     */
    @PreAuthorize(ReglasAcceso.ROL_CONSULTA)
    @GetMapping("/roles/{vln_IdRol}/impacto-inactivacion")
    public ResponseEntity<ModeloImpactoInactivacionRespuesta>
    SIMT_USUARIO_ROL_ObtenerImpactoInactivacion(
            @PathVariable Long vln_IdRol
    ) {

        return ResponseEntity.ok(
                vgo_BllUsuarioRol.ContarImpactoInactivacionRol(
                        vln_IdRol
                )
        );
    }

    /**
     * Descripcion: convierte una entidad de rol
     * al modelo utilizado por la API.
     * @param vlo_EnRol entidad de rol.
     * @return modelo de respuesta.
     */
    private ModeloRolRespuesta ConvertirRolARespuesta(
            EntRol vlo_EnRol
    ) {

        ModeloRolRespuesta vlo_Respuesta =
                new ModeloRolRespuesta();

        vlo_Respuesta.setIdRol(
                vlo_EnRol.getIdRol()
        );

        vlo_Respuesta.setNombre(
                vlo_EnRol.getNombre()
        );

        vlo_Respuesta.setDescripcion(
                vlo_EnRol.getDescripcion()
        );

        vlo_Respuesta.setEstado(
                vlo_EnRol.getEstado()
        );

        List<ModeloPermisoRespuesta> vlo_LsPermisos =
                vgo_BllRolPermiso
                        .ListarPermisosDeRol(
                                vlo_EnRol.getIdRol()
                        )
                        .stream()
                        .map(this::ConvertirPermisoARespuesta)
                        .toList();

        vlo_Respuesta.setPermisos(vlo_LsPermisos);

        return vlo_Respuesta;
    }

    /**
     * Descripcion: convierte una entidad de permiso
     * al modelo utilizado por la API.
     * @param vlo_EnPermiso entidad de permiso.
     * @return modelo de respuesta.
     */
    private ModeloPermisoRespuesta ConvertirPermisoARespuesta(
            EntPermiso vlo_EnPermiso
    ) {

        ModeloPermisoRespuesta vlo_Respuesta =
                new ModeloPermisoRespuesta();

        vlo_Respuesta.setIdPermiso(
                vlo_EnPermiso.getIdPermiso()
        );

        vlo_Respuesta.setCodigo(
                vlo_EnPermiso.getCodigo()
        );

        vlo_Respuesta.setNombre(
                vlo_EnPermiso.getNombre()
        );

        vlo_Respuesta.setDescripcion(
                vlo_EnPermiso.getDescripcion()
        );

        Enumerados.ModuloPermiso vlo_Modulo =
                Enumerados.ModuloPermiso.DesdeCodigo(
                        vlo_EnPermiso.getModulo()
                );

        vlo_Respuesta.setModulo(
                vlo_Modulo != null
                        ? vlo_Modulo.name()
                        : null
        );

        vlo_Respuesta.setNombreModulo(
                vlo_Modulo != null
                        ? vlo_Modulo.getNombre()
                        : null
        );

        return vlo_Respuesta;
    }

    /**
     * Descripcion: asigna un permiso a un rol existente.
     * @param vln_IdRol identificador del rol.
     * @param vln_IdPermiso identificador del permiso.
     * @return rol con sus permisos actualizados.
     */
    @PreAuthorize(ReglasAcceso.ROL_MODIFICAR)
    @PostMapping("/roles/{vln_IdRol}/permisos/{vln_IdPermiso}")
    public ResponseEntity<ModeloRolRespuesta>
    SIMT_ROL_PERMISO_InsertarRegistro(
            @PathVariable Long vln_IdRol,
            @PathVariable Long vln_IdPermiso
    ) {

        vgo_BllRolPermiso.AsignarPermisoARol(
                vln_IdRol,
                vln_IdPermiso
        );

        EntRol vlo_EnRol =
                vgo_BllRol.ObtenerPorId(vln_IdRol);

        return ResponseEntity.ok(
                ConvertirRolARespuesta(vlo_EnRol)
        );
    }

    /**
     * Descripcion: retira un permiso activo de un rol.
     * @param vln_IdRol identificador del rol.
     * @param vln_IdPermiso identificador del permiso.
     * @return rol con sus permisos actualizados.
     */
    @PreAuthorize(ReglasAcceso.ROL_MODIFICAR)
    @DeleteMapping("/roles/{vln_IdRol}/permisos/{vln_IdPermiso}")
    public ResponseEntity<ModeloRolRespuesta>
    SIMT_ROL_PERMISO_EliminarRegistro(
            @PathVariable Long vln_IdRol,
            @PathVariable Long vln_IdPermiso
    ) {

        vgo_BllRolPermiso.QuitarPermisoDeRol(
                vln_IdRol,
                vln_IdPermiso
        );

        EntRol vlo_EnRol =
                vgo_BllRol.ObtenerPorId(vln_IdRol);

        return ResponseEntity.ok(
                ConvertirRolARespuesta(vlo_EnRol)
        );
    }

    /**
     * Descripcion: obtiene los permisos activos disponibles
     * para asociarlos a los roles.
     * @return lista de permisos activos.
     */
    @PreAuthorize(ReglasAcceso.PERMISO_CONSULTA)
    @GetMapping("/permisos")
    public ResponseEntity<List<ModeloPermisoRespuesta>>
    SIMM_PERMISO_ObtenerRegistros() {

        List<ModeloPermisoRespuesta> vlo_LsPermisos =
                vgo_BllPermiso.ListarPermisosActivos()
                        .stream()
                        .map(this::ConvertirPermisoARespuesta)
                        .toList();

        return ResponseEntity.ok(vlo_LsPermisos);
    }

    /**
     * Descripcion: asigna uno o varios roles
     * a un usuario del sistema.
     * @param vlo_Solicitud datos de la asignacion.
     * @param pvo_Usuario usuario de la sesión, que realiza la asignación.
     * @return asignaciones registradas.
     */
    @PreAuthorize(ReglasAcceso.ROL_ASIGNAR)
    @PostMapping("/usuarios/roles")
    public ResponseEntity<List<ModeloUsuarioRolRespuesta>>
    SIMT_USUARIO_ROL_InsertarRegistros(
            @RequestBody ModeloAsignacionRolSolicitud vlo_Solicitud,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario
    ) {

        List<ModeloUsuarioRolRespuesta> vlo_LsAsignaciones =
                vgo_BllUsuarioRol.AsignarRolesAUsuario(
                                vlo_Solicitud.getIdUsuario(),
                                vlo_Solicitud.getIdRoles(),
                                ObtenerIdUsuario(pvo_Usuario),
                                vlo_Solicitud.getMotivo()
                        )
                        .stream()
                        .map(this::ConvertirUsuarioRolARespuesta)
                        .toList();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vlo_LsAsignaciones);
    }

    /**
     * Descripcion: desactiva la asignación
     * de un rol a un usuario.
     * @param vln_IdUsuario usuario que posee el rol.
     * @param vln_IdRol rol que se retira.
     * @param vlo_Solicitud datos de finalizacion (motivo opcional).
     * @param pvo_Usuario usuario de la sesión, que retira el rol.
     * @return asignacion finalizada.
     */
    @PreAuthorize(ReglasAcceso.ROL_ASIGNAR)
    @DeleteMapping("/usuarios/{vln_IdUsuario}/roles/{vln_IdRol}")
    public ResponseEntity<ModeloUsuarioRolRespuesta>
    SIMT_USUARIO_ROL_EliminarRegistro(
            @PathVariable Integer vln_IdUsuario,
            @PathVariable Long vln_IdRol,
            @RequestBody(required = false) ModeloRetiroRolSolicitud vlo_Solicitud,
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario
    ) {

        EntUsuarioRol vlo_EnUsuarioRol =
                vgo_BllUsuarioRol.QuitarRolDeUsuario(
                        vln_IdUsuario,
                        vln_IdRol,
                        ObtenerIdUsuario(pvo_Usuario),
                        vlo_Solicitud != null
                                ? vlo_Solicitud.getMotivo()
                                : null
                );

        return ResponseEntity.ok(
                ConvertirUsuarioRolARespuesta(vlo_EnUsuarioRol)
        );
    }

    /**
     * Descripcion: obtiene los roles activos
     * asignados a un usuario.
     * @param vln_IdUsuario identificador del usuario.
     * @return roles activos del usuario.
     */
    @PreAuthorize(ReglasAcceso.USUARIO_ROLES_CONSULTA)
    @GetMapping("/usuarios/{vln_IdUsuario}/roles")
    public ResponseEntity<List<ModeloRolRespuesta>>
    SIMT_USUARIO_ROL_ObtenerRegistros(
            @PathVariable Integer vln_IdUsuario
    ) {

        List<ModeloRolRespuesta> vlo_LsRoles =
                vgo_BllUsuarioRol
                        .ListarRolesActivosDeUsuario(vln_IdUsuario)
                        .stream()
                        .map(this::ConvertirRolARespuesta)
                        .toList();

        return ResponseEntity.ok(vlo_LsRoles);
    }

    /**
     * Descripcion: convierte una asignacion de rol
     * al modelo utilizado por la API.
     * @param vlo_EnUsuarioRol entidad de asignacion.
     * @return modelo de respuesta.
     */
    private ModeloUsuarioRolRespuesta ConvertirUsuarioRolARespuesta(
            EntUsuarioRol vlo_EnUsuarioRol
    ) {

        ModeloUsuarioRolRespuesta vlo_Respuesta =
                new ModeloUsuarioRolRespuesta();

        vlo_Respuesta.setIdUsuarioRol(
                vlo_EnUsuarioRol.getIdUsuarioRol()
        );

        vlo_Respuesta.setIdUsuario(
                vlo_EnUsuarioRol.getUsuario().getIdUsuario()
        );

        vlo_Respuesta.setIdRol(
                vlo_EnUsuarioRol.getRol().getIdRol()
        );

        vlo_Respuesta.setNombreRol(
                vlo_EnUsuarioRol.getRol().getNombre()
        );

        vlo_Respuesta.setIdUsuarioAsignador(
                vlo_EnUsuarioRol
                        .getUsuarioAsignador()
                        .getIdUsuario()
        );

        vlo_Respuesta.setFechaAsignacion(
                vlo_EnUsuarioRol.getFechaAsignacion()
        );

        vlo_Respuesta.setFechaFinalizacion(
                vlo_EnUsuarioRol.getFechaFinalizacion()
        );

        vlo_Respuesta.setMotivo(
                vlo_EnUsuarioRol.getMotivo()
        );

        vlo_Respuesta.setEstado(
                vlo_EnUsuarioRol.getEstado()
        );

        if (vlo_EnUsuarioRol.getUsuarioFinalizador() != null) {
            vlo_Respuesta.setIdUsuarioFinalizador(
                    vlo_EnUsuarioRol
                            .getUsuarioFinalizador()
                            .getIdUsuario()
            );
        }

        return vlo_Respuesta;
    }

    /**
     * Descripcion: obtiene el historial completo
     * de asignaciones de roles de un usuario.
     * @param vln_IdUsuario identificador del usuario.
     * @return historial de asignaciones de roles.
     */
    @PreAuthorize(ReglasAcceso.USUARIO_ROLES_CONSULTA)
    @GetMapping("/usuarios/{vln_IdUsuario}/roles/historial")
    public ResponseEntity<List<ModeloUsuarioRolRespuesta>>
    SIMT_USUARIO_ROL_ObtenerHistorial(
            @PathVariable Integer vln_IdUsuario
    ) {

        List<ModeloUsuarioRolRespuesta> vlo_LsHistorial =
                vgo_BllUsuarioRol
                        .ListarHistorialRolesDeUsuario(
                                vln_IdUsuario
                        )
                        .stream()
                        .map(this::ConvertirUsuarioRolARespuesta)
                        .toList();

        return ResponseEntity.ok(vlo_LsHistorial);
    }

    /**
     * Descripcion: consulta un usuario mediante
     * su correo.
     * @param vlc_Correo correo institucional.
     * @return usuario encontrado.
     */
    @PreAuthorize(ReglasAcceso.USUARIO_ROLES_CONSULTA)
    @GetMapping("/usuarios/buscar")
    public ResponseEntity<ModeloUsuarioRespuesta>
    SIMF_USUARIO_ObtenerRegistroPorCorreo(
            @RequestParam("correo") String vlc_Correo
    ) {

        EntUsuario vlo_EnUsuario =
                vgo_BllUsuarioRol
                        .ObtenerUsuarioPorCorreo(
                                vlc_Correo
                        );

        return ResponseEntity.ok(
                ConvertirUsuarioARespuesta(
                        vlo_EnUsuario
                )
        );
    }


    /**
     * Descripcion: convierte un usuaroo
     * al modelo utilizado por la API.
     * @param vlo_EnUsuario entidad de usuario.
     * @return modelo de respuesta.
     */
    private ModeloUsuarioRespuesta ConvertirUsuarioARespuesta(
            EntUsuario vlo_EnUsuario
    ) {

        ModeloUsuarioRespuesta vlo_Respuesta =
                new ModeloUsuarioRespuesta();

        vlo_Respuesta.setIdUsuario(
                vlo_EnUsuario.getIdUsuario()
        );

        vlo_Respuesta.setNumeroCedula(
                vlo_EnUsuario.getNumeroCedula()
        );

        vlo_Respuesta.setNombreCompleto(
                vlo_EnUsuario.getNombreCompleto()
        );

        vlo_Respuesta.setCorreoInstitucional(
                vlo_EnUsuario.getCorreoInstitucional()
        );

        vlo_Respuesta.setEstadoValidacion(
                vlo_EnUsuario.getEstadoValidacion()
        );

        // SIMF_USUARIO no tiene estado de cuenta: toda cuenta registrada está activa.
        vlo_Respuesta.setEstado(
                ConstantesSeguridad.ESTADO_ACTIVO
        );

        return vlo_Respuesta;
    }

    /**
     * Descripcion: obtiene el identificador del
     * usuario de la sesión.
     * @param pvo_Usuario usuario autenticado.
     * @return identificador del usuario, o null.
     */
    private Integer ObtenerIdUsuario(
            UsuarioAutenticado pvo_Usuario
    ) {
        return pvo_Usuario == null
                ? null
                : pvo_Usuario.idUsuario();
    }
}
