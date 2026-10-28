package cr.ac.ucr.sim.logicanegocio.tramites;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Autor: Camila Pastor Castro
 * Fecha: 03/10/2026
 * Descripción: Ejecuta diariamente el ciclo de alertas de vencimiento y los reintentos de los
 *   correos pendientes (HU-027).
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: usa BllAlertaTemporal, y la hora de ejecución se toma de la propiedad
 *     app.alertas-temporales.cron (por defecto, las 8:00) para poder probar las alertas en el
 *     ambiente local sin esperar al día siguiente.
 */
@Component
public class ProgramadorAlertasTemporales {

    private final BllAlertaTemporal vgo_BllAlertaTemporal;

    public ProgramadorAlertasTemporales(BllAlertaTemporal pvo_BllAlertaTemporal) {
        this.vgo_BllAlertaTemporal = pvo_BllAlertaTemporal;
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Genera las alertas del día y envía los correos pendientes.
     */
    @Scheduled(cron = "${app.alertas-temporales.cron:0 0 8 * * *}", zone = "America/Costa_Rica")
    public void GenerarAlertasDiarias() {
        this.vgo_BllAlertaTemporal.ProcesarAlertas(this.vgo_BllAlertaTemporal.Hoy());
    }
}
