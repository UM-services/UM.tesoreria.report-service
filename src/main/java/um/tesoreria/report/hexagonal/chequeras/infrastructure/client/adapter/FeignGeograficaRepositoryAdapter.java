package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.Geografica;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.GeograficaRepository;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.GeograficaClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper.ChequerasMapper;

@Component
@RequiredArgsConstructor
public class FeignGeograficaRepositoryAdapter implements GeograficaRepository {

    private final GeograficaClient client;
    private final ChequerasMapper mapper;

    @Override
    public Geografica findByGeograficaId(Integer geograficaId) {
        return mapper.toDomain(client.findByGeograficaId(geograficaId));
    }
}
