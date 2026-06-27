package um.tesoreria.report.hexagonal.chequeras.domain.ports.out;

import um.tesoreria.report.hexagonal.chequeras.domain.model.CuotaPeriodo;
import java.util.List;

public interface ChequeraCuotaRepository {
    List<CuotaPeriodo> findAllPeriodosLectivo(Integer lectivoId);
}
