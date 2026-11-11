package um.tesoreria.report.hexagonal.contable.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.contable.domain.model.Mayor;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorSeleccion;
import um.tesoreria.report.hexagonal.contable.domain.ports.in.GenerateMayorPdfUseCase;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.MayorPdfGenerator;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.MayorRepository;

/** Pide el mayor al core y lo entrega al generador de PDF. Un rechazo del core se propaga sin generar archivo. */
@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateMayorPdfUseCaseImpl implements GenerateMayorPdfUseCase {

    private final MayorRepository repository;
    private final MayorPdfGenerator generator;

    @Override
    public byte[] generate(MayorSeleccion seleccion) {
        log.debug("Processing GenerateMayorPdfUseCaseImpl.generate");
        Mayor mayor = repository.consultar(seleccion);
        return generator.generate(mayor);
    }
}