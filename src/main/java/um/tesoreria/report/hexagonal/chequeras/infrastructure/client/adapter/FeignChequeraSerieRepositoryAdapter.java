package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.ChequeraSerie;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.ChequeraSerieRepository;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.ChequeraSerieClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper.ChequerasMapper;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeignChequeraSerieRepositoryAdapter implements ChequeraSerieRepository {

    private final ChequeraSerieClient client;
    private final ChequerasMapper mapper;

    @Override
    public List<ChequeraSerie> findAllBySede(Integer facultadId, Integer lectivoId, Integer geograficaId) {
        return client.findAllBySede(facultadId, lectivoId, geograficaId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
