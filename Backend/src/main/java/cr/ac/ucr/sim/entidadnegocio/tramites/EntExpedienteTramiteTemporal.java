package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Expediente completo de una importación temporal (HU-025): datos comunes, datos
 *   propios del trámite, cuenta regresiva del vencimiento, pendientes operativos, agentes,
 *   documentación e historial de estados.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: atiende también la exportación temporal (HU-026) y muestra su país de destino.
 *
 * @param referenciaInterna referencia del expediente
 * @param tipoTramite nombre del tipo de trámite
 * @param estado nombre del estado actual
 * @param estadoFinal true si el expediente está cerrado
 * @param usuarioResponsable nombre del usuario responsable
 * @param fechaCreacion fecha y hora del registro
 * @param fechaCierre fecha en que el expediente se cerró
 * @param descripcionMercancia descripción del equipo
 * @param valorEquipo valor declarado del equipo
 * @param moneda código ISO de la moneda
 * @param aduanaControl aduana que controla el régimen
 * @param paisDestino exportación: nombre del país al que sale el equipo
 * @param observaciones observaciones del expediente
 * @param datosTemporal datos propios del trámite temporal
 * @param vencimiento cuenta regresiva del vencimiento
 * @param validacionesPausadas true si el equipo está retenido en aduana y los pendientes se pausan
 * @param pendientes datos que el expediente aún debe registrar en su etapa actual
 * @param agentes agentes de aduanas asignados
 * @param documentos documentación del expediente
 * @param historial historial de estados
 * @param historialProrrogas historial de prórrogas y documentos vinculados
 */
public record EntExpedienteTramiteTemporal(
        String referenciaInterna,
        String tipoTramite,
        String estado,
        Boolean estadoFinal,
        String usuarioResponsable,
        LocalDateTime fechaCreacion,
        LocalDate fechaCierre,
        String descripcionMercancia,
        BigDecimal valorEquipo,
        String moneda,
        String aduanaControl,
        String paisDestino,
        String observaciones,
        EntTramiteTemporal datosTemporal,
        EntIndicadorVencimiento vencimiento,
        boolean validacionesPausadas,
        List<String> pendientes,
        List<EntAgenteTemporal> agentes,
        List<EntDocumentoExpediente> documentos,
        List<EntMovimientoEstado> historial,
        List<EntHistorialProrrogaTemporal> historialProrrogas) {
}
