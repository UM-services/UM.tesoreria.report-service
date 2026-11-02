package um.tesoreria.report.hexagonal.chequeras.domain.ports.out;

import um.tesoreria.report.hexagonal.chequeras.domain.model.MateriasEstado;
import java.util.List;

public interface MateriasInscriptasRepository {
    List<MateriasEstado> findMaterias(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId);
}
