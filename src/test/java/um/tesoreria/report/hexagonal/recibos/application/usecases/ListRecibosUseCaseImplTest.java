package um.tesoreria.report.hexagonal.recibos.application.usecases;

import org.junit.jupiter.api.Test;
import um.tesoreria.report.hexagonal.recibos.domain.model.Recibo;
import um.tesoreria.report.hexagonal.recibos.domain.ports.out.RecibosRepository;
import um.tesoreria.report.hexagonal.recibos.domain.ports.out.RecibosRepository.ChequeraRef;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListRecibosUseCaseImplTest {

    private static final BigDecimal PERSONA = new BigDecimal("23693840");

    private final RecibosRepository repository = mock(RecibosRepository.class);
    private final ListRecibosUseCaseImpl useCase = new ListRecibosUseCaseImpl(repository);

    private static Recibo recibo(long id, ChequeraRef chequera, String fecha, String cae) {
        return new Recibo(id, chequera.facultadId(), chequera.tipoChequeraId(), chequera.chequeraSerieId(), id * 10,
                1, "Arancel", 2026, 3, 1, id, new BigDecimal("1000.00"), fecha == null ? null : OffsetDateTime.parse(fecha), cae);
    }

    // protects=solo se consultan los recibos de las chequeras de la facultad pedida; fails_when=se piden o devuelven los de otra facultad; no_claim=no prueba el filtro por lectivo (lo hace el core)
    @Test
    void listRecibos_ignoresChequerasFromOtherFacultades() {
        ChequeraRef propia = new ChequeraRef(1, 2, 100L);
        ChequeraRef ajena = new ChequeraRef(7, 2, 200L);
        when(repository.findChequeras(PERSONA, 1, 37)).thenReturn(List.of(propia, ajena));
        when(repository.findRecibosByChequera(propia)).thenReturn(List.of(recibo(1, propia, "2026-03-01T00:00:00Z", "CAE1")));

        List<Recibo> result = useCase.listRecibos(1, 37, PERSONA, 1);

        assertThat(result).extracting(Recibo::facultadId).containsOnly(1);
        verify(repository, never()).findRecibosByChequera(ajena);
    }

    // protects=se juntan los recibos de todas las chequeras de la persona y salen del más reciente al más antiguo; fails_when=solo se lista una chequera o se pierde el orden; no_claim=no prueba paginación
    @Test
    void listRecibos_mergesAllChequerasNewestFirst() {
        ChequeraRef a = new ChequeraRef(1, 2, 100L);
        ChequeraRef b = new ChequeraRef(1, 3, 101L);
        when(repository.findChequeras(PERSONA, 1, 37)).thenReturn(List.of(a, b));
        when(repository.findRecibosByChequera(a)).thenReturn(List.of(
                recibo(1, a, "2026-03-01T00:00:00Z", "CAE1"), recibo(3, a, "2026-05-01T00:00:00Z", "CAE3")));
        when(repository.findRecibosByChequera(b)).thenReturn(List.of(recibo(2, b, "2026-04-01T00:00:00Z", "CAE2")));

        assertThat(useCase.listRecibos(1, 37, PERSONA, 1)).extracting(Recibo::facturacionElectronicaId)
                .containsExactly(3L, 2L, 1L);
    }

    // protects=misma fecha se desempata por id descendente y la fecha nula va al final; fails_when=orden inestable o NPE con fecha nula; no_claim=no define fecha nula como válida en el core
    @Test
    void listRecibos_breaksTiesByIdAndPutsNullDatesLast() {
        ChequeraRef a = new ChequeraRef(1, 2, 100L);
        when(repository.findChequeras(PERSONA, 1, 37)).thenReturn(List.of(a));
        when(repository.findRecibosByChequera(a)).thenReturn(List.of(
                recibo(1, a, null, "CAE1"), recibo(2, a, "2026-03-01T00:00:00Z", "CAE2"),
                recibo(3, a, "2026-03-01T00:00:00Z", "CAE3")));

        assertThat(useCase.listRecibos(1, 37, PERSONA, 1)).extracting(Recibo::facturacionElectronicaId)
                .containsExactly(3L, 2L, 1L);
    }

    // protects=solo se listan recibos con CAE; fails_when=aparece un recibo pendiente de facturar (CAE nulo o vacío); no_claim=no decide qué pasa con recibos pendientes en el core
    @Test
    void listRecibos_skipsRecibosWithoutCae() {
        ChequeraRef a = new ChequeraRef(1, 2, 100L);
        when(repository.findChequeras(PERSONA, 1, 37)).thenReturn(List.of(a));
        when(repository.findRecibosByChequera(a)).thenReturn(List.of(
                recibo(1, a, "2026-03-01T00:00:00Z", null), recibo(2, a, "2026-03-02T00:00:00Z", " "),
                recibo(3, a, "2026-03-03T00:00:00Z", "CAE3")));

        assertThat(useCase.listRecibos(1, 37, PERSONA, 1)).extracting(Recibo::facturacionElectronicaId)
                .containsExactly(3L);
    }

    // protects=persona sin chequeras o chequera sin recibos devuelve [] sin romper; fails_when=excepción o null; no_claim=no prueba el caso de error del core
    @Test
    void listRecibos_returnsEmptyWhenThereIsNothing() {
        ChequeraRef a = new ChequeraRef(1, 2, 100L);
        when(repository.findChequeras(PERSONA, 1, 37)).thenReturn(List.of());
        assertThat(useCase.listRecibos(1, 37, PERSONA, 1)).isEmpty();

        when(repository.findChequeras(PERSONA, 1, 38)).thenReturn(List.of(a));
        when(repository.findRecibosByChequera(a)).thenReturn(List.of());
        assertThat(useCase.listRecibos(1, 38, PERSONA, 1)).isEmpty();
    }

    // protects=un fallo del core en una chequera se propaga y no se devuelve una lista parcial; fails_when=se traga la excepción; no_claim=no define el código HTTP final
    @Test
    void listRecibos_propagatesCoreFailureInsteadOfReturningPartialList() {
        ChequeraRef a = new ChequeraRef(1, 2, 100L);
        ChequeraRef b = new ChequeraRef(1, 3, 101L);
        when(repository.findChequeras(PERSONA, 1, 37)).thenReturn(List.of(a, b));
        when(repository.findRecibosByChequera(a)).thenReturn(List.of(recibo(1, a, "2026-03-01T00:00:00Z", "CAE1")));
        when(repository.findRecibosByChequera(b)).thenThrow(new IllegalStateException("core caído"));

        assertThatThrownBy(() -> useCase.listRecibos(1, 37, PERSONA, 1)).isInstanceOf(IllegalStateException.class);
    }
}