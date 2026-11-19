package um.tesoreria.report.hexagonal.contable.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Lo que se pide del mayor: rango de fechas y cuentas, por lista o por rango. El report no lo valida ni lo
 * interpreta: lo pasa tal cual al core, que es quien decide si el pedido es válido.
 */
public record MayorSeleccion(
        OffsetDateTime desde,
        OffsetDateTime hasta,
        List<BigDecimal> cuentas,
        BigDecimal cuentaDesde,
        BigDecimal cuentaHasta) {
}