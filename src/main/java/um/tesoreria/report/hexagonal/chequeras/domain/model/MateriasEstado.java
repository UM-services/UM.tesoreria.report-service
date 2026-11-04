package um.tesoreria.report.hexagonal.chequeras.domain.model;

import java.util.List;

public record MateriasEstado(String fuente, EstadoMaterias estado, Integer cursoInscripcion,
                             String plan, String carrera, String observaciones, List<CursoMaterias> cursos) {
    public MateriasEstado {
        cursos = cursos == null ? List.of() : List.copyOf(cursos);
    }

    public String etiquetaFuente() {
        return "GUARANI".equals(fuente) ? "Guaraní" : fuente;
    }
}
