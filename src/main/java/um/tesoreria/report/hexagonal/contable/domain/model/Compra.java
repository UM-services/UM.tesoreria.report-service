package um.tesoreria.report.hexagonal.contable.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Factura o nota de crédito de compras lista para imprimir, tal como la informa el core. Los importes llegan con su
 * signo (una nota de crédito es negativa) y el report no recalcula nada.
 */
public record Compra(Long proveedorMovimientoId,
                     String comprobanteDescripcion,
                     String numero,
                     OffsetDateTime fecha,
                     OffsetDateTime fechaAnulacion,
                     String concepto,
                     String razonSocial,
                     String cuit,
                     BigDecimal importe,
                     List<Detalle> detalle,
                     OffsetDateTime fechaContable,
                     Integer ordenContable,
                     List<Asiento> asiento,
                     BigDecimal totalDebe,
                     BigDecimal totalHaber) {

    public record Detalle(String articulo, String concepto, BigDecimal cantidad,
                          BigDecimal precioUnitario, BigDecimal precioFinal) {
    }

    public record Asiento(BigDecimal numeroCuenta, String nombreCuenta, BigDecimal debe, BigDecimal haber) {
    }
}