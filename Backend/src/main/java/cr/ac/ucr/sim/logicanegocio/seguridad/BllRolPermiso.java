package cr.ac.ucr.sim.logicanegocio.seguridad;

import cr.ac.ucr.sim.accesodatos.seguridad.DalPermiso;
import cr.ac.ucr.sim.accesodatos.seguridad.DalRol;
import cr.ac.ucr.sim.accesodatos.seguridad.DalRolPermiso;
import cr.ac.ucr.sim.entidadnegocio.seguridad.EntPermiso;
import cr.ac.ucr.sim.entidadnegocio.seguridad.EntRol;
import cr.ac.ucr.sim.entidadnegocio.seguridad.EntRolPermiso;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionAutenticacion;
import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Descripcion: Lógica de negocio para la
 * asignacion de permisos a los roles del sistema.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main: los errores usan ExcepcionAutenticacion de main y
 *     las constantes vienen de ConstantesSeguridad.
 */
@Service
public class BllRolPermiso {

    private final DalRolPermiso vgo_DalRolPermiso;
    private final DalRol vgo_DalRol;
    private final DalPermiso vgo_DalPermiso;

    /**
     * Descripcion: inicializa la logica de negocio
     * para los permisos asociados a roles.
     * @param vlo_DalRolPermiso acceso a datos de relaciones rol-permiso.
     * @param vlo_DalRol acceso a datos de roles.
     * @param vlo_DalPermiso acceso a datos de permisos.
     */
    public BllRolPermiso(
            DalRolPermiso vlo_DalRolPermiso,
            DalRol vlo_DalRol,
            DalPermiso vlo_DalPermiso
    ) {
        this.vgo_DalRolPermiso = vlo_DalRolPermiso;
        this.vgo_DalRol = vlo_DalRol;
        this.vgo_DalPermiso = vlo_DalPermiso;
    }

    /**
     * Descripcion: asigna un permiso activo a un rol.
     * @param vln_IdRol identificador del rol.
     * @param vln_IdPermiso identificador del permiso.
     * @return relacion creada entre el rol y el permiso.
     */
    @Transactional
    public EntRolPermiso AsignarPermisoARol(
            Long vln_IdRol,
            Long vln_IdPermiso
    ) {

        if (vln_IdRol == null) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El identificador del rol es obligatorio."
            );
        }

        if (vln_IdPermiso == null) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El identificador del permiso es obligatorio."
            );
        }

        EntRol vlo_EnRol = ObtenerRol(vln_IdRol);
        EntPermiso vlo_EnPermiso = ObtenerPermiso(vln_IdPermiso);

        if (!ConstantesSeguridad.ESTADO_ACTIVO.equals(
                vlo_EnRol.getEstado()
        )) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "No se pueden asignar permisos a un rol inactivo."
            );
        }

        if (!ConstantesSeguridad.ESTADO_ACTIVO.equals(
                vlo_EnPermiso.getEstado()
        )) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "No se puede asignar un permiso inactivo."
            );
        }

        boolean vlb_ExisteAsignacion =
                vgo_DalRolPermiso.ExistePorRolPermisoEstado(
                        vlo_EnRol,
                        vlo_EnPermiso,
                        ConstantesSeguridad.ESTADO_ACTIVO
                );

        if (vlb_ExisteAsignacion) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.CONFLICT,
                    "El permiso ya se encuentra asignado al rol."
            );
        }

        EntRolPermiso vlo_EnRolPermiso =
                new EntRolPermiso();

        vlo_EnRolPermiso.setRol(vlo_EnRol);
        vlo_EnRolPermiso.setPermiso(vlo_EnPermiso);
        vlo_EnRolPermiso.setEstado(
                ConstantesSeguridad.ESTADO_ACTIVO
        );

        return vgo_DalRolPermiso.save(
                vlo_EnRolPermiso
        );
    }

    /**
     * Descripcion: asigna una coleccion de permisos
     * a un mismo rol.
     * @param vln_IdRol identificador del rol.
     * @param vll_IdPermisos identificadores de permisos.
     * @return relaciones creadas entre rol y permisos.
     */
    @Transactional
    public List<EntRolPermiso> AsignarPermisosARol(
            Long vln_IdRol,
            List<Long> vll_IdPermisos
    ) {

        if (vll_IdPermisos == null
                || vll_IdPermisos.isEmpty()) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "Debe seleccionar al menos un permiso para el rol."
            );
        }

        Set<Long> vls_IdPermisos =
                new LinkedHashSet<>(vll_IdPermisos);

        return vls_IdPermisos
                .stream()
                .map(vln_IdPermiso ->
                        AsignarPermisoARol(
                                vln_IdRol,
                                vln_IdPermiso
                        )
                )
                .toList();
    }

    /**
     * Descripcion: inactiva/desactiva la asignacion de
     * un permiso asociado a un rol.
     * @param vln_IdRol identificador del rol.
     * @param vln_IdPermiso identificador del permiso.
     * @return relacion inactivada.
     */
    @Transactional
    public EntRolPermiso QuitarPermisoDeRol(
            Long vln_IdRol,
            Long vln_IdPermiso
    ) {

        if (vln_IdRol == null) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El identificador del rol es obligatorio."
            );
        }

        if (vln_IdPermiso == null) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El identificador del permiso es obligatorio."
            );
        }

        EntRol vlo_EnRol = ObtenerRol(vln_IdRol);
        EntPermiso vlo_EnPermiso =
                ObtenerPermiso(vln_IdPermiso);

        EntRolPermiso vlo_EnRolPermiso =
                vgo_DalRolPermiso
                        .ObtenerPorRolPermisoEstado(
                                vlo_EnRol,
                                vlo_EnPermiso,
                                ConstantesSeguridad.ESTADO_ACTIVO
                        )
                        .orElseThrow(() ->
                                new ExcepcionAutenticacion(
                    HttpStatus.NOT_FOUND,
                    "El permiso no se encuentra asignado al rol."
            )
                        );

        vlo_EnRolPermiso.setEstado(
                ConstantesSeguridad.ESTADO_INACTIVO
        );

        return vgo_DalRolPermiso.save(
                vlo_EnRolPermiso
        );
    }

    /**
     * Descripcion: obtiene los permisos activos
     * asociados a un rol.
     * @param vln_IdRol identificador del rol.
     * @return permisos activos del rol.
     */
    @Transactional(readOnly = true)
    public List<EntPermiso> ListarPermisosDeRol(
            Long vln_IdRol
    ) {

        if (vln_IdRol == null) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    "El identificador del rol es obligatorio."
            );
        }

        EntRol vlo_EnRol = ObtenerRol(vln_IdRol);

        return vgo_DalRolPermiso
                .ObtenerPorRolEstado(
                        vlo_EnRol,
                        ConstantesSeguridad.ESTADO_ACTIVO
                )
                .stream()
                .map(EntRolPermiso::getPermiso)
                .toList();
    }

    /**
     * Descripcion: obtiene y valida la existencia
     * de un rol.
     * @param vln_IdRol identificador del rol.
     * @return rol encontrado.
     */
    private EntRol ObtenerRol(Long vln_IdRol) {

        return vgo_DalRol.findById(vln_IdRol)
                .orElseThrow(() ->
                        new ExcepcionAutenticacion(
                    HttpStatus.NOT_FOUND,
                    "No existe un rol con el ID indicado."
            )
                );
    }

    /**
     * Descripcion: obtiene y valida la existencia
     * de un permiso.
     * @param vln_IdPermiso identificador del permiso.
     * @return permiso encontrado.
     */
    private EntPermiso ObtenerPermiso(
            Long vln_IdPermiso
    ) {

        return vgo_DalPermiso
                .findById(vln_IdPermiso)
                .orElseThrow(() ->
                        new ExcepcionAutenticacion(
                    HttpStatus.NOT_FOUND,
                    "No existe un permiso con el ID indicado."
            )
                );
    }
}