package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Fila del panel de trámites activos (HU-030) tal como sale de la base de datos: los
 *   datos comunes del trámite y los datos propios de su tipo que el panel necesita para mostrar el
 *   motivo, el DUA, el valor declarado y el vencimiento. Los datos que no aplican al tipo vienen nulos.
 *
 * @param idTramite identificador del expediente
 * @param referenciaInterna referencia interna
 * @param codigoTipoTramite código del tipo de trámite
 * @param tipoTramite nombre del tipo de trámite
 * @param estado nombre del estado actual
 * @param categoriaEstado categoría del estado: PEN, ACT o FIN
 * @param grupoPanel grupo del estado en el panel; nulo si el estado no está clasificado
 * @param esFinal si el estado cierra el expediente
 * @param descripcion descripción del trámite
 * @param valorEstimado valor estimado o declarado
 * @param moneda código ISO de la moneda
 * @param numeroDua número de DUA de inicio (trámites temporales)
 * @param fechaVencimiento vencimiento del régimen (trámites temporales)
 * @param tipoDonacion tipo de donación
 * @param tipoExoneracion modalidad de exoneración
 * @param origenDependencia dependencia del desalmacenaje especial
 * @param descripcionPedido descripción del pedido a proveedor
 * @param montoOrden monto de la orden de compra del pedido
 * @param monedaOrden moneda de la orden de compra del pedido
 * @param fechaIngresoPais fecha de ingreso al país del pedido
 * @param fechaDesalmacenajePedido fecha de desalmacenaje del pedido
 * @param fechaRegistro fecha de registro del expediente
 * @param responsable nombre del usuario responsable
 */
public record EntFilaPanelTramite(
        Integer idTramite,
        String referenciaInterna,
        String codigoTipoTramite,
        String tipoTramite,
        String estado,
        String categoriaEstado,
        String grupoPanel,
        Boolean esFinal,
        String descripcion,
        BigDecimal valorEstimado,
        String moneda,
        String numeroDua,
        LocalDate fechaVencimiento,
        String tipoDonacion,
        String tipoExoneracion,
        String origenDependencia,
        String descripcionPedido,
        BigDecimal montoOrden,
        String monedaOrden,
        LocalDate fechaIngresoPais,
        LocalDate fechaDesalmacenajePedido,
        LocalDateTime fechaRegistro,
        String responsable) {
}
