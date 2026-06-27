package um.tesoreria.report.hexagonal.chequeras.domain.ports.out;

import um.tesoreria.report.hexagonal.chequeras.domain.model.ChequeraCuotaPagos;
import java.util.List;

public interface ChequeraRepository {
    List<ChequeraCuotaPagos> findAllCuotaPagosByChequera(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId);
}
