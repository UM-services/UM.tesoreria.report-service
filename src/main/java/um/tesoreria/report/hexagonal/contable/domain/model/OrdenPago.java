package um.tesoreria.report.hexagonal.contable.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/** Orden de pago lista para imprimir, tal como la informa el core. El report no calcula totales. */
public record OrdenPago(Long proveedorMovimientoId,
                        Integer prefijo,
                        Long numeroComprobante,
                        OffsetDateTime fecha,
                        OffsetDateTime fechaAnulacion,
                        String concepto,
                        String razonSocial,
                        String domicilio,
                        String cuit,
                        List<Comprobante> comprobantes,
                        BigDecimal totalComprobantes,
                        List<Valor> valores,
                        BigDecimal totalValores,
                        List<Traza> traza,
                        BigDecimal totalDebe,
                        BigDecimal totalHaber) {

    public record Comprobante(String descripcion, Integer prefijo, Long numeroComprobante,
                              OffsetDateTime fecha, BigDecimal importe) {
    }

    public record Valor(String concepto, String titular, Long numero, OffsetDateTime fecha, BigDecimal importe) {
    }

    public record Traza(BigDecimal numeroCuenta, String nombreCuenta, BigDecimal debe, BigDecimal haber) {
    }
}