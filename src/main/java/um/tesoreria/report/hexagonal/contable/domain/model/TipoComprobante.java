package um.tesoreria.report.hexagonal.contable.domain.model;

/** Cómo se imprime un comprobante de proveedor: las órdenes de pago tienen su propio formato; el resto es compra. */
public enum TipoComprobante {
    ORDEN_PAGO, FACTURA;

    public static TipoComprobante desde(String tipo) {
        return "ORDEN_PAGO".equalsIgnoreCase(tipo) ? ORDEN_PAGO : FACTURA;
    }
}