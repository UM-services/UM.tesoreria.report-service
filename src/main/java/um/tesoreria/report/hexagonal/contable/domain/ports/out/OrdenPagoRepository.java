package um.tesoreria.report.hexagonal.contable.domain.ports.out;

import um.tesoreria.report.hexagonal.contable.domain.model.OrdenPago;

public interface OrdenPagoRepository {

    /** Pide la orden al core. Un rechazo del core (no existe, no es una orden de pago) se propaga como excepción del cliente. */
    OrdenPago findById(Long proveedorMovimientoId);
}