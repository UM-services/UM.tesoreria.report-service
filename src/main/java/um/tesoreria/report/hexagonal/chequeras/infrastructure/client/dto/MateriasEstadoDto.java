package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto;

import java.util.List;

public record MateriasEstadoDto(String fuente, String estado, Integer cursoInscripcion,
                                String plan, String carrera, String observaciones, List<CursoMateriasDto> cursos) {
}
