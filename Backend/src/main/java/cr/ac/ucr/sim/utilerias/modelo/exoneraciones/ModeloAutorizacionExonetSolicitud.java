package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Datos que envía la pantalla para registrar una solicitud / autorización EXONET o
 *   para completar los datos vacíos de la vigente (HU-023). Viaja como multipart/form-data junto
 *   con el archivo opcional, que el servicio recibe aparte; las fechas llegan en formato ISO
 *   (aaaa-mm-dd). No incluye el usuario: el servicio lo toma de la sesión autenticada. El estado,
 *   las fechas de registro y el enlace con otras autorizaciones los asigna el servidor.
 *   Todos los datos son opcionales en el modelo: las reglas de obligatoriedad y la normalización
 *   ("", "Nulo", "No aplica", mayúsculas) las aplica la lógica de negocio.
 *
 *   Las anotaciones @DateTimeFormat están en los setters porque el enlace de Spring busca las
 *   anotaciones por nombre de propiedad y los atributos usan el prefijo vgd_.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
public class ModeloAutorizacionExonetSolicitud {

    private String vgc_NumeroSolicitudExonet;
    private LocalDate vgd_FechaEnvioExonet;
    private String vgc_NumeroAutorizacion;
    private LocalDate vgd_FechaAutorizacion;
    private LocalDate vgd_FechaComunicacion;
    private String vgc_Observaciones;

    public ModeloAutorizacionExonetSolicitud() {
    }

    /**
     * Número de solicitud que devuelve EXONET al presentar la solicitud.
     */
    public String getNumeroSolicitudExonet() {
        return this.vgc_NumeroSolicitudExonet;
    }

    public void setNumeroSolicitudExonet(String pvc_NumeroSolicitudExonet) {
        this.vgc_NumeroSolicitudExonet = pvc_NumeroSolicitudExonet;
    }

    /**
     * Fecha de envío o presentación en EXONET.
     */
    public LocalDate getFechaEnvioExonet() {
        return this.vgd_FechaEnvioExonet;
    }

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    public void setFechaEnvioExonet(LocalDate pvd_FechaEnvioExonet) {
        this.vgd_FechaEnvioExonet = pvd_FechaEnvioExonet;
    }

    /**
     * Número de la autorización externa. El texto visible en pantalla puede cambiar; el nombre
     *   del dato no.
     */
    public String getNumeroAutorizacion() {
        return this.vgc_NumeroAutorizacion;
    }

    public void setNumeroAutorizacion(String pvc_NumeroAutorizacion) {
        this.vgc_NumeroAutorizacion = pvc_NumeroAutorizacion;
    }

    /**
     * Fecha de la autorización externa.
     */
    public LocalDate getFechaAutorizacion() {
        return this.vgd_FechaAutorizacion;
    }

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    public void setFechaAutorizacion(LocalDate pvd_FechaAutorizacion) {
        this.vgd_FechaAutorizacion = pvd_FechaAutorizacion;
    }

    /**
     * Fecha en que se comunicó la autorización al interesado.
     */
    public LocalDate getFechaComunicacion() {
        return this.vgd_FechaComunicacion;
    }

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    public void setFechaComunicacion(LocalDate pvd_FechaComunicacion) {
        this.vgd_FechaComunicacion = pvd_FechaComunicacion;
    }

    /**
     * Observaciones opcionales.
     */
    public String getObservaciones() {
        return this.vgc_Observaciones;
    }

    public void setObservaciones(String pvc_Observaciones) {
        this.vgc_Observaciones = pvc_Observaciones;
    }
}
