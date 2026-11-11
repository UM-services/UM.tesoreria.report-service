package um.tesoreria.report.hexagonal.chequeras.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import um.tesoreria.report.hexagonal.chequeras.domain.model.FormatoPlanilla;

import java.util.List;

public record PlanillaDetalleZipRequest(
        @NotNull Integer lectivoId,
        @NotNull FormatoPlanilla formato,
        @NotEmpty @Size(max = 50) List<@Valid Destino> destinos) {

    public record Destino(
            @NotNull Integer facultadId,
            @NotNull Integer geograficaId) {
    }
}
