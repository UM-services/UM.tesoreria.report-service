package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.adapter;

import org.junit.jupiter.api.Test;
import um.tesoreria.report.hexagonal.chequeras.MateriasInscriptasFixture;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.MateriasInscriptasClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper.ChequerasMapper;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeignMateriasInscriptasRepositoryAdapterTest {
    private final MateriasInscriptasClient client = mock(MateriasInscriptasClient.class);
    private final FeignMateriasInscriptasRepositoryAdapter adapter =
            new FeignMateriasInscriptasRepositoryAdapter(client, new ChequerasMapper());

    // protects: las tres claves consultan y mapean el endpoint separado; fails_when: claves cambiadas o lista perdida; no_claim: Feign simulado, no verifica HTTP.
    @Test
    void mapsTheSeparateResponse() throws IOException {
        when(client.findMaterias(1, 2, 12345L)).thenReturn(MateriasInscriptasFixture.dtos());
        assertThat(adapter.findMaterias(1, 2, 12345L)).isEqualTo(MateriasInscriptasFixture.referencia().materias());
        verify(client).findMaterias(1, 2, 12345L);
    }

    // protects: cuerpo HTTP ausente no significa no grado; fails_when: null se convierte en PDF de lista vacía; no_claim: no prueba deserialización HTTP de un 204.
    @Test
    void rejectsAMissingResponseBody() {
        when(client.findMaterias(1, 2, 12345L)).thenReturn(null);
        assertThatThrownBy(() -> adapter.findMaterias(1, 2, 12345L))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("cuerpo vacío");
    }
}
