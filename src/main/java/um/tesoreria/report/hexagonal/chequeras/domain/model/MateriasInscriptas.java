package um.tesoreria.report.hexagonal.chequeras.domain.model;

import java.util.List;

public record MateriasInscriptas(EstadoChequera encabezado, List<MateriasEstado> materias) {
    public MateriasInscriptas {
        materias = materias == null ? List.of() : List.copyOf(materias);
    }
}
