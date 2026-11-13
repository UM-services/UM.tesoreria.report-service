package um.tesoreria.report.hexagonal.contable.domain.ports.out;

import um.tesoreria.report.hexagonal.contable.domain.model.TipoComprobante;

public interface ComprobanteTipoRepository {

    /** Tipo del comprobante, para elegir el formato de impresión. */
    TipoComprobante findTipo(Long proveedorMovimientoId);
}