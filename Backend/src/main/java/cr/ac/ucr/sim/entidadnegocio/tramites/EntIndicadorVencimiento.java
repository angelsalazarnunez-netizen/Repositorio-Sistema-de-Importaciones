package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.time.LocalDate;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Cuenta regresiva del vencimiento de un trámite temporal (HU-025). No se guarda: se
 *   calcula en cada consulta a partir de la fecha de vencimiento.
 *
 * @param fechaVencimiento vencimiento vigente, nulo si la aduana aún no lo fija
 * @param diasRestantes días que faltan para el vencimiento; negativo si ya venció
 * @param nivel SIN_FECHA, EN_PLAZO, PROXIMO o VENCIDO; nulo si el expediente está cerrado
 * @param prorrogado true si el expediente registra una prórroga
 * @param indicacion recomendación de la HU-027 para el plazo estándar vencido, o texto vacío
 */
public record EntIndicadorVencimiento(
        LocalDate fechaVencimiento,
        Long diasRestantes,
        String nivel,
        boolean prorrogado,
        String indicacion) {
}
