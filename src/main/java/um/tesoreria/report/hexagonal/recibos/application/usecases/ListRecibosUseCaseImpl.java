package um.tesoreria.report.hexagonal.recibos.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.recibos.domain.model.Recibo;
import um.tesoreria.report.hexagonal.recibos.domain.ports.in.ListRecibosUseCase;
import um.tesoreria.report.hexagonal.recibos.domain.ports.out.RecibosRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListRecibosUseCaseImpl implements ListRecibosUseCase {

    private static final Comparator<Recibo> MAS_RECIENTE_PRIMERO = Comparator
            .comparing(Recibo::fechaRecibo, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(Recibo::facturacionElectronicaId, Comparator.nullsLast(Comparator.reverseOrder()));

    private final RecibosRepository recibosRepository;

    @Override
    public List<Recibo> listRecibos(Integer facultadId, Integer lectivoId, BigDecimal personaId, Integer documentoId) {
        log.debug("Processing ListRecibosUseCaseImpl.listRecibos");
        // Un fallo del core en cualquier chequera se propaga: no se devuelve una lista parcial como completa.
        return recibosRepository.findChequeras(personaId, documentoId, lectivoId).stream()
                .filter(chequera -> Objects.equals(chequera.facultadId(), facultadId))
                .distinct()
                .flatMap(chequera -> recibosRepository.findRecibosByChequera(chequera).stream())
                .filter(ListRecibosUseCaseImpl::estaEmitido)
                .sorted(MAS_RECIENTE_PRIMERO)
                .toList();
    }

    /** Solo los recibos con CAE tienen comprobante válido. */
    private static boolean estaEmitido(Recibo recibo) {
        return recibo.cae() != null && !recibo.cae().isBlank();
    }
}