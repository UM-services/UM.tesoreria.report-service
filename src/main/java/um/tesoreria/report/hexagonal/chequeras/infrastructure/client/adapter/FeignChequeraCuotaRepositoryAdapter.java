package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.CuotaPeriodo;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.ChequeraCuotaRepository;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.ChequeraCuotaClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper.ChequerasMapper;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeignChequeraCuotaRepositoryAdapter implements ChequeraCuotaRepository {

    private final ChequeraCuotaClient client;
    private final ChequerasMapper mapper;

    @Override
    public List<CuotaPeriodo> findAllPeriodosLectivo(Integer lectivoId) {
        return client.findAllPeriodosLectivo(lectivoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
