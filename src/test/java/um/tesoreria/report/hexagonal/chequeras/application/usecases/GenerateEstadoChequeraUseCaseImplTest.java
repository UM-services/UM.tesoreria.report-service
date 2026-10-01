package um.tesoreria.report.hexagonal.chequeras.application.usecases;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openpdf.text.pdf.PdfReader;
import org.springframework.core.env.Environment;
import um.tesoreria.report.hexagonal.chequeras.EstadoChequeraFixture;
import um.tesoreria.report.hexagonal.chequeras.domain.model.EstadoChequera;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.ChequeraRepository;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.EstadoChequeraPdfGenerator;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.pdf.OpenPdfEstadoChequeraPdfGenerator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateEstadoChequeraUseCaseImplTest {

    private final Environment environment = mock(Environment.class);
    private final ChequeraRepository chequeraRepository = mock(ChequeraRepository.class);

    @Test
    void generateEstadoChequera_writesAValidTwoPagePdfNamedAfterTheChequera(@TempDir Path tempDir) throws IOException {
        when(environment.getProperty("path.reports")).thenReturn(tempDir + File.separator);
        when(chequeraRepository.findEstadoChequera(1, 2, 12345L, 1)).thenReturn(EstadoChequeraFixture.referencia());
        GenerateEstadoChequeraUseCaseImpl useCase = new GenerateEstadoChequeraUseCaseImpl(environment,
                chequeraRepository, new OpenPdfEstadoChequeraPdfGenerator());

        String filename = useCase.generateEstadoChequera(1, 2, 12345L, 1);

        // Sin separador de miles en el número de chequera (no "12,345").
        assertThat(filename).isEqualTo(tempDir + File.separator + "estado-chequera.1.2.12345.1.pdf");
        byte[] bytes = Files.readAllBytes(Path.of(filename));
        assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
        assertThat(new PdfReader(bytes).getNumberOfPages()).isEqualTo(2);
        verify(chequeraRepository).findEstadoChequera(1, 2, 12345L, 1);
    }

    @Test
    void generateEstadoChequera_removesThePartialFileAndFailsWhenTheGeneratorFails(@TempDir Path tempDir) {
        when(environment.getProperty("path.reports")).thenReturn(tempDir + File.separator);
        when(chequeraRepository.findEstadoChequera(1, 2, 12345L, 1)).thenReturn(EstadoChequeraFixture.referencia());
        EstadoChequeraPdfGenerator failing = mock(EstadoChequeraPdfGenerator.class);
        doThrow(new IllegalArgumentException("boom")).when(failing).generate(any(EstadoChequera.class), any(OutputStream.class));
        GenerateEstadoChequeraUseCaseImpl useCase = new GenerateEstadoChequeraUseCaseImpl(environment,
                chequeraRepository, failing);

        assertThatThrownBy(() -> useCase.generateEstadoChequera(1, 2, 12345L, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("1/2/12345")
                .hasCauseInstanceOf(IllegalArgumentException.class);

        assertThat(tempDir.resolve("estado-chequera.1.2.12345.1.pdf")).doesNotExist();
    }
}