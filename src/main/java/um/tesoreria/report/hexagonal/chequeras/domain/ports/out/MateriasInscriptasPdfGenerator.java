package um.tesoreria.report.hexagonal.chequeras.domain.ports.out;

import um.tesoreria.report.hexagonal.chequeras.domain.model.MateriasInscriptas;
import java.io.OutputStream;

public interface MateriasInscriptasPdfGenerator {
    void generate(MateriasInscriptas materias, OutputStream out);
}
