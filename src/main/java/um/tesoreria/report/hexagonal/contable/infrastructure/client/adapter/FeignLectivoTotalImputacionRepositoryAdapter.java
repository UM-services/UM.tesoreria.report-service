package um.tesoreria.report.hexagonal.contable.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.LectivoTotalImputacionDto;
import um.tesoreria.report.hexagonal.contable.domain.model.LectivoTotalImputacion;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.LectivoTotalImputacionRepository;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.LectivoTotalImputacionClient;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.mapper.ContableMapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeignLectivoTotalImputacionRepositoryAdapter implements LectivoTotalImputacionRepository {

    private final LectivoTotalImputacionClient client;
    private final ContableMapper mapper;

    @Override
    public List<LectivoTotalImputacion> findAllByLectivo(Integer lectivoId) {
        List<LectivoTotalImputacionDto> dtos = client.findAllByLectivo(lectivoId);
        if (dtos == null) {
            return Collections.emptyList();
        }
        return dtos.stream()
                .map(dto -> mapper.toDomain(dto))
                .collect(Collectors.toList());
    }
}
