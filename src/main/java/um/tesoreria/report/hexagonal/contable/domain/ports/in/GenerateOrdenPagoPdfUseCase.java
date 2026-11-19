package um.tesoreria.report.hexagonal.contable.domain.ports.in;

public interface GenerateOrdenPagoPdfUseCase {

    /** Devuelve el contenido del {@code .pdf} de la orden de pago {@code proveedorMovimientoId}. */
    byte[] generate(Long proveedorMovimientoId);
}