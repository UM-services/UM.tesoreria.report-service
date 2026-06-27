package um.tesoreria.report.hexagonal.chequeras.domain.ports.out;

import um.tesoreria.report.hexagonal.chequeras.domain.model.Legajo;
import java.util.List;

public interface LegajoRepository {
    List<Legajo> findAllByFacultadId(Integer facultadId);
}
