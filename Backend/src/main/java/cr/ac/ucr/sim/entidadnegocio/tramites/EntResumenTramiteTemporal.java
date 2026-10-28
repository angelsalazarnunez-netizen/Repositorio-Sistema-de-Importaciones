package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Autor: Camila Pastor Castro
 * Fecha: 03/10/2026
 * Descripción: Fila de la lista de trámites temporales.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: muestra los datos del expediente de importación temporal (HU-025) y la cuenta
 *     regresiva de su vencimiento.
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: atiende también la exportación temporal (HU-026) y muestra su país de destino.
 *
 * @param referenciaInterna referencia del expediente
 * @param estado nombre del estado actual
 * @param estadoFinal true si el expediente está cerrado
 * @param unidadSolicitante unidad que solicita el trámite
 * @param descripcionMercancia descripción del equipo
 * @param valorEquipo valor declarado del equipo
 * @param moneda código ISO de la moneda
 * @param fechaRecepcion fecha de recepción del oficio
 * @param paisDestino exportación: nombre del país al que sale el equipo
 * @param vencimiento cuenta regresiva del vencimiento
 */
public record EntResumenTramiteTemporal(
        String referenciaInterna,
        String estado,
        Boolean estadoFinal,
        String unidadSolicitante,
        String descripcionMercancia,
        BigDecimal valorEquipo,
        String moneda,
        LocalDate fechaRecepcion,
        String paisDestino,
        EntIndicadorVencimiento vencimiento) {
}
