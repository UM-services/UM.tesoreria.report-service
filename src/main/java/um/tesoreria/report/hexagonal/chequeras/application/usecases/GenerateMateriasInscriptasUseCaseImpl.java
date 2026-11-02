package um.tesoreria.report.hexagonal.chequeras.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.MateriasInscriptas;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GenerateMateriasInscriptasUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.ChequeraRepository;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.MateriasInscriptasRepository;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.MateriasInscriptasPdfGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class GenerateMateriasInscriptasUseCaseImpl implements GenerateMateriasInscriptasUseCase {
    private final Environment environment;
    private final ChequeraRepository chequeraRepository;
    private final MateriasInscriptasRepository materiasRepository;
    private final MateriasInscriptasPdfGenerator pdfGenerator;

    @Override
    public String generateMateriasInscriptas(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                             Integer alternativaId) {
        var encabezado = chequeraRepository.findEstadoChequera(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
        var materias = materiasRepository.findMaterias(facultadId, tipoChequeraId, chequeraSerieId);
        // Un archivo por solicitud evita que descargas simultáneas sobrescriban el mismo PDF.
        Path file = null;
        try {
            file = Files.createTempFile(Path.of(environment.getRequiredProperty("path.reports")),
                    "materias-inscriptas-", ".pdf");
            try (var out = Files.newOutputStream(file)) {
                pdfGenerator.generate(new MateriasInscriptas(encabezado, materias), out);
            }
            return file.toString();
        } catch (IOException | RuntimeException e) {
            if (file != null) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException cleanup) {
                    e.addSuppressed(cleanup);
                }
            }
            throw new IllegalStateException("No se pudo generar el PDF de materias inscriptas", e);
        }
    }
}
