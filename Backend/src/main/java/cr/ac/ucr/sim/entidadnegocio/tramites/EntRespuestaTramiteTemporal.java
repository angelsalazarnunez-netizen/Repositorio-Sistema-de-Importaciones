package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.time.LocalDateTime;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Confirmación del registro de una importación temporal (HU-025).
 *
 * @param mensaje mensaje para la persona usuaria
 * @param referenciaInterna referencia del expediente creado
 * @param estado estado con el que nace el expediente
 * @param cantidadDocumentos documentos adjuntos en el registro
 * @param fechaCreacion fecha y hora del registro
 */
public record EntRespuestaTramiteTemporal(
        String mensaje,
        String referenciaInterna,
        String estado,
        int cantidadDocumentos,
        LocalDateTime fechaCreacion) {
}
