package cr.ac.ucr.sim.entidadnegocio.tramites;

public record EntDetallePedidoImportacion(
        Integer idTramite,
        String referenciaInterna,
        String estado,
        String usuarioResponsable,
        EntRegistroPedidoImportacion solicitud) {
}
