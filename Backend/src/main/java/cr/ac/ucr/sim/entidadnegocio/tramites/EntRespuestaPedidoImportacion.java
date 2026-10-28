package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.time.LocalDateTime;

public record EntRespuestaPedidoImportacion(
        String mensaje,
        Integer idTramite,
        String referenciaInterna,
        String estado,
        String usuarioResponsable,
        LocalDateTime fechaCreacion) {
}
