package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.time.LocalDate;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Prórroga del plazo de una importación temporal (HU-025). La Unidad envía un oficio
 *   a la aduana, la aduana le asigna un número de gestión (a veces el mismo de la autorización
 *   inicial) y responde con la autorización. El vencimiento se calcula al registrar la prórroga.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: se agrega el motivo de la prórroga.
 *
 * @param motivoProrroga motivo por el que la unidad necesita más plazo
 * @param oficioProrroga oficio o correo con el que se solicita la prórroga
 * @param numeroGestionProrroga número de gestión de la prórroga
 * @param mismoNumeroGestion true para repetir el número de gestión de la autorización inicial
 * @param autorizacionProrroga respuesta de la aduana que autoriza la prórroga
 * @param fechaVencimientoActual vencimiento que el agente confirma antes de registrar
 * @param numeroContrato contrato requerido si el bien permanecerá en el país por años
 * @param permanecePorAnos indica si el bien permanecerá en el país por años
 */
public record EntProrrogaTemporal(
        String motivoProrroga,
        String oficioProrroga,
        String numeroGestionProrroga,
        Boolean mismoNumeroGestion,
        String autorizacionProrroga,
        LocalDate fechaVencimientoActual,
        String numeroContrato,
        Boolean permanecePorAnos) {
}
