package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Datos de un expediente de importación temporal que se completan después del
 *   registro, conforme avanza el trámite ante la aduana (HU-025). La petición envía todos los
 *   datos: un valor vacío borra el dato registrado.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: atiende también la exportación temporal (HU-026): los últimos seis datos solo los
 *     registra la exportación y se ignoran en la importación.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 18/10/2026
 *   Descripción: la reimportación indica si paga impuestos y cuánto (comentario de la Unidad).
 *
 * @param aduanaControl aduana que controla el régimen
 * @param numeroFactura número de la factura del exportador
 * @param oficioAduana oficio o correo enviado a la aduana
 * @param numeroGestion número de gestión que asignó la aduana
 * @param resolucionAutorizacion resolución de autorización de la aduana
 * @param numeroDuaInicio número del DUA de importación o de exportación temporal
 * @param fechaDuaInicio fecha de aceptación del DUA de importación o de exportación temporal
 * @param fechaVencimiento vencimiento que autorizó la aduana
 * @param numeroDuaCierre número del DUA de reexportación o de reingreso
 * @param fechaDuaCierre fecha de aceptación del DUA de cierre
 * @param cancelacionRegimen oficio o correo con el que se cancela el régimen
 * @param fechaCancelacion fecha de la cancelación del régimen
 * @param observaciones observaciones del expediente
 * @param oficioNotificacionUsuario exportación: oficio que notifica al usuario la autorización
 * @param notificacionDuaAduana exportación: correo con que se notificó a la aduana el DUA de salida
 * @param solicitudIngreso exportación: oficio o correo con que el usuario solicita el reingreso
 * @param fechaReingreso exportación: fecha en que el equipo reingresó al país
 * @param documentoEmbarque exportación: guía o documento de embarque del reingreso
 * @param fechaDesalmacenaje exportación: fecha de desalmacenaje del equipo reimportado
 * @param pagaImpuestosReimportacion exportación: true si la reimportación paga impuestos por los
 *     trabajos, false si no los paga (por ejemplo, reparación en garantía)
 * @param montoImpuestosReimportacion exportación: monto de impuestos pagados en la reimportación
 */
public record EntDatosTramiteTemporal(
        String aduanaControl,
        String numeroFactura,
        String oficioAduana,
        String numeroGestion,
        String resolucionAutorizacion,
        String numeroDuaInicio,
        LocalDate fechaDuaInicio,
        LocalDate fechaVencimiento,
        String numeroDuaCierre,
        LocalDate fechaDuaCierre,
        String cancelacionRegimen,
        LocalDate fechaCancelacion,
        String observaciones,
        String oficioNotificacionUsuario,
        String notificacionDuaAduana,
        String solicitudIngreso,
        LocalDate fechaReingreso,
        String documentoEmbarque,
        LocalDate fechaDesalmacenaje,
        Boolean pagaImpuestosReimportacion,
        BigDecimal montoImpuestosReimportacion) {
}
