package um.tesoreria.report.hexagonal.chequeras.application.usecases;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.Environment;
import um.tesoreria.report.hexagonal.chequeras.EstadoChequeraFixture;
import um.tesoreria.report.hexagonal.chequeras.MateriasInscriptasFixture;
import um.tesoreria.report.hexagonal.chequeras.domain.model.MateriasInscriptas;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.*;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.pdf.OpenPdfMateriasInscriptasPdfGenerator;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class GenerateMateriasInscriptasUseCaseImplTest {
    private final Environment environment = mock(Environment.class);
    private final ChequeraRepository chequeras = mock(ChequeraRepository.class);
    private final MateriasInscriptasRepository materias = mock(MateriasInscriptasRepository.class);

    private void setup(Path dir) throws IOException {
        when(environment.getRequiredProperty("path.reports")).thenReturn(dir.toString());
        when(chequeras.findEstadoChequera(1, 2, 12345L, 1)).thenReturn(EstadoChequeraFixture.referencia());
        when(materias.findMaterias(1, 2, 12345L)).thenReturn(MateriasInscriptasFixture.referencia().materias());
    }

    // protects: combina encabezado y endpoint separado, con archivos distintos por descarga; fails_when: falta una consulta o se sobrescribe otro PDF; no_claim: repositorios simulados, no E2E.
    @Test
    void generatesIndependentPdfs(@TempDir Path dir) throws IOException {
        setup(dir);
        var useCase = new GenerateMateriasInscriptasUseCaseImpl(environment, chequeras, materias,
                new OpenPdfMateriasInscriptasPdfGenerator());
        String first = useCase.generateMateriasInscriptas(1, 2, 12345L, 1);
        String second = useCase.generateMateriasInscriptas(1, 2, 12345L, 1);
        assertThat(first).isNotEqualTo(second);
        try (PdfReader pdf = new PdfReader(Files.readAllBytes(Path.of(first)))) {
            assertThat(new PdfTextExtractor(pdf).getTextFromPage(1)).contains("Materias inscriptas", "CALCULO II");
        }
        verify(chequeras, times(2)).findEstadoChequera(1, 2, 12345L, 1);
        verify(materias, times(2)).findMaterias(1, 2, 12345L);
    }

    // protects: PDF fallido no queda disponible; fails_when: archivo parcial persiste o error se oculta; no_claim: no prueba fallos de disco reales.
    @Test
    void removesPartialFiles(@TempDir Path dir) throws IOException {
        setup(dir);
        var generator = mock(MateriasInscriptasPdfGenerator.class);
        doThrow(new IllegalArgumentException("error sintético")).when(generator).generate(any(MateriasInscriptas.class), any(OutputStream.class));
        var useCase = new GenerateMateriasInscriptasUseCaseImpl(environment, chequeras, materias, generator);
        assertThatThrownBy(() -> useCase.generateMateriasInscriptas(1, 2, 12345L, 1))
                .isInstanceOf(IllegalStateException.class).hasCauseInstanceOf(IllegalArgumentException.class);
        try (var files = Files.list(dir)) { assertThat(files).isEmpty(); }
    }

    // protects: un fallo del endpoint no se presenta como no grado; fails_when: devuelve PDF vacío con éxito; no_claim: no verifica status final del controller.
    @Test
    void propagatesCoreFailuresWithoutCreatingAPdf(@TempDir Path dir) throws IOException {
        setup(dir);
        when(materias.findMaterias(1, 2, 12345L)).thenThrow(new IllegalStateException("core no disponible"));
        var generator = mock(MateriasInscriptasPdfGenerator.class);
        var useCase = new GenerateMateriasInscriptasUseCaseImpl(environment, chequeras, materias, generator);
        assertThatThrownBy(() -> useCase.generateMateriasInscriptas(1, 2, 12345L, 1))
                .isInstanceOf(IllegalStateException.class).hasMessage("core no disponible");
        verifyNoInteractions(generator);
        try (var files = Files.list(dir)) { assertThat(files).isEmpty(); }
    }
}
