package cr.ac.ucr.sim.seguridad;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalSesion;
import cr.ac.ucr.sim.accesodatos.autenticacion.DalAutorizacion;
import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Configuración de seguridad del sistema.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003).
 *     - Se habilita la seguridad por método (@EnableMethodSecurity): cada servicio REST declara el
 *       permiso que exige con @PreAuthorize (ver ReglasAcceso y SrvAutorizacion).
 *     - Las rutas públicas pasan a la constante RUTAS_PUBLICAS, que la prueba CoberturaPermisosTest
 *       compara con los servicios marcados con @AccesoPublico. Se retira
 *       /api/autenticacion/asignar-rol, que ya no existe: un usuario no puede asignarse roles sin
 *       sesión.
 *     - Las respuestas 401 y 403 llevan el cuerpo JSON {"mensaje": "..."} que esperan las pantallas.
 */
@Configuration
@EnableMethodSecurity
public class CfgSeguridad {

    /**
     * Rutas que se usan sin sesión. Toda ruta nueva aquí debe tener su servicio marcado con
     * @AccesoPublico, y viceversa.
     */
    public static final String[] RUTAS_PUBLICAS = {
            "/api/autenticacion/login",
            "/api/autenticacion/registro",
            "/api/autenticacion/verificar",
    };

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: El inicio de sesión y el registro de cuenta son públicos; las demás llamadas
     *   a la API requieren un token de sesión. El permiso de cada operación lo valida
     *   @PreAuthorize en el servicio.
     *
     * @param pvo_HttpSecurity configuración HTTP
     * @return cadena de filtros
     * @throws Exception si la configuración falla
     */
    @Bean
    public SecurityFilterChain FiltroSeguridad(
            HttpSecurity pvo_HttpSecurity,
            DalSesion pvo_DalSesion,
            DalAutorizacion pvo_DalAutorizacion,
            DalUsuario pvo_DalUsuario) throws Exception {
        AuthenticationEntryPoint vlo_EntradaNoAutenticada =
                (pvo_Solicitud, pvo_Respuesta, pvo_Excepcion) ->
                        EscribirError(pvo_Respuesta, HttpServletResponse.SC_UNAUTHORIZED,
                                ConstantesSeguridad.MENSAJE_SESION_REQUERIDA);
        AccessDeniedHandler vlo_AccesoDenegado =
                (pvo_Solicitud, pvo_Respuesta, pvo_Excepcion) ->
                        EscribirError(pvo_Respuesta, HttpServletResponse.SC_FORBIDDEN,
                                ConstantesSeguridad.MENSAJE_ACCESO_DENEGADO);

        pvo_HttpSecurity
                .csrf(pvo_Csrf -> pvo_Csrf.disable())
                .formLogin(pvo_Formulario -> pvo_Formulario.disable())
                .httpBasic(pvo_Basic -> pvo_Basic.disable())
                .sessionManagement(pvo_Sesion -> pvo_Sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(pvo_Autorizacion -> pvo_Autorizacion
                        .requestMatchers(RUTAS_PUBLICAS)
                        .permitAll()
                        .requestMatchers("/error")
                        .permitAll()
                        .requestMatchers("/api/**")
                        .authenticated()
                        .anyRequest()
                        .permitAll())
                .exceptionHandling(pvo_Errores -> pvo_Errores
                        .authenticationEntryPoint(vlo_EntradaNoAutenticada)
                        .accessDeniedHandler(vlo_AccesoDenegado))
                .addFilterBefore(
                        new FiltroAutenticacionToken(pvo_DalSesion, pvo_DalAutorizacion, pvo_DalUsuario),
                        UsernamePasswordAuthenticationFilter.class);
        return pvo_HttpSecurity.build();
    }

    @Bean
    public PasswordEncoder CodificadorContrasena() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Escribe una respuesta de error con el cuerpo {"mensaje": "..."}, el mismo formato
     *   de ManejadorExcepciones. Los mensajes son constantes del sistema y no llevan comillas.
     *
     * @param pvo_Respuesta respuesta HTTP
     * @param pvn_Estado código de estado HTTP
     * @param pvc_Mensaje mensaje para la persona usuaria
     * @throws IOException si no se puede escribir la respuesta
     */
    private static void EscribirError(HttpServletResponse pvo_Respuesta, int pvn_Estado, String pvc_Mensaje)
            throws IOException {
        if (pvo_Respuesta.isCommitted()) {
            return;
        }
        pvo_Respuesta.setStatus(pvn_Estado);
        pvo_Respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        pvo_Respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        pvo_Respuesta.getWriter().write("{\"mensaje\":\"" + pvc_Mensaje.replace("\"", "'") + "\"}");
    }
}
