package cr.ac.ucr.sim.logicanegocio.correo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Descripción: Servicio de envío de correos electrónicos.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main. El remitente se toma de
 *     app.notificaciones.correo-remitente, la misma propiedad que usan las demás notificaciones.
 *     Si el envío falla, el error queda en el log y no se interrumpe el registro de la cuenta.
 */
@Service
public class CorreoService {

    private static final Logger vgo_Bitacora = LoggerFactory.getLogger(CorreoService.class);

    private final JavaMailSender vgo_MailSender;
    private final String vgc_Remitente;

    public CorreoService(
            JavaMailSender pvo_MailSender,
            @Value("${app.notificaciones.correo-remitente}") String pvc_Remitente) {
        this.vgo_MailSender = pvo_MailSender;
        this.vgc_Remitente = pvc_Remitente;
    }

    /**
     * Descripción: Envía correo de validación institucional.
     *
     * @param pvc_Destinatario correo del destinatario
     * @param pvc_NombreCompleto nombre del destinatario
     * @param pvc_EnlaceVerificacion enlace de validación
     * @return verdadero si el correo se envió; falso si no se pudo enviar
     */
    public boolean EnviarCorreoValidacion(String pvc_Destinatario, String pvc_NombreCompleto, String pvc_EnlaceVerificacion) {
        try {
            SimpleMailMessage vlo_Mensaje = new SimpleMailMessage();
            vlo_Mensaje.setFrom(this.vgc_Remitente);
            vlo_Mensaje.setTo(pvc_Destinatario);
            vlo_Mensaje.setSubject("Validación de cuenta - Sistema de Importaciones");
            vlo_Mensaje.setText(ConstruirCorreoValidacion(pvc_NombreCompleto, pvc_EnlaceVerificacion));

            this.vgo_MailSender.send(vlo_Mensaje);
            vgo_Bitacora.info("Correo de validación enviado exitosamente a {}", pvc_Destinatario);
            return true;
        } catch (Exception vlo_Excepcion) {
            vgo_Bitacora.error("Error al enviar correo de validación a {}: {}", pvc_Destinatario, vlo_Excepcion.getMessage());
            return false;
        }
    }

    /**
     * Descripción: Construye el cuerpo del correo de validación.
     *
     * @param pvc_NombreCompleto nombre del destinatario
     * @param pvc_EnlaceVerificacion enlace de validación
     * @return cuerpo del correo
     */
    private String ConstruirCorreoValidacion(String pvc_NombreCompleto, String pvc_EnlaceVerificacion) {
        return String.format(
                "Estimado/a %s,%n\n" +
                        "Gracias por registrarse en el Sistema de Importaciones.%n\n" +
                        "Para completar el proceso de registro, por favor valida tu correo haciendo clic en el siguiente enlace:%n\n" +
                        "%s%n\n" +
                        "Este enlace expirará en 24 horas.%n\n" +
                        "Si no solicitaste este registro, por favor ignora este correo.%n\n" +
                        "Saludos cordiales,%n" +
                        "Sistema de Importaciones",
                pvc_NombreCompleto,
                pvc_EnlaceVerificacion
        );
    }
}