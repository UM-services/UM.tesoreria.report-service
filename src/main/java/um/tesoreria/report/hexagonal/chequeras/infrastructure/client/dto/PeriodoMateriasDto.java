package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto;

import java.util.List;

public record PeriodoMateriasDto(String periodo, List<MateriaInscriptaDto> materias) {
}
