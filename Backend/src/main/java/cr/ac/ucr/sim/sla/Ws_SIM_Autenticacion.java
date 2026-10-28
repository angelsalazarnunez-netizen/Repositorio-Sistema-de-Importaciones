package cr.ac.ucr.sim.sla;

import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntIniciarSesion;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntRespuestaSesion;
import cr.ac.ucr.sim.logicanegocio.autenticacion.BllAutenticacion;
import cr.ac.ucr.sim.seguridad.AccesoPublico;
import cr.ac.ucr.sim.seguridad.ReglasAcceso;
import cr.ac.ucr.sim.seguridad.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servicio REST del inicio y cierre de sesión (HU-004).
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: se agrega GET /sesion, que devuelve los roles y permisos vigentes del usuario para
 *     que el menú refleje un cambio de roles sin volver a iniciar sesión (HU-003: "actualización de
 *     las funcionalidades disponibles para el usuario"). El inicio de sesión se marca como público
 *     y el cierre exige sesión.
 */
@RestController
@RequestMapping("/api/autenticacion")
public class Ws_SIM_Autenticacion {

    private final BllAutenticacion vgo_BllAutenticacion;

    public Ws_SIM_Autenticacion(BllAutenticacion pvo_BllAutenticacion) {
        this.vgo_BllAutenticacion = pvo_BllAutenticacion;
    }

    @AccesoPublico
    @PostMapping("/login")
    public ResponseEntity<EntRespuestaSesion> IniciarSesion(
            @Valid @RequestBody EntIniciarSesion pvo_Entrada) {
        return ResponseEntity.ok(this.vgo_BllAutenticacion.IniciarSesion(
                pvo_Entrada.getCorreoInstitucional(),
                pvo_Entrada.getContrasena()));
    }

    @PreAuthorize(ReglasAcceso.CON_SESION)
    @PostMapping("/logout")
    public ResponseEntity<Void> CerrarSesion(HttpServletRequest pvo_Solicitud) {
        String vlc_Autorizacion = pvo_Solicitud.getHeader("Authorization");
        String vlc_Token = vlc_Autorizacion != null && vlc_Autorizacion.startsWith("Bearer ")
                ? vlc_Autorizacion.substring("Bearer ".length()).trim()
                : null;
        this.vgo_BllAutenticacion.CerrarSesion(vlc_Token);
        return ResponseEntity.noContent().build();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Devuelve los datos de la sesión actual con los roles y permisos vigentes. No
     *   devuelve el token: la pantalla conserva el que recibió al iniciar sesión.
     *
     * @param pvo_Usuario usuario de la sesión
     * @return datos del usuario, roles y permisos efectivos
     */
    @PreAuthorize(ReglasAcceso.CON_SESION)
    @GetMapping("/sesion")
    public ResponseEntity<EntRespuestaSesion> ObtenerSesion(
            @AuthenticationPrincipal UsuarioAutenticado pvo_Usuario) {
        return ResponseEntity.ok(this.vgo_BllAutenticacion.ObtenerSesionActual(
                pvo_Usuario != null ? pvo_Usuario.idUsuario() : null));
    }
}
