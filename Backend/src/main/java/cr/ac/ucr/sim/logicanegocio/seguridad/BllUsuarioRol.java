package cr.ac.ucr.sim.logicanegocio.seguridad;

import cr.ac.ucr.sim.accesodatos.seguridad.DalRol;
import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.seguridad.DalUsuarioRol;
import cr.ac.ucr.sim.entidadnegocio.seguridad.EntRol;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.seguridad.EntUsuarioRol;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionAutenticacion;
import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import cr.ac.ucr.sim.utilerias.modelo.seguridad.ModeloImpactoInactivacionRespuesta;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Descripcion: contiene la logica de negocio para
 * la asignacion de roles a usuarios del sistema.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main. Usa EntUsuario y DalUsuario de main (autenticacion) y
 *     el identificador de usuario es Integer. Al asignar un rol, la cuenta pasa a ESTADO_ROL
 *     asignado y, al retirar su último rol activo, vuelve a pendiente: el inicio de sesión de
 *     main exige ese estado. Se quita la validación de usuario inactivo porque SIMF_USUARIO no
 *     tiene estado de cuenta. Los errores usan ExcepcionAutenticacion. *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: heurística H2 (EH5-01). El estado de rol del usuario cuenta solo
 *     los roles activos, se agrega el conteo de los usuarios que quedarían sin roles activos al
 *     inactivar un rol y la actualización del estado de rol de todos los usuarios de un rol.
 */
@Service
public class BllUsuarioRol {

    private final DalUsuarioRol vgo_DalUsuarioRol;
    private final DalUsuario vgo_DalUsuario;
    private final DalRol vgo_DalRol;

    public BllUsuarioRol(
            DalUsuarioRol vlo_DalUsuarioRol,
            DalUsuario vlo_DalUsuario,
            DalRol vlo_DalRol
    ) {
        this.vgo_DalUsuarioRol = vlo_DalUsuarioRol;
        this.vgo_DalUsuario = vlo_DalUsuario;
        this.vgo_DalRol = vlo_DalRol;
    }

