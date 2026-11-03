package um.tesoreria.report.hexagonal.contable.infrastructure.client.adapter.adapter;

import org.junit.jupiter.api.Test;
import um.tesoreria.report.hexagonal.contable.domain.model.CuentaPlan;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.CuentaClient;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.SedeClient;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.adapter.FeignPlanCuentasRepositoryAdapter;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.CuentaPlanDto;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.SedeDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeignPlanCuentasRepositoryAdapterTest {

    private final CuentaClient cuentaClient = mock(CuentaClient.class);
    private final SedeClient sedeClient = mock(SedeClient.class);
    private final FeignPlanCuentasRepositoryAdapter adapter =
            new FeignPlanCuentasRepositoryAdapter(cuentaClient, sedeClient);

    // protects=cada campo del core llega a la cuenta del plan sin cruzarse; fails_when=se pierde grado, integradora, visible o sede; no_claim=no prueba la llamada HTTP real al core
    @Test
    void findAllCuentas_mapsTheFieldsFromTheCore() {
        CuentaPlanDto dto = new CuentaPlanDto();
        dto.setNumeroCuenta(new BigDecimal("10101010000"));
        dto.setNombre("Caja");
        dto.setGrado(3);
        dto.setIntegradora((byte) 0);
        dto.setVisible((byte) 1);
        dto.setGeograficaId(2);
        when(cuentaClient.findAll()).thenReturn(List.of(dto));

        assertThat(adapter.findAllCuentas()).containsExactly(
                new CuentaPlan(new BigDecimal("10101010000"), "Caja", 3, (byte) 0, (byte) 1, 2));
    }

    // protects=una respuesta nula del core es un plan vacío y no un error; fails_when=NullPointerException con respuesta nula; no_claim=no prueba el comportamiento si el core responde con error HTTP
    @Test
    void findAllCuentas_treatsANullResponseAsEmpty() {
        when(cuentaClient.findAll()).thenReturn(null);

        assertThat(adapter.findAllCuentas()).isEmpty();
    }

    // protects=los nombres de sede se indexan por id y se descartan las filas incompletas; fails_when=se cruzan ids y nombres o una sede sin nombre rompe el listado; no_claim=no prueba sedes repetidas (el core las informa una vez)
    @Test
    void findNombresSedes_indexesByIdAndSkipsIncompleteRows() {
        SedeDto mendoza = new SedeDto();
        mendoza.setGeograficaId(1);
        mendoza.setNombre("Mendoza");
        SedeDto sinNombre = new SedeDto();
        sinNombre.setGeograficaId(2);
        sinNombre.setNombre(null);
        when(sedeClient.findAll()).thenReturn(List.of(mendoza, sinNombre));

        assertThat(adapter.findNombresSedes()).isEqualTo(Map.of(1, "Mendoza"));
    }
}