package um.tesoreria.report.hexagonal.contable.domain.ports.out;

import um.tesoreria.report.hexagonal.contable.domain.model.Mayor;

public interface MayorPdfGenerator {

    /** Arma el PDF con el mayor tal como lo informa el core; no calcula ni reordena nada. */
    byte[] generate(Mayor mayor);
}