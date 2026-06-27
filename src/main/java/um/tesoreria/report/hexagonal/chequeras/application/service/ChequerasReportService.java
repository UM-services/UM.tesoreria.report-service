package um.tesoreria.report.hexagonal.chequeras.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaDetalleUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaPagosUseCase;

@Service
@RequiredArgsConstructor
public class ChequerasReportService {

    private final GeneratePlanillaDetalleUseCase generatePlanillaDetalleUseCase;
    private final GeneratePlanillaPagosUseCase generatePlanillaPagosUseCase;

    public String generatePlanillaDetalle(Integer facultadId, Integer lectivoId, Integer geograficaId) {
        return generatePlanillaDetalleUseCase.generatePlanillaDetalle(facultadId, lectivoId, geograficaId);
    }

    public String generatePlanillaPagos(Integer facultadId, Integer tipoChequeraId, Integer lectivoId) {
        return generatePlanillaPagosUseCase.generatePlanillaPagos(facultadId, tipoChequeraId, lectivoId);
    }
}
