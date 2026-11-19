package um.tesoreria.report.hexagonal.contable.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.contable.domain.model.Compra;
import um.tesoreria.report.hexagonal.contable.domain.ports.in.GenerateCompraPdfUseCase;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.CompraPdfGenerator;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.CompraRepository;

/** Pide la factura o nota de crédito al core y la entrega al generador de PDF. */
@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateCompraPdfUseCaseImpl implements GenerateCompraPdfUseCase {

    private final CompraRepository repository;
    private final CompraPdfGenerator generator;

    @Override
    public byte[] generate(Long proveedorMovimientoId) {
        log.debug("Processing GenerateCompraPdfUseCaseImpl.generate");
        Compra compra = repository.findById(proveedorMovimientoId);
        return generator.generate(compra);
    }
}