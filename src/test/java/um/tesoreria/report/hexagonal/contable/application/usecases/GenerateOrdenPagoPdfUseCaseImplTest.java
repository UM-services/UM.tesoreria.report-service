package um.tesoreria.report.hexagonal.contable.application.usecases;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import um.tesoreria.report.hexagonal.contable.domain.model.OrdenPago;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.OrdenPagoPdfGenerator;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.OrdenPagoRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateOrdenPagoPdfUseCaseImplTest {

    private final OrdenPagoRepository repository = mock(OrdenPagoRepository.class);
    private final OrdenPagoPdfGenerator generator = mock(OrdenPagoPdfGenerator.class);
    private final GenerateOrdenPagoPdfUseCaseImpl useCase = new GenerateOrdenPagoPdfUseCaseImpl(repository, generator);

    private static OrdenPago orden() {
        return new OrdenPago(10L, 15, 3684L, null, null, null, null, null, null,
                List.of(), null, List.of(), null, List.of(), null, null);
    }

    // protects=el PDF se arma con la orden que informa el core, sin tocarla; fails_when=se genera con otra orden o se descarta el resultado; no_claim=no prueba el contenido del PDF
    @Test
    void generate_buildsThePdfFromTheOrderReturnedByTheCore() {
        OrdenPago orden = orden();
        byte[] pdf = "%PDF".getBytes(StandardCharsets.US_ASCII);
        when(repository.findById(10L)).thenReturn(orden);
        when(generator.generate(orden)).thenReturn(pdf);

        assertThat(useCase.generate(10L)).isSameAs(pdf);
    }

    // protects=si el core rechaza el comprobante no se genera ningún archivo y el rechazo llega a quien llamó; fails_when=se genera un PDF vacío o se traga el error del core; no_claim=no prueba cómo se traduce el error a HTTP
    @Test
    void generate_propagatesTheCoreRejectionWithoutBuildingAFile() {
        Request request = Request.create(Request.HttpMethod.GET, "/x", Map.of(), null, StandardCharsets.UTF_8, null);
        when(repository.findById(5L)).thenThrow(new FeignException.Conflict("no es una orden", request, null, Map.of()));

        assertThatThrownBy(() -> useCase.generate(5L)).isInstanceOf(FeignException.Conflict.class);
        verify(generator, never()).generate(org.mockito.ArgumentMatchers.any());
    }
}