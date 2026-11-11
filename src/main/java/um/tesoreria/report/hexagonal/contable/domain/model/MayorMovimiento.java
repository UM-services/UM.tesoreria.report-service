package um.tesoreria.report.hexagonal.contable.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Una línea del mayor; {@code debe} y {@code haber} vienen nulos del lado que no corresponde. */
public record MayorMovimiento(
        OffsetDateTime fechaContable,
        Integer ordenContable,
        Integer ejercicioId,
        String ejercicioNombre,
        String comprobante,
        String proveedor,
        String concepto,
        String ordenPago,
        String beneficiario,
        BigDecimal debe,
        BigDecimal haber,
        BigDecimal saldo,
        String ordenPagoValor,
        String valor,
        Long numeroValor,
        BigDecimal importeValor) {
}