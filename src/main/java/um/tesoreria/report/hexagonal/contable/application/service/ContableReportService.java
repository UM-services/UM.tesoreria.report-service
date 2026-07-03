package um.tesoreria.report.hexagonal.contable.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.report.hexagonal.contable.domain.ports.in.GenerateLectivoTotalImputacionReportUseCase;

@Service
@RequiredArgsConstructor
public class ContableReportService {

    private final GenerateLectivoTotalImputacionReportUseCase generateLectivoTotalImputacionReportUseCase;

    public String generateReport(Integer lectivoId) {
        return generateLectivoTotalImputacionReportUseCase.generateReport(lectivoId);
    }
}
