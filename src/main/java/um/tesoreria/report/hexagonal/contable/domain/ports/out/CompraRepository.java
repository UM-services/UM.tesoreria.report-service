package um.tesoreria.report.hexagonal.contable.domain.ports.out;

import um.tesoreria.report.hexagonal.contable.domain.model.Compra;

public interface CompraRepository {

    Compra findById(Long proveedorMovimientoId);
}