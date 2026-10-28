package cr.ac.ucr.sim.sla;

import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionAutenticacion;
import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Traduce las excepciones de negocio a respuestas HTTP con un mensaje en español,
 *   sin exponer códigos internos ni nombres de tablas o de clases, según el estándar D06.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: se traduce el rechazo de @PreAuthorize (restricciones por rol, HU-003) a un 403
 *     con el mismo cuerpo {"mensaje": "..."} que el resto de los errores.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: hallazgo H5 del Sprint 1. Un archivo que supera el tamaño permitido devuelve un
 *     mensaje comprensible en lugar de "Failed to fetch" o "Error 403".
 */
@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler(ExcepcionAutenticacion.class)
    public ResponseEntity<Map<String, String>> ManejarExcepcionAutenticacion(
            ExcepcionAutenticacion pvo_Excepcion) {
        Map<String, String> vlo_Cuerpo = new HashMap<>();
        vlo_Cuerpo.put("mensaje", pvo_Excepcion.getMessage());
        return ResponseEntity.status(pvo_Excepcion.getEstadoHttp()).body(vlo_Cuerpo);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Reporta que el usuario no tiene el permiso que exige el servicio (@PreAuthorize).
     *   Incluye AuthorizationDeniedException, que hereda de AccessDeniedException.
     *
     * @param pvo_Excepcion excepción de acceso denegado
     * @return 403 con el mensaje para la persona usuaria
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> ManejarAccesoDenegado(AccessDeniedException pvo_Excepcion) {
        Map<String, String> vlo_Cuerpo = new HashMap<>();
        vlo_Cuerpo.put("mensaje", ConstantesSeguridad.MENSAJE_ACCESO_DENEGADO);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(vlo_Cuerpo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> ManejarErrorValidacion(
            MethodArgumentNotValidException pvo_Excepcion) {
        Map<String, String> vlo_Errores = pvo_Excepcion.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> NombreCampoHttp(error.getField()),
                        error -> error.getDefaultMessage() == null
                                ? "El valor no es válido."
                                : error.getDefaultMessage(),
                        (pvc_Anterior, pvc_Nuevo) -> pvc_Anterior));
        Map<String, Object> vlo_Cuerpo = new HashMap<>();
        vlo_Cuerpo.put("mensaje", vlo_Errores.values().stream().findFirst()
                .orElse("El formulario contiene datos inválidos."));
        vlo_Cuerpo.put("errores", vlo_Errores);
        return ResponseEntity.badRequest().body(vlo_Cuerpo);
    }

    private static String NombreCampoHttp(String pvc_Campo) {
        return switch (pvc_Campo) {
            case "vgc_CorreoInstitucional" -> "correoInstitucional";
            case "vgc_Contrasena" -> "contrasena";
            default -> pvc_Campo;
        };
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Reporta el incumplimiento de una regla de negocio.
     *
     * @param pvo_Excepcion excepción de negocio
     * @return cuerpo JSON con el mensaje para la persona usuaria
     */
    @ExceptionHandler(ExcepcionNegocio.class)
    public ResponseEntity<Map<String, String>> ManejarExcepcionNegocio(ExcepcionNegocio pvo_Excepcion) {
        Map<String, String> vlo_Cuerpo = new HashMap<>();
        vlo_Cuerpo.put("mensaje", pvo_Excepcion.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(vlo_Cuerpo);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 25/10/2026
     * Descripción: Reporta que los archivos adjuntos superan el tamaño que acepta el servidor
     *   (spring.servlet.multipart: 10 MB por archivo y 50 MB por envío).
     *
     * @param pvo_Excepcion excepción del tamaño de la carga
     * @return 413 con el mensaje para la persona usuaria
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> ManejarTamanoArchivoExcedido(
            MaxUploadSizeExceededException pvo_Excepcion) {
        Map<String, String> vlo_Cuerpo = new HashMap<>();
        vlo_Cuerpo.put("mensaje", "Los archivos adjuntos superan el tamaño permitido: cada archivo puede "
                + "pesar hasta 10 MB y el envío completo hasta 50 MB. Reduzca el tamaño o adjúntelos por separado.");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(vlo_Cuerpo);
    }
}
