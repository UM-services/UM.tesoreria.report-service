package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.ChequeraCuotaPagos;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.ChequeraRepository;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.facade.ChequeraClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper.ChequerasMapper;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeignChequeraRepositoryAdapter implements ChequeraRepository {

    private final ChequeraClient client;
    private final ChequerasMapper mapper;

    @Override
    public List<ChequeraCuotaPagos> findAllCuotaPagosByChequera(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId) {
        return client.findAllCuotaPagosByChequera(facultadId, tipoChequeraId, chequeraSerieId, alternativaId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
