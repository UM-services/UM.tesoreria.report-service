package um.tesoreria.report.hexagonal.contable.application.usecases;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import um.tesoreria.report.hexagonal.contable.domain.model.Compra;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.CompraPdfGenerator;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.CompraRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateCompraPdfUseCaseImplTest {

    @Mock
    private CompraRepository repository;
    @Mock
    private CompraPdfGenerator generator;

    // protects=el PDF se genera con el comprobante que informa el core; fails_when=se genera con otro objeto o no se consulta el core; no_claim=no prueba el contenido del PDF
    @Test
    void generate_passesCoreCompraToGenerator() {
        Compra compra = new Compra(20L, "Factura B", "0007-00118482", null, null, null, null, null, null,
                List.of(), null, null, List.of(), null, null);
        when(repository.findById(20L)).thenReturn(compra);
        when(generator.generate(compra)).thenReturn(new byte[] {7});

        assertThat(new GenerateCompraPdfUseCaseImpl(repository, generator).generate(20L)).containsExactly(7);
    }
}