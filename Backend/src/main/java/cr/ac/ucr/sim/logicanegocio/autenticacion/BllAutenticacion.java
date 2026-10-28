package cr.ac.ucr.sim.logicanegocio.autenticacion;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalAutorizacion;
import cr.ac.ucr.sim.accesodatos.autenticacion.DalSesion;
import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntRespuestaSesion;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntSesion;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.seguridad.TokenSesionUtil;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BllAutenticacion {

    private final DalUsuario vgo_DalUsuario;
    private final DalAutorizacion vgo_DalAutorizacion;
    private final DalSesion vgo_DalSesion;
    private final PasswordEncoder vgo_PasswordEncoder;

    public BllAutenticacion(
            DalUsuario pvo_DalUsuario,
            DalAutorizacion pvo_DalAutorizacion,
            DalSesion pvo_DalSesion,
            PasswordEncoder pvo_PasswordEncoder) {
        this.vgo_DalUsuario = pvo_DalUsuario;
        this.vgo_DalAutorizacion = pvo_DalAutorizacion;
        this.vgo_DalSesion = pvo_DalSesion;
        this.vgo_PasswordEncoder = pvo_PasswordEncoder;
    }

    @Transactional
    public EntRespuestaSesion IniciarSesion(String pvc_CorreoInstitucional, String pvc_Contrasena) {
        if (pvc_CorreoInstitucional == null || pvc_Contrasena == null
                || pvc_CorreoInstitucional.isBlank() || pvc_Contrasena.isBlank()) {
            throw new ExcepcionAutenticacion(HttpStatus.UNAUTHORIZED, Constantes.MENSAJE_CREDENCIALES_INVALIDAS);
        }

        EntUsuario vlo_Usuario = this.vgo_DalUsuario
                .SIMF_USUARIO_ObtenerPorCorreoInstitucional(pvc_CorreoInstitucional.trim());
        if (vlo_Usuario == null || vlo_Usuario.getContrasenaHash() == null
                || !this.vgo_PasswordEncoder.matches(pvc_Contrasena, vlo_Usuario.getContrasenaHash())) {
            throw new ExcepcionAutenticacion(HttpStatus.UNAUTHORIZED, Constantes.MENSAJE_CREDENCIALES_INVALIDAS);
        }

        if (!Constantes.ESTADO_VALIDACION_VALIDADO.equalsIgnoreCase(vlo_Usuario.getEstadoValidacion())) {
            throw new ExcepcionAutenticacion(HttpStatus.UNAUTHORIZED, Constantes.MENSAJE_CORREO_NO_VALIDADO);
        }
        List<String> vlo_Roles = this.vgo_DalAutorizacion
                .SIMT_USUARIO_ROL_ObtenerRolesActivos(vlo_Usuario.getIdUsuario());
        if (!Constantes.ESTADO_ROL_ASIGNADO.equalsIgnoreCase(vlo_Usuario.getEstadoRol())
                || vlo_Roles.isEmpty()) {
            throw new ExcepcionAutenticacion(HttpStatus.FORBIDDEN, Constantes.MENSAJE_USUARIO_SIN_ROL);
        }
        List<String> vlo_Permisos = this.vgo_DalAutorizacion
                .SIMT_USUARIO_ROL_ObtenerPermisosActivos(vlo_Usuario.getIdUsuario());

        String vlc_Token = TokenSesionUtil.GenerarToken();
        EntSesion vlo_Sesion = new EntSesion();
        vlo_Sesion.setIdUsuario(vlo_Usuario.getIdUsuario());
        vlo_Sesion.setTokenHash(TokenSesionUtil.CalcularHash(vlc_Token));
        vlo_Sesion.setFechaExpiracion(LocalDateTime.now().plusHours(Constantes.DURACION_SESION_HORAS));
        this.vgo_DalSesion.SIT_SESION_Guardar(vlo_Sesion);

        EntRespuestaSesion vlo_Respuesta = new EntRespuestaSesion();
        vlo_Respuesta.setIdUsuario(vlo_Usuario.getIdUsuario());
        vlo_Respuesta.setNombreCompleto(vlo_Usuario.getNombreCompleto());
        vlo_Respuesta.setCorreoInstitucional(vlo_Usuario.getCorreoInstitucional());
        vlo_Respuesta.setToken(vlc_Token);
        vlo_Respuesta.setMensaje(Constantes.MENSAJE_INICIO_SESION_EXITOSO);
        vlo_Respuesta.setRoles(vlo_Roles);
        vlo_Respuesta.setPermisos(vlo_Permisos);
        return vlo_Respuesta;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Obtiene los roles y permisos vigentes del usuario de la sesión (HU-003). Los
     *   permisos efectivos son la unión de los permisos de sus roles activos, igual que al iniciar
     *   sesión y que en FiltroAutenticacionToken.
     *
     * @param pvn_IdUsuario usuario de la sesión
     * @return datos del usuario, roles y permisos, sin token
     */
    @Transactional(readOnly = true)
    public EntRespuestaSesion ObtenerSesionActual(Integer pvn_IdUsuario) {
        EntUsuario vlo_Usuario = pvn_IdUsuario == null
                ? null
                : this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(pvn_IdUsuario);
        if (vlo_Usuario == null) {
            throw new ExcepcionAutenticacion(HttpStatus.UNAUTHORIZED, ConstantesSeguridad.MENSAJE_SESION_REQUERIDA);
        }

        EntRespuestaSesion vlo_Respuesta = new EntRespuestaSesion();
        vlo_Respuesta.setIdUsuario(vlo_Usuario.getIdUsuario());
        vlo_Respuesta.setNombreCompleto(vlo_Usuario.getNombreCompleto());
        vlo_Respuesta.setCorreoInstitucional(vlo_Usuario.getCorreoInstitucional());
        vlo_Respuesta.setRoles(this.vgo_DalAutorizacion
                .SIMT_USUARIO_ROL_ObtenerRolesActivos(vlo_Usuario.getIdUsuario()));
        vlo_Respuesta.setPermisos(this.vgo_DalAutorizacion
                .SIMT_USUARIO_ROL_ObtenerPermisosActivos(vlo_Usuario.getIdUsuario()));
        return vlo_Respuesta;
    }

    @Transactional
    public void CerrarSesion(String pvc_Token) {
        if (pvc_Token != null && !pvc_Token.isBlank()) {
            this.vgo_DalSesion.SIT_SESION_Desactivar(TokenSesionUtil.CalcularHash(pvc_Token));
        }
    }
}
