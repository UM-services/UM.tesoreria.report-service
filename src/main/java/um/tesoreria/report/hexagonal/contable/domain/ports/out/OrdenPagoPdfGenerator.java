package um.tesoreria.report.hexagonal.contable.domain.ports.out;

import um.tesoreria.report.hexagonal.contable.domain.model.OrdenPago;

public interface OrdenPagoPdfGenerator {

    /** Arma el PDF con la orden tal como la informa el core; no calcula ni reordena nada. */
    byte[] generate(OrdenPago orden);
}