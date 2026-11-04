package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto;

import java.util.List;

public record CursoMateriasDto(Integer curso, List<PeriodoMateriasDto> periodos) {
}
