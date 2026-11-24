package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.Borrador;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.BorradorRepository;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.BorradorClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper.ChequerasMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FeignBorradorRepositoryAdapter implements BorradorRepository {

    private final BorradorClient client;
    private final ChequerasMapper mapper;

    @Override
    public List<Borrador> findAllBySede(Integer facultadId, Integer lectivoId, Integer geograficaId) {
        return client.findAllBySede(facultadId, lectivoId, geograficaId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
