package um.tesoreria.report.hexagonal.contable.domain.model;

import java.math.BigDecimal;

public record MayorSaldoInicial(BigDecimal debe, BigDecimal haber, BigDecimal saldo) {
}