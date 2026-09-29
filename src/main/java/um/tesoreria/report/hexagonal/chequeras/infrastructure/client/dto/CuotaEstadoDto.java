package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CuotaEstadoDto(
        Integer cuotaId,
        Integer mes,
        Integer anho,
        LocalDate primerVencimiento,
        BigDecimal importe,
        Integer ordenPago,
        LocalDate fechaPago,
        BigDecimal importePagado,
        String referenciaPago) {
}