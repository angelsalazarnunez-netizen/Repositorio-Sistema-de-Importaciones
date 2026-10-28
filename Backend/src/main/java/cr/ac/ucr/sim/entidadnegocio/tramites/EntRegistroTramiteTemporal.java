package cr.ac.ucr.sim.entidadnegocio.tramites;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Autor: Camila Pastor Castro
 * Fecha: 03/10/2026
 * Descripción: Datos del formulario de registro de un trámite temporal.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: recibe los datos de la importación temporal de la HU-025. Viaja como
 *     multipart/form-data junto con los documentos, por eso las fechas declaran su formato. Los
 *     agentes responsables pasan a ser opcionales y el vencimiento lo fija la aduana con una fecha.
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: recibe también la exportación temporal (HU-026): los últimos seis datos solo los
 *     registra la exportación y se ignoran en la importación.
 *
 * @param referenciaInterna referencia con la sigla del tipo (IT- o EXP-T-)
 * @param unidadSolicitante unidad de la universidad que solicita el trámite
 * @param oficioSolicitud número del oficio con el que la unidad hace la solicitud
 * @param fechaRecepcion fecha en que la Unidad de Importaciones recibe el oficio
 * @param descripcionMercancia descripción del equipo
 * @param estadoMercancia condición física del equipo: NUEVA, USADA o REACONDICIONADA
 * @param valorEquipo valor declarado del equipo
 * @param moneda código ISO de la moneda del valor declarado
 * @param aduanaControl aduana que controla el régimen, si ya se conoce
 * @param numeroFactura número de la factura del exportador, o SR si no trae
 * @param oficioAduana oficio o correo enviado a la aduana, si ya se envió
 * @param numeroGestion número de gestión que asignó la aduana, si ya existe
 * @param resolucionAutorizacion resolución de autorización de la aduana, si ya existe
 * @param numeroDuaInicio número del DUA de importación o de exportación temporal, si ya existe
 * @param fechaDuaInicio fecha de aceptación del DUA de importación o de exportación temporal
 * @param fechaVencimiento vencimiento que autorizó la aduana, si ya se conoce
 * @param observaciones observaciones del expediente
 * @param agentesResponsables agentes de aduanas que también reciben las alertas, opcionales
 * @param antecedente exportación: placa, pedido o donación con que la Universidad adquirió el equipo
 * @param enGarantia exportación: el equipo está en periodo de garantía
 * @param pesoKg exportación: peso del equipo en kilogramos
 * @param idPaisDestino exportación: país al que sale el equipo
 * @param oficioNotificacionUsuario exportación: oficio que notifica al usuario la autorización
 * @param notificacionDuaAduana exportación: correo con que se notificó a la aduana el DUA de salida
 */
public record EntRegistroTramiteTemporal(
        String referenciaInterna,
        String unidadSolicitante,
        String oficioSolicitud,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaRecepcion,
        String descripcionMercancia,
        String estadoMercancia,
        BigDecimal valorEquipo,
        String moneda,
        String aduanaControl,
        String numeroFactura,
        String oficioAduana,
        String numeroGestion,
        String resolucionAutorizacion,
        String numeroDuaInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDuaInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVencimiento,
        String observaciones,
        List<Integer> agentesResponsables,
        String antecedente,
        Boolean enGarantia,
        BigDecimal pesoKg,
        Integer idPaisDestino,
        String oficioNotificacionUsuario,
        String notificacionDuaAduana) {
}
