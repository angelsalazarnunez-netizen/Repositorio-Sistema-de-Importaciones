package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.time.LocalDate;

public record EntNotificacionPedidoImportacion(
        String referenciaInterna,
        LocalDate fechaTransferencia,
        long diasCalendarioPago,
        String mensaje) {
}
