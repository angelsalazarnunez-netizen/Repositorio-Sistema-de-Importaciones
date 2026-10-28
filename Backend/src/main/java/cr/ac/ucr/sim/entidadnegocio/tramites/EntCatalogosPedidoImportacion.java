package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.util.List;

public record EntCatalogosPedidoImportacion(
        List<String> unidadesSolicitantes,
        List<String> companiasAdjudicadas,
        List<String> incoterms,
        List<String> monedas,
        // Estados del catálogo para el pedido, en su orden: la pantalla los ofrece en una lista
        // para que no se escriban estados distintos para lo mismo.
        List<String> estados) {
}
