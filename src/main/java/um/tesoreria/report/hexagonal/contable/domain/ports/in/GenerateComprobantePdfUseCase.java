package um.tesoreria.report.hexagonal.contable.domain.ports.in;

public interface GenerateComprobantePdfUseCase {

    /** PDF de cualquier comprobante de proveedor: orden de pago, factura o nota de crédito. */
    byte[] generate(Long proveedorMovimientoId);
}