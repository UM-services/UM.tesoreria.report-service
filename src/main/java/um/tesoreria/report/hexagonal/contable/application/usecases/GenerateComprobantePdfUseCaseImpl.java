package um.tesoreria.report.hexagonal.contable.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.contable.domain.ports.in.GenerateCompraPdfUseCase;
import um.tesoreria.report.hexagonal.contable.domain.ports.in.GenerateComprobantePdfUseCase;
import um.tesoreria.report.hexagonal.contable.domain.ports.in.GenerateOrdenPagoPdfUseCase;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.ComprobanteTipoRepository;

/** Consulta el tipo del comprobante en el core y delega en el formato de orden de pago o en el de compra. */
@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateComprobantePdfUseCaseImpl implements GenerateComprobantePdfUseCase {

    private final ComprobanteTipoRepository tipoRepository;
    private final GenerateOrdenPagoPdfUseCase ordenPago;
    private final GenerateCompraPdfUseCase compra;

    @Override
    public byte[] generate(Long proveedorMovimientoId) {
        log.debug("Processing GenerateComprobantePdfUseCaseImpl.generate");
        return switch (tipoRepository.findTipo(proveedorMovimientoId)) {
            case ORDEN_PAGO -> ordenPago.generate(proveedorMovimientoId);
            case FACTURA -> compra.generate(proveedorMovimientoId);
        };
    }
}