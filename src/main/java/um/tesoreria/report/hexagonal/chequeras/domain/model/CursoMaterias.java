package um.tesoreria.report.hexagonal.chequeras.domain.model;

import java.util.List;

public record CursoMaterias(Integer curso, List<PeriodoMaterias> periodos) {
    public CursoMaterias {
        periodos = periodos == null ? List.of() : List.copyOf(periodos);
    }
}
