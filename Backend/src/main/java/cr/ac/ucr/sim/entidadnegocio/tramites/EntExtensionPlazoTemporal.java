package cr.ac.ucr.sim.entidadnegocio.tramites;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EntExtensionPlazoTemporal(
        @NotNull Integer cantidadPlazo,
        @NotBlank String unidadPlazo,
        @Size(max = 45) String numeroContrato) {
}
