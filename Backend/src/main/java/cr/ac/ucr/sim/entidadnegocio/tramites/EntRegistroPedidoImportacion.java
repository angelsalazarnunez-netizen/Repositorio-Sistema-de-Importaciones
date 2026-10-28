package cr.ac.ucr.sim.entidadnegocio.tramites;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos de entrada de la solicitud de importación (HU-006).
 */
public record EntRegistroPedidoImportacion(
        @NotBlank
        @Pattern(regexp = "\\d{4}-\\d{3}", message = "Use el formato AAAA-000.")
        String numeroSolicitud,
        @DecimalMin(value = "0.001")
        @Digits(integer = 10, fraction = 3)
        BigDecimal cantidadSolicitada,
        @NotNull
        LocalDate fechaRecibidoExpediente,
        @NotBlank
        @Size(max = 120)
        String unidadSolicitante,
        @NotBlank
        @Size(max = 4000)
        String descripcion,
        @NotBlank
        @Size(max = 100)
        String numeroContratacion,
        @NotBlank
        @Size(max = 20)
        String numeroPedido,
        @NotBlank
        @Size(max = 160)
        String companiaAdjudicada,
        @NotBlank
        @Size(max = 20)
        String numeroOrdenCompra,
        @NotBlank
        @Size(max = 10)
        @Pattern(regexp = "[A-Z]{2,10}", message = "Use siglas en mayúscula.")
        String incoterm,
        @DecimalMin("0")
        @DecimalMax("50000000")
        @Digits(integer = 13, fraction = 2)
        BigDecimal presupuestoOrdenCRC,
        @DecimalMin("0")
        @Digits(integer = 16, fraction = 2)
        BigDecimal montoOrden,
        @NotBlank
        @Size(max = 30)
        String monedaOrden,
        @DecimalMin("0")
        @Digits(integer = 16, fraction = 2)
        BigDecimal saldoDisponible,
        LocalDate fechaRecibidoSpOaf,
        LocalDate fechaRecibidoCopiaChequeUi,
        LocalDate fechaTransferencia,
        LocalDate fechaEstimadaDespacho,
        LocalDate fechaIngresoPais,
        LocalDate fechaDesalmacenaje,
        @Size(max = 100)
        String factura,
        @NotNull
        @Min(1)
        @Max(2)
        Integer cantidadPartidas,
        Integer partida,
        Integer partida2,
        @DecimalMin("0")
        @Digits(integer = 13, fraction = 2)
        BigDecimal montoCanceladoCRC,
        @Digits(integer = 13, fraction = 2)
        BigDecimal sobranteFaltanteCRC,
        @NotBlank
        @Size(max = 50)
        String estado,
        @Size(max = 2000)
        String reclamo) {
}
