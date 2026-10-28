package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EntResumenPedidoImportacion(
        String referenciaInterna,
        String estado,
        String unidadSolicitante,
        String companiaAdjudicada,
        String numeroContratacion,
        LocalDate fechaRecibidoExpediente,
        LocalDateTime fechaCreacion) {
}
