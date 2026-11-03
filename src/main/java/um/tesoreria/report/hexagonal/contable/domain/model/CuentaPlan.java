package um.tesoreria.report.hexagonal.contable.domain.model;

import java.math.BigDecimal;

/**
 * Cuenta del plan tal como la informa el core. {@code integradora} y {@code visible} son 1/0 (o null si el core
 * no los informa); {@code geograficaId} es la sede, null o 0 si la cuenta no tiene sede.
 */
public record CuentaPlan(BigDecimal numeroCuenta, String nombre, Integer grado, Byte integradora, Byte visible,
                         Integer geograficaId) {
}