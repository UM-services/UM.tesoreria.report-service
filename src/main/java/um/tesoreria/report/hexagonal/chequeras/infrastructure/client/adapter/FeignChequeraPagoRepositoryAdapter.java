package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.ChequeraPago;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.ChequeraPagoRepository;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.ChequeraPagoClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper.ChequerasMapper;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeignChequeraPagoRepositoryAdapter implements ChequeraPagoRepository {

    private final ChequeraPagoClient client;
    private final ChequerasMapper mapper;

    @Override
    public List<ChequeraPago> findAllByFacultadIdAndTipoChequeraIdAndLectivoId(Integer facultadId, Integer tipoChequeraId, Integer lectivoId) {
        return client.findAllByFacultadIdAndTipoChequeraIdAndLectivoId(facultadId, tipoChequeraId, lectivoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