    /**
     * Descripcion: asigna un rol activo a un usuario.
     * @param vln_IdUsuario usuario que recibe el rol.
     * @param vln_IdRol rol a asignar.
     * @param vln_IdUsuarioAsignador usuario que realiza la asignacion.
     * @param vlc_Motivo motivo opcional.
     * @return asignacion registrada.
     */
    @Transactional
    public EntUsuarioRol AsignarRolAUsuario(
            Integer vln_IdUsuario,
            Long vln_IdRol,
            Integer vln_IdUsuarioAsignador,
            String vlc_Motivo
    ) {

        if (vln_IdUsuario == null
                || vln_IdRol == null
                || vln_IdUsuarioAsignador == null) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El usuario, el rol y el usuario asignador son obligatorios."
            );
        }

        if (vln_IdUsuario.equals(vln_IdUsuarioAsignador)) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.FORBIDDEN,
                    "El usuario no puede asignarse roles a sí mismo."
            );
        }

        if (vlc_Motivo != null
                && vlc_Motivo.trim().length() > 255) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El motivo no puede superar los 255 caracteres."
            );
        }

        EntUsuario vlo_EnUsuario =
                ObtenerUsuario(vln_IdUsuario);

        EntUsuario vlo_EnUsuarioAsignador =
                ObtenerUsuario(vln_IdUsuarioAsignador);

        EntRol vlo_EnRol =
                ObtenerRol(vln_IdRol);

        if (!ConstantesSeguridad.ESTADO_ACTIVO.equals(
                vlo_EnRol.getEstado()
        )) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "No se puede asignar un rol inactivo."
            );
        }

        boolean vlb_ExisteAsignacion =
                vgo_DalUsuarioRol.ExistePorUsuarioRolEstado(
                        vlo_EnUsuario,
                        vlo_EnRol,
                        ConstantesSeguridad.ESTADO_ACTIVO
                );

        if (vlb_ExisteAsignacion) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.CONFLICT,
                    "El usuario ya tiene asignado ese rol."
            );
        }

        EntUsuarioRol vlo_EnUsuarioRol =
                new EntUsuarioRol();

        vlo_EnUsuarioRol.setUsuario(
                vlo_EnUsuario
        );

        vlo_EnUsuarioRol.setRol(
                vlo_EnRol
        );

        vlo_EnUsuarioRol.setUsuarioAsignador(
                vlo_EnUsuarioAsignador
        );

        vlo_EnUsuarioRol.setMotivo(
                vlc_Motivo != null
                        ? vlc_Motivo.trim()
                        : null
        );

        vlo_EnUsuarioRol.setEstado(
                ConstantesSeguridad.ESTADO_ACTIVO
        );

        EntUsuarioRol vlo_EnAsignacion =
                vgo_DalUsuarioRol.save(
                        vlo_EnUsuarioRol
                );

        ActualizarEstadoRolUsuario(vlo_EnUsuario);

        return vlo_EnAsignacion;
    }

    /**
     * Descripcion: asigna varios roles a un mismo usuario.
     */
    @Transactional
    public List<EntUsuarioRol> AsignarRolesAUsuario(
            Integer vln_IdUsuario,
            List<Long> vll_IdRoles,
            Integer vln_IdUsuarioAsignador,
            String vlc_Motivo
    ) {

        if (vll_IdRoles == null
                || vll_IdRoles.isEmpty()) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "Debe seleccionar al menos un rol."
            );
        }

        Set<Long> vls_IdRoles =
                new LinkedHashSet<>(vll_IdRoles);

        return vls_IdRoles
                .stream()
                .map(vln_IdRol ->
                        AsignarRolAUsuario(
                                vln_IdUsuario,
                                vln_IdRol,
                                vln_IdUsuarioAsignador,
                                vlc_Motivo
                        )
                )
                .toList();
    }

    /**
     * Descripcion: desactiva una asignación
     * de rol sin eliminar su historial.
     */
    @Transactional
    public EntUsuarioRol QuitarRolDeUsuario(
            Integer vln_IdUsuario,
            Long vln_IdRol,
            Integer vln_IdUsuarioFinalizador,
            String vlc_Motivo
    ) {

        if (vln_IdUsuario == null
                || vln_IdRol == null
                || vln_IdUsuarioFinalizador == null) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El usuario, el rol y el usuario finalizador son obligatorios."
            );
        }

        if (vln_IdUsuario.equals(vln_IdUsuarioFinalizador)) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.FORBIDDEN,
                    "El usuario no puede modificar sus propios roles."
            );
        }

        if (vlc_Motivo != null
                && vlc_Motivo.trim().length() > 255) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El motivo no puede superar los 255 caracteres."
            );
        }

        EntUsuario vlo_EnUsuario =
                ObtenerUsuario(vln_IdUsuario);

        EntUsuario vlo_EnUsuarioFinalizador =
                ObtenerUsuario(vln_IdUsuarioFinalizador);

        EntRol vlo_EnRol =
                ObtenerRol(vln_IdRol);

        EntUsuarioRol vlo_EnUsuarioRol =
                vgo_DalUsuarioRol
                        .ObtenerPorUsuarioRolEstado(
                                vlo_EnUsuario,
                                vlo_EnRol,
                                ConstantesSeguridad.ESTADO_ACTIVO
                        )
                        .orElseThrow(() ->
                                new ExcepcionAutenticacion(
                    HttpStatus.NOT_FOUND,
                    "El usuario no tiene ese rol asignado."
            )
                        );

        vlo_EnUsuarioRol.setEstado(
                ConstantesSeguridad.ESTADO_INACTIVO
        );

        vlo_EnUsuarioRol.setFechaFinalizacion(
                LocalDateTime.now()
        );

        vlo_EnUsuarioRol.setUsuarioFinalizador(
                vlo_EnUsuarioFinalizador
        );

        vlo_EnUsuarioRol.setMotivo(
                vlc_Motivo != null
                        ? vlc_Motivo.trim()
                        : null
        );

        EntUsuarioRol vlo_EnAsignacion =
                vgo_DalUsuarioRol.saveAndFlush(
                        vlo_EnUsuarioRol
                );

        ActualizarEstadoRolUsuario(vlo_EnUsuario);

        return vlo_EnAsignacion;
    }

    /**
     * Descripcion: sincroniza ESTADO_ROL de SIMF_USUARIO con las
     * asignaciones activas del usuario: asignado si tiene al menos un rol
     * activo (asignacion y rol activos) y pendiente si no tiene ninguno. El inicio de sesión de main
     * exige el estado asignado.
     * @param vlo_EnUsuario usuario a actualizar.
     */
    private void ActualizarEstadoRolUsuario(
            EntUsuario vlo_EnUsuario
    ) {

        boolean vlb_TieneRolesActivos =
                vgo_DalUsuarioRol
                        .ContarRolesActivosVigentes(
                                vlo_EnUsuario,
                                ConstantesSeguridad.ESTADO_ACTIVO
                        ) > 0;

        vlo_EnUsuario.setEstadoRol(
                vlb_TieneRolesActivos
                        ? ConstantesSeguridad.ESTADO_ROL_ASIGNADO
                        : ConstantesSeguridad.ESTADO_ROL_PENDIENTE
        );

        vgo_DalUsuario.SIMF_USUARIO_ModificarRegistro(
                vlo_EnUsuario
        );
    }

    /**
     * Descripcion: cuenta las personas que tienen asignado el rol y las que
     * quedarian sin ningun rol activo si el rol se inactiva.
     * @param vln_IdRol identificador del rol.
     * @return personas con el rol y personas que quedarian sin roles activos.
     */
    @Transactional(readOnly = true)
    public ModeloImpactoInactivacionRespuesta ContarImpactoInactivacionRol(
            Long vln_IdRol
    ) {

        EntRol vlo_EnRol =
                ObtenerRol(vln_IdRol);

        long vln_UsuariosConRol =
                vgo_DalUsuarioRol
                        .ObtenerUsuariosPorRolEstado(
                                vlo_EnRol,
                                ConstantesSeguridad.ESTADO_ACTIVO
                        )
                        .size();

        long vln_UsuariosSinRoles =
                vgo_DalUsuarioRol
                        .ContarUsuariosSinOtroRolActivo(
                                vlo_EnRol,
                                ConstantesSeguridad.ESTADO_ACTIVO
                        );

        ModeloImpactoInactivacionRespuesta vlo_Respuesta =
                new ModeloImpactoInactivacionRespuesta();

        vlo_Respuesta.setIdRol(vlo_EnRol.getIdRol());
        vlo_Respuesta.setUsuariosConRol(vln_UsuariosConRol);
        vlo_Respuesta.setUsuariosSinRolesActivos(vln_UsuariosSinRoles);

        return vlo_Respuesta;
    }

    /**
     * Descripcion: actualiza el estado de rol de todas las personas
     * que tienen asignado el rol, despues de activarlo o inactivarlo.
     * @param vlo_EnRol rol cuyo estado cambio.
     */
    @Transactional
    public void ActualizarEstadoRolUsuariosDelRol(
            EntRol vlo_EnRol
    ) {

        vgo_DalUsuarioRol
                .ObtenerUsuariosPorRolEstado(
                        vlo_EnRol,
                        ConstantesSeguridad.ESTADO_ACTIVO
                )
                .forEach(this::ActualizarEstadoRolUsuario);
    }

    /**
     * Descripcion: obtiene los roles activos
     * asignados a un usuario.
     */
    @Transactional(readOnly = true)
    public List<EntRol> ListarRolesActivosDeUsuario(
            Integer vln_IdUsuario
    ) {

        EntUsuario vlo_EnUsuario =
                ObtenerUsuario(vln_IdUsuario);

        return vgo_DalUsuarioRol
                .ObtenerPorUsuarioEstado(
                        vlo_EnUsuario,
                        ConstantesSeguridad.ESTADO_ACTIVO
                )
                .stream()
                .map(EntUsuarioRol::getRol)
                .toList();
    }

    private EntUsuario ObtenerUsuario(
            Integer vln_IdUsuario
    ) {

        EntUsuario vlo_EnUsuario =
                vgo_DalUsuario
                        .SIMF_USUARIO_ObtenerRegistro(vln_IdUsuario);

        if (vlo_EnUsuario == null) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.NOT_FOUND,
                    "No existe un usuario con el ID indicado."
            );
        }

        return vlo_EnUsuario;
    }

    private EntRol ObtenerRol(
            Long vln_IdRol
    ) {

        return vgo_DalRol
                .findById(vln_IdRol)
                .orElseThrow(() ->
                        new ExcepcionAutenticacion(
                    HttpStatus.NOT_FOUND,
                    "No existe un rol con el ID indicado."
            )
                );
    }

    /**
     * Descripcion: obtiene el historial completo de asignaciones
     * de roles registradas para un usuario.
     * @param vln_IdUsuario identificador del usuario.
     * @return historial de asignaciones del usuario.
     */
    @Transactional(readOnly = true)
    public List<EntUsuarioRol> ListarHistorialRolesDeUsuario(
            Integer vln_IdUsuario
    ) {

        EntUsuario vlo_EnUsuario =
                ObtenerUsuario(vln_IdUsuario);

        return vgo_DalUsuarioRol
                .ObtenerPorUsuario(vlo_EnUsuario);
    }

    /**
     * Descripcion: obtiene un usuario registrado
     * mediante su correo institucional.
     * @param vlc_CorreoInstitucional correo del usuario.
     * @return usuario encontrado.
     */
    @Transactional(readOnly = true)
    public EntUsuario ObtenerUsuarioPorCorreo(
            String vlc_CorreoInstitucional
    ) {

        if (vlc_CorreoInstitucional == null
                || vlc_CorreoInstitucional.isBlank()) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El correo institucional es obligatorio."
            );
        }

        String vlc_CorreoNormalizado =
                vlc_CorreoInstitucional
                        .trim()
                        .toLowerCase();

        EntUsuario vlo_EnUsuario =
                vgo_DalUsuario
                        .SIMF_USUARIO_ObtenerPorCorreoInstitucional(
                                vlc_CorreoNormalizado
                        );

        if (vlo_EnUsuario == null) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.NOT_FOUND,
                    "No existe un usuario con el correo indicado."
            );
        }

        return vlo_EnUsuario;
    }}