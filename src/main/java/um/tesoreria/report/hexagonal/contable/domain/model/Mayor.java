package um.tesoreria.report.hexagonal.contable.domain.model;

import java.time.OffsetDateTime;
import java.util.List;

/** Mayor tal como lo calcula el core: un bloque por cuenta. */
public record Mayor(OffsetDateTime desde, OffsetDateTime hasta, List<MayorCuenta> cuentas) {
}