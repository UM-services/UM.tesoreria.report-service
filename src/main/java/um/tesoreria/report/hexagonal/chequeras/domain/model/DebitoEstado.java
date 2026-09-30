package um.tesoreria.report.hexagonal.chequeras.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Una cuota adherida al débito automático. */
public record DebitoEstado(
        Integer cuotaId,
        BigDecimal importe,
        LocalDate fechaVencimiento,
        String cbu,
        String tipoDebito,
        LocalDateTime fechaEnvio,
        boolean rechazado,
        String motivoRechazo) {
}