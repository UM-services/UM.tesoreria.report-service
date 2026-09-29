package um.tesoreria.report.hexagonal.chequeras.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Una cuota con su primer pago, si lo tiene ({@code ordenPago}, {@code fechaPago},
 * {@code importePagado} y {@code referenciaPago} son {@code null} mientras esté impaga).
 */
public record CuotaEstado(
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