package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DebitoEstadoDto(
        Integer cuotaId,
        BigDecimal importe,
        LocalDate fechaVencimiento,
        String cbu,
        LocalDateTime fechaEnvio,
        boolean rechazado,
        String motivoRechazo) {
}