package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.time.LocalDate;

/**
 * Autor: Camila Pastor Castro
 * Fecha: 03/10/2026
 * Descripción: Fila de la bandeja de alertas de vencimiento (HU-027).
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: la referencia, el tipo y la descripción se leen del expediente común, y la
 *     cédula del responsable se sustituye por la unidad solicitante (HU-025).
 *
 * @param idAlerta identificador de la alerta
 * @param referenciaInterna referencia del expediente
 * @param tipoTramite nombre del tipo de trámite
 * @param unidadSolicitante unidad que solicita el trámite
 * @param descripcionMercancia descripción del equipo
 * @param fechaVencimiento vencimiento al que se refiere la alerta
 * @param cantidadPlazo plazo de la HU-027, si se registró
 * @param unidadPlazo unidad del plazo de la HU-027, si se registró
 * @param cantidadExtension extensión de la HU-027, si se registró
 * @param unidadExtension unidad de la extensión, si se registró
 * @param numeroContrato contrato del plazo en años, si se registró
 * @param estado PENDIENTE o ATENDIDA
 * @param estadoCorreo PENDIENTE o ENVIADO
 * @param indicacionVencimiento recomendación para el plazo estándar vencido, o texto vacío
 */
public record EntResumenAlertaTemporal(
        Integer idAlerta,
        String referenciaInterna,
        String tipoTramite,
        String unidadSolicitante,
        String descripcionMercancia,
        LocalDate fechaVencimiento,
        Integer cantidadPlazo,
        String unidadPlazo,
        Integer cantidadExtension,
        String unidadExtension,
        String numeroContrato,
        String estado,
        String estadoCorreo,
        String indicacionVencimiento) {
}
