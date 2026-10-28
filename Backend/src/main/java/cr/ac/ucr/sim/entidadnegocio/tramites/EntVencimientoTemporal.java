package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.time.LocalDate;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Trámite temporal abierto cuyo vencimiento entra en el periodo de alerta (HU-027).
 *
 * @param idTramite identificador del expediente
 * @param idUsuarioResponsable usuario responsable, que siempre recibe la alerta
 * @param fechaVencimiento vencimiento vigente
 */
public record EntVencimientoTemporal(
        Integer idTramite,
        Integer idUsuarioResponsable,
        LocalDate fechaVencimiento) {
}
