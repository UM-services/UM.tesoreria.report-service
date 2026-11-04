package um.tesoreria.report.hexagonal.chequeras;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import um.tesoreria.report.hexagonal.chequeras.domain.model.*;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.MateriasEstadoDto;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper.ChequerasMapper;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

/** Fixture del contrato público #481. Titular y claves de EstadoChequeraFixture son ficticios. */
public final class MateriasInscriptasFixture {
    private MateriasInscriptasFixture() {}

    public static List<MateriasEstadoDto> dtos() throws IOException {
        try (var input = MateriasInscriptasFixture.class.getResourceAsStream("/materiasInscriptas/materias-control.json")) {
            return JsonMapper.builder().build().readValue(input, new TypeReference<List<MateriasEstadoDto>>() {});
        }
    }

    public static MateriasInscriptas referencia() throws IOException {
        return reporte(new ChequerasMapper().toMaterias(dtos()));
    }

    public static MateriasInscriptas reporte(List<MateriasEstado> materias) {
        return new MateriasInscriptas(EstadoChequeraFixture.referencia(), materias);
    }

    public static MateriasEstado estado(String fuente, EstadoMaterias estado) {
        return new MateriasEstado(fuente, estado, null, null, null, null, null);
    }

    public static MateriasEstado volumen(int cantidad) {
        return new MateriasEstado("SIUM", EstadoMaterias.OK, 0, "Plan de prueba", "Carrera de prueba", null,
                List.of(new CursoMaterias(null, List.of(new PeriodoMaterias(PeriodoCursada.SIN_PERIODO,
                        IntStream.rangeClosed(1, cantidad).mapToObj(i -> new MateriaInscripta(
                                "M%03d".formatted(i), "Materia de prueba %03d".formatted(i))).toList())))));
    }
}
