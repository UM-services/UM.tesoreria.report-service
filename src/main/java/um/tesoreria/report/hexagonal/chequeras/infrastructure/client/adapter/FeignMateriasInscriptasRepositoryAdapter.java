package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.MateriasEstado;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.MateriasInscriptasRepository;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.MateriasInscriptasClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper.ChequerasMapper;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FeignMateriasInscriptasRepositoryAdapter implements MateriasInscriptasRepository {
    private final MateriasInscriptasClient client;
    private final ChequerasMapper mapper;

    @Override
    public List<MateriasEstado> findMaterias(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        return mapper.toMaterias(java.util.Objects.requireNonNull(
                client.findMaterias(facultadId, tipoChequeraId, chequeraSerieId),
                "El core devolvió un cuerpo vacío en materiasInscriptas"));
    }
}
