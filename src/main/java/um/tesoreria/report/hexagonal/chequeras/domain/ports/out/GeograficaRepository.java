package um.tesoreria.report.hexagonal.chequeras.domain.ports.out;

import um.tesoreria.report.hexagonal.chequeras.domain.model.Geografica;

public interface GeograficaRepository {
    Geografica findByGeograficaId(Integer geograficaId);
}
