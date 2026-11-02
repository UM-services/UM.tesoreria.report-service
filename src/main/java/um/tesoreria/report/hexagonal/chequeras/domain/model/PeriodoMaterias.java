package um.tesoreria.report.hexagonal.chequeras.domain.model;

import java.util.List;

public record PeriodoMaterias(PeriodoCursada periodo, List<MateriaInscripta> materias) {
    public PeriodoMaterias {
        materias = materias == null ? List.of() : List.copyOf(materias);
    }
}
