package cr.ac.ucr.sim.entidadnegocio.tramites;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Resultado de la consulta de la lista de trámites temporales (HU-025). Reúne el
 *   expediente común, sus datos temporales, su estado y su moneda, para que la lógica de negocio
 *   calcule la cuenta regresiva de cada fila.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: incluye el país de destino de la exportación temporal (HU-026).
 *
 * @param tramite expediente común
 * @param temporal datos propios del trámite temporal
 * @param estado estado actual del expediente
 * @param moneda código ISO de la moneda del valor declarado
 * @param paisDestino nombre del país de destino; nulo en la importación
 */
public record EntFilaTramiteTemporal(
        EntTramite tramite,
        EntTramiteTemporal temporal,
        EntEstadoTramite estado,
        String moneda,
        String paisDestino) {
}
