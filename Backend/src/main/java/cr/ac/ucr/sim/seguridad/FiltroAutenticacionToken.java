package cr.ac.ucr.sim.seguridad;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalSesion;
import cr.ac.ucr.sim.accesodatos.autenticacion.DalAutorizacion;
import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntSesion;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.utilerias.Constantes;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FiltroAutenticacionToken extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final DalSesion vgo_DalSesion;
    private final DalAutorizacion vgo_DalAutorizacion;
    private final DalUsuario vgo_DalUsuario;

    public FiltroAutenticacionToken(
            DalSesion pvo_DalSesion,
            DalAutorizacion pvo_DalAutorizacion,
            DalUsuario pvo_DalUsuario) {
        this.vgo_DalSesion = pvo_DalSesion;
        this.vgo_DalAutorizacion = pvo_DalAutorizacion;
        this.vgo_DalUsuario = pvo_DalUsuario;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest pvo_Solicitud,
            HttpServletResponse pvo_Respuesta,
            FilterChain pvo_Cadena) throws ServletException, IOException {
        String vlc_Autorizacion = pvo_Solicitud.getHeader("Authorization");
        if (vlc_Autorizacion != null && vlc_Autorizacion.startsWith(PREFIJO_BEARER)) {
            String vlc_Token = vlc_Autorizacion.substring(PREFIJO_BEARER.length()).trim();
            if (!vlc_Token.isBlank()) {
                this.Autenticar(vlc_Token, pvo_Solicitud);
            }
        }
        pvo_Cadena.doFilter(pvo_Solicitud, pvo_Respuesta);
    }

    private void Autenticar(String pvc_Token, HttpServletRequest pvo_Solicitud) {
        EntSesion vlo_Sesion = this.vgo_DalSesion.SIT_SESION_ObtenerActiva(
                TokenSesionUtil.CalcularHash(pvc_Token),
                LocalDateTime.now());
        if (vlo_Sesion == null) {
            return;
        }

        EntUsuario vlo_Usuario = this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(vlo_Sesion.getIdUsuario());
        if (vlo_Usuario == null
                || !Constantes.ESTADO_VALIDACION_VALIDADO.equalsIgnoreCase(vlo_Usuario.getEstadoValidacion())
                || !Constantes.ESTADO_ROL_ASIGNADO.equalsIgnoreCase(vlo_Usuario.getEstadoRol())) {
            return;
        }
        List<String> vlo_Roles = this.vgo_DalAutorizacion
                .SIMT_USUARIO_ROL_ObtenerRolesActivos(vlo_Usuario.getIdUsuario());
        if (vlo_Roles.isEmpty()) {
            return;
        }
        List<String> vlo_Permisos = this.vgo_DalAutorizacion
                .SIMT_USUARIO_ROL_ObtenerPermisosActivos(vlo_Usuario.getIdUsuario());
        List<SimpleGrantedAuthority> vlo_Autoridades = new ArrayList<>();
        vlo_Roles.stream()
                .map(vlc_Rol -> "ROLE_" + vlc_Rol)
                .map(SimpleGrantedAuthority::new)
                .forEach(vlo_Autoridades::add);
        vlo_Permisos.stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(vlo_Autoridades::add);

        UsuarioAutenticado vlo_Principal = new UsuarioAutenticado(
                vlo_Usuario.getIdUsuario(),
                vlo_Usuario.getNombreCompleto(),
                vlo_Usuario.getCorreoInstitucional());
        UsernamePasswordAuthenticationToken vlo_Autenticacion =
                new UsernamePasswordAuthenticationToken(
                        vlo_Principal,
                        null,
                        vlo_Autoridades);
        vlo_Autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(pvo_Solicitud));
        SecurityContextHolder.getContext().setAuthentication(vlo_Autenticacion);
    }
}
