package cr.ac.ucr.sim.logicanegocio.seguridad;

import cr.ac.ucr.sim.accesodatos.seguridad.DalRol;
import cr.ac.ucr.sim.entidadnegocio.seguridad.EntRol;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionAutenticacion;
import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Descripcion: Lógica de negocio para el registro
 * y administracion de roles del sistema.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main: los errores usan ExcepcionAutenticacion de main y
 *     las constantes vienen de ConstantesSeguridad. *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: heurística H2 (EH5-01). Al activar o inactivar un rol se actualiza el
 *     estado de rol de las personas que lo tienen asignado, para que una cuenta sin roles activos
 *     quede pendiente de asignación.
 */
@Service
public class BllRol {

    private final DalRol vgo_DalRol;
    private final BllRolPermiso vgo_BllRolPermiso;
    private final BllUsuarioRol vgo_BllUsuarioRol;

    public BllRol(
            DalRol vlo_DalRol,
            BllRolPermiso vlo_BllRolPermiso,
            BllUsuarioRol vlo_BllUsuarioRol
    ) {
        this.vgo_DalRol = vlo_DalRol;
        this.vgo_BllRolPermiso = vlo_BllRolPermiso;
        this.vgo_BllUsuarioRol = vlo_BllUsuarioRol;
    }

    @Transactional
    public EntRol CrearRol(
            String vlc_Nombre,
            String vlc_Descripcion,
            List<Long> vll_IdPermisos
    ) {

        if (vlc_Nombre == null || vlc_Nombre.isBlank()) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El nombre del rol es obligatorio."
            );
        }

        String vlc_NombreNormalizado = vlc_Nombre.trim();

        if (vlc_NombreNormalizado.length() > 50) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El nombre del rol no puede superar los 50 caracteres."
            );
        }

        if (vlc_Descripcion != null
                && vlc_Descripcion.trim().length() > 255) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "La descripción del rol no puede superar los 255 caracteres."
            );
        }

        if (vll_IdPermisos == null
                || vll_IdPermisos.isEmpty()) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "Debe seleccionar al menos un permiso para el rol."
            );
        }

        if (vgo_DalRol.ExistePorNombre(vlc_NombreNormalizado)) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.CONFLICT,
                    "Ya existe un rol con ese nombre."
            );
        }

        EntRol vlo_EnRol = new EntRol();

        vlo_EnRol.setNombre(vlc_NombreNormalizado);

        vlo_EnRol.setDescripcion(
                vlc_Descripcion != null
                        ? vlc_Descripcion.trim()
                        : null
        );

        vlo_EnRol.setEstado(
                ConstantesSeguridad.ESTADO_ACTIVO
        );

        vlo_EnRol = vgo_DalRol.save(vlo_EnRol);

        vgo_BllRolPermiso.AsignarPermisosARol(
                vlo_EnRol.getIdRol(),
                vll_IdPermisos
        );

        return vlo_EnRol;
    }

    @Transactional(readOnly = true)
    public List<EntRol> ListarRoles() {
        return vgo_DalRol.findAll();
    }

    @Transactional(readOnly = true)
    public EntRol ObtenerPorId(Long vln_IdRol) {

        if (vln_IdRol == null) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El identificador del rol es obligatorio."
            );
        }

        return vgo_DalRol.findById(vln_IdRol)
                .orElseThrow(() ->
                        new ExcepcionAutenticacion(
                    HttpStatus.NOT_FOUND,
                    "No existe un rol con el ID indicado."
            )
                );
    }

    @Transactional
    public EntRol ActualizarRol(
            Long vln_IdRol,
            String vlc_Nombre,
            String vlc_Descripcion
    ) {

        EntRol vlo_EnRol = ObtenerPorId(vln_IdRol);

        if (vlc_Nombre == null || vlc_Nombre.isBlank()) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El nombre del rol es obligatorio."
            );
        }

        String vlc_NombreNormalizado = vlc_Nombre.trim();

        if (vlc_NombreNormalizado.length() > 50) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El nombre del rol no puede superar los 50 caracteres."
            );
        }

        if (vlc_Descripcion != null
                && vlc_Descripcion.trim().length() > 255) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "La descripción del rol no puede superar los 255 caracteres."
            );
        }

        vgo_DalRol.ObtenerPorNombre(vlc_NombreNormalizado)
                .filter(vlo_OtroRol ->
                        !vlo_OtroRol.getIdRol().equals(vln_IdRol)
                )
                .ifPresent(vlo_OtroRol -> {
                    throw new ExcepcionAutenticacion(
                    HttpStatus.CONFLICT,
                    "Ya existe un rol con ese nombre."
            );
                });

        vlo_EnRol.setNombre(vlc_NombreNormalizado);

        vlo_EnRol.setDescripcion(
                vlc_Descripcion != null
                        ? vlc_Descripcion.trim()
                        : null
        );

        return vgo_DalRol.save(vlo_EnRol);
    }

    @Transactional
    public EntRol CambiarEstado(
            Long vln_IdRol,
            String vlc_Estado
    ) {

        if (!ConstantesSeguridad.ESTADO_ACTIVO.equals(vlc_Estado)
                && !ConstantesSeguridad.ESTADO_INACTIVO.equals(vlc_Estado)) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El estado del rol debe ser ACT o INA."
            );
        }

        EntRol vlo_EnRol = ObtenerPorId(vln_IdRol);

        vlo_EnRol.setEstado(vlc_Estado);

        EntRol vlo_EnRolGuardado =
                vgo_DalRol.saveAndFlush(vlo_EnRol);

        vgo_BllUsuarioRol.ActualizarEstadoRolUsuariosDelRol(
                vlo_EnRolGuardado
        );

        return vlo_EnRolGuardado;
    }
}