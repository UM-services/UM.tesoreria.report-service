package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.Lectivo;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.LectivoRepository;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.LectivoClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper.ChequerasMapper;

@Component
@RequiredArgsConstructor
public class FeignLectivoRepositoryAdapter implements LectivoRepository {

    private final LectivoClient client;
    private final ChequerasMapper mapper;

    @Override
    public Lectivo findByLectivoId(Integer lectivoId) {
        return mapper.toDomain(client.findByLectivoId(lectivoId));
    }
}
