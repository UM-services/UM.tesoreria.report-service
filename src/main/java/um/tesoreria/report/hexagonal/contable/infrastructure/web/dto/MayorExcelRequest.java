package um.tesoreria.report.hexagonal.contable.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/** Misma selección que la consulta del mayor en el core: lista de cuentas o rango de cuentas, nunca ambas. */
public record MayorExcelRequest(
        OffsetDateTime desde,
        OffsetDateTime hasta,
        List<BigDecimal> cuentas,
        BigDecimal cuentaDesde,
        BigDecimal cuentaHasta) {
}