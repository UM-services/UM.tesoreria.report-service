package um.tesoreria.report.hexagonal.contable.application.usecases;

import org.junit.jupiter.api.Test;
import um.tesoreria.report.hexagonal.contable.domain.model.Mayor;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorSeleccion;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.MayorPdfGenerator;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.MayorRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateMayorPdfUseCaseImplTest {

    private final MayorRepository repository = mock(MayorRepository.class);
    private final MayorPdfGenerator generator = mock(MayorPdfGenerator.class);
    private final GenerateMayorPdfUseCaseImpl useCase = new GenerateMayorPdfUseCaseImpl(repository, generator);

    private static MayorSeleccion seleccion() {
        return new MayorSeleccion(OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 4, 30, 0, 0, 0, 0, ZoneOffset.UTC),
                List.of(new BigDecimal("10101010001")), null, null);
    }

    // protects=el PDF se arma con el mayor que devuelve el core para la misma selección, sin tocarlo; fails_when=se consulta otra selección o se genera con otros datos; no_claim=no prueba el contenido del PDF
    @Test
    void generaElPdfConElMayorDelCore() {
        Mayor mayor = new Mayor(null, null, List.of());
        when(repository.consultar(seleccion())).thenReturn(mayor);
        when(generator.generate(mayor)).thenReturn(new byte[] {1, 2, 3});

        assertThat(useCase.generate(seleccion())).containsExactly(1, 2, 3);
        verify(repository).consultar(seleccion());
    }

    // protects=si el core rechaza el pedido la excepción se propaga y no se genera ningún PDF; fails_when=se traga el error o se genera un archivo vacío; no_claim=no prueba el estado HTTP (lo cubre el advice)
    @Test
    void siElCoreRechazaNoGeneraPdf() {
        when(repository.consultar(seleccion())).thenThrow(new IllegalStateException("rechazo"));

        assertThatThrownBy(() -> useCase.generate(seleccion())).isInstanceOf(IllegalStateException.class);
        verify(generator, never()).generate(org.mockito.ArgumentMatchers.any());
    }
}