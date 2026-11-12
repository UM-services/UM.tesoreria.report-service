package um.tesoreria.report.hexagonal.contable.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.contable.domain.model.OrdenPago;
import um.tesoreria.report.hexagonal.contable.domain.ports.in.GenerateOrdenPagoPdfUseCase;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.OrdenPagoPdfGenerator;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.OrdenPagoRepository;

/** Pide la orden al core y la entrega al generador de PDF. Un rechazo del core se propaga sin generar archivo. */
@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateOrdenPagoPdfUseCaseImpl implements GenerateOrdenPagoPdfUseCase {

    private final OrdenPagoRepository repository;
    private final OrdenPagoPdfGenerator generator;

    @Override
    public byte[] generate(Long proveedorMovimientoId) {
        log.debug("Processing GenerateOrdenPagoPdfUseCaseImpl.generate");
        OrdenPago orden = repository.findById(proveedorMovimientoId);
        return generator.generate(orden);
    }
}