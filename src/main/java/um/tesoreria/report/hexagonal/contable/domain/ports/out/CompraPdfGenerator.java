package um.tesoreria.report.hexagonal.contable.domain.ports.out;

import um.tesoreria.report.hexagonal.contable.domain.model.Compra;

public interface CompraPdfGenerator {

    byte[] generate(Compra compra);
}