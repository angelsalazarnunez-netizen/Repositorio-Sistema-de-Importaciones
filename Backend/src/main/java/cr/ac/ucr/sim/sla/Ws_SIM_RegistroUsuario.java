package cr.ac.ucr.sim.sla;

import cr.ac.ucr.sim.logicanegocio.seguridad.BllUsuario;
import cr.ac.ucr.sim.seguridad.AccesoPublico;
import cr.ac.ucr.sim.utilerias.modelo.seguridad.ModeloRegistroUsuarioSolicitud;
import cr.ac.ucr.sim.utilerias.modelo.seguridad.ModeloRegistroUsuarioRespuesta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Descripcion: servicio REST encargado del registro
 * y verificacion de cuentas de usuario.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main. La clase se llamaba Ws_SIM_Autenticacion; se renombra
 *     porque main ya tiene un servicio con ese nombre (inicio y cierre de sesión). Comparte con
 *     él la ruta /api/autenticacion, cuyos /registro y /verificar ya son públicos en
 *     CfgSeguridad.
 */
@RestController
@RequestMapping("/api/autenticacion")
public class Ws_SIM_RegistroUsuario {

    private final BllUsuario vgo_BllUsuario;

    public Ws_SIM_RegistroUsuario(
            BllUsuario vlo_BllUsuario) {

        this.vgo_BllUsuario =
                vlo_BllUsuario;
    }

    /**
     * Descripcion: registra una cuenta de usuario.
     *
     * @param vlo_Registro datos del registro.
     * @return resultado del registro.
     */
    @AccesoPublico
    @PostMapping("/registro")
    public ResponseEntity<ModeloRegistroUsuarioRespuesta>
    RegistrarUsuario(
            @RequestBody
            ModeloRegistroUsuarioSolicitud vlo_Registro) {

        ModeloRegistroUsuarioRespuesta vlo_Respuesta =
                vgo_BllUsuario.InsertarRegistro(
                        vlo_Registro
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vlo_Respuesta);
    }

    /**
     * Descripcion: valida el correo institucional
     * mediante el token enviado al usuario.
     *
     * @param vlc_Token token de verificacion.
     * @return resultado de la verificacion.
     */
    @AccesoPublico
    @GetMapping("/verificar")
    public ResponseEntity<ModeloRegistroUsuarioRespuesta>
    VerificarCorreo(
            @RequestParam("token")
            String vlc_Token) {

        ModeloRegistroUsuarioRespuesta vlo_Respuesta =
                vgo_BllUsuario
                        .VerificarCorreoInstitucional(
                                vlc_Token
                        );

        return ResponseEntity.ok(
                vlo_Respuesta
        );
    }
}