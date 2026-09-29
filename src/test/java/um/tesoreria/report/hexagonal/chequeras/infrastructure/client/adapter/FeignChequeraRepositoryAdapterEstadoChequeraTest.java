package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.adapter;

import org.junit.jupiter.api.Test;
import um.tesoreria.report.hexagonal.chequeras.domain.model.EstadoChequera;
import um.tesoreria.report.hexagonal.chequeras.domain.model.CuotaEstado;
import um.tesoreria.report.hexagonal.chequeras.domain.model.DebitoEstado;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.EstadoChequeraDto;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.CuotaEstadoDto;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.DebitoEstadoDto;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.ProductoEstadoDto;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.facade.ChequeraClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper.ChequerasMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeignChequeraRepositoryAdapterEstadoChequeraTest {

    private final ChequeraClient client = mock(ChequeraClient.class);
    private final FeignChequeraRepositoryAdapter adapter = new FeignChequeraRepositoryAdapter(client, new ChequerasMapper());

    @Test
    void findEstadoChequera_mapsTheCoreResponseToTheDomainModel() {
        CuotaEstadoDto cuota = new CuotaEstadoDto(1, 6, 2026, LocalDate.of(2026, 6, 10), new BigDecimal("182000.00"),
                0, LocalDate.of(2026, 6, 19), new BigDecimal("182000.00"), "D2026062301_30000000000");
        ProductoEstadoDto producto = new ProductoEstadoDto(1, "Matrícula", "Matrícula", 2,
                new BigDecimal("375000.00"), new BigDecimal("182000.00"), List.of(cuota));
        DebitoEstadoDto debito = new DebitoEstadoDto(5, new BigDecimal("364000.00"), LocalDate.of(2026, 7, 22),
                "1234567890123456789012", LocalDateTime.of(2026, 7, 3, 0, 0), true, "0011000100");
        EstadoChequeraDto dto = new EstadoChequeraDto(1, "Facultad de Ingeniería", 2, "Matrícula y Arancel", 12345L,
                new BigDecimal("12345678"), "MUÑOZ", "Ana Ejemplo", "Ciclo Completo", "Lectivo 2026 - 2027",
                new BigDecimal("0.15"), "Rapipago", 1, List.of(producto), List.of(debito));
        when(client.findEstadoChequera(1, 2, 12345L, 1, 2)).thenReturn(dto);

        EstadoChequera estado = adapter.findEstadoChequera(1, 2, 12345L, 1, 2);

        assertThat(estado.facultadNombre()).isEqualTo("Facultad de Ingeniería");
        assertThat(estado.personaId()).isEqualByComparingTo("12345678");
        assertThat(estado.becaPorcentaje()).isEqualByComparingTo("0.15");
        assertThat(estado.tipoImpresionNombre()).isEqualTo("Rapipago");
        assertThat(estado.productos()).hasSize(1);
        assertThat(estado.productos().get(0).nombre()).isEqualTo("Matrícula");
        assertThat(estado.productos().get(0).total()).isEqualByComparingTo("375000");
        assertThat(estado.productos().get(0).cuotas()).hasSize(1);
        CuotaEstado cuotaMapeada = estado.productos().get(0).cuotas().get(0);
        assertThat(cuotaMapeada.primerVencimiento()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(cuotaMapeada.fechaPago()).isEqualTo(LocalDate.of(2026, 6, 19));
        assertThat(cuotaMapeada.referenciaPago()).isEqualTo("D2026062301_30000000000");
        assertThat(cuotaMapeada.ordenPago()).isZero();
        assertThat(estado.debitos()).hasSize(1);
        DebitoEstado debitoMapeado = estado.debitos().get(0);
        assertThat(debitoMapeado.rechazado()).isTrue();
        assertThat(debitoMapeado.motivoRechazo()).isEqualTo("0011000100");
        assertThat(debitoMapeado.fechaEnvio()).isEqualTo(LocalDateTime.of(2026, 7, 3, 0, 0));
    }

    @Test
    void findEstadoChequera_treatsMissingListsAsEmpty() {
        EstadoChequeraDto dto = new EstadoChequeraDto(1, null, 2, null, 12345L, null, null, null, null, null, null,
                null, 1, null, null);
        when(client.findEstadoChequera(1, 2, 12345L, 1, 2)).thenReturn(dto);

        EstadoChequera estado = adapter.findEstadoChequera(1, 2, 12345L, 1, 2);

        assertThat(estado.productos()).isEmpty();
        assertThat(estado.debitos()).isEmpty();
    }
}