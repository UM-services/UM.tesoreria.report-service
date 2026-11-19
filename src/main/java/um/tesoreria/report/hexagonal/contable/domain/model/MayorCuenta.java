package um.tesoreria.report.hexagonal.contable.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record MayorCuenta(
        BigDecimal numeroCuenta,
        String nombre,
        MayorSaldoInicial saldoInicial,
        List<MayorMovimiento> movimientos,
        BigDecimal totalDebe,
        BigDecimal totalHaber,
        BigDecimal saldoFinal) {
}