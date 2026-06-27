package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.Legajo;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.LegajoRepository;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.LegajoClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper.ChequerasMapper;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeignLegajoRepositoryAdapter implements LegajoRepository {

    private final LegajoClient client;
    private final ChequerasMapper mapper;

    @Override
    public List<Legajo> findAllByFacultadId(Integer facultadId) {
        return client.findAllByFacultadId(facultadId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
