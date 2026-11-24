package um.tesoreria.report.hexagonal.chequeras.domain.ports.out;

import um.tesoreria.report.hexagonal.chequeras.domain.model.Borrador;

import java.util.List;

public interface BorradorRepository {

    List<Borrador> findAllBySede(Integer facultadId, Integer lectivoId, Integer geograficaId);
}
