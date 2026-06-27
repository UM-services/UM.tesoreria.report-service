package um.tesoreria.report.hexagonal.chequeras.domain.ports.out;

import um.tesoreria.report.hexagonal.chequeras.domain.model.ChequeraSerie;
import java.util.List;

public interface ChequeraSerieRepository {
    List<ChequeraSerie> findAllBySede(Integer facultadId, Integer lectivoId, Integer geograficaId);
}
