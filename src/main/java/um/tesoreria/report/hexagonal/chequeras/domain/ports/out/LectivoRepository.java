package um.tesoreria.report.hexagonal.chequeras.domain.ports.out;

import um.tesoreria.report.hexagonal.chequeras.domain.model.Lectivo;

public interface LectivoRepository {
    Lectivo findByLectivoId(Integer lectivoId);
}
