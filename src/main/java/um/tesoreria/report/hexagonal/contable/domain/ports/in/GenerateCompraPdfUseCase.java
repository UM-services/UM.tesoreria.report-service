package um.tesoreria.report.hexagonal.contable.domain.ports.in;

public interface GenerateCompraPdfUseCase {

    /** PDF de una factura o nota de crédito de compras. */
    byte[] generate(Long proveedorMovimientoId);
}