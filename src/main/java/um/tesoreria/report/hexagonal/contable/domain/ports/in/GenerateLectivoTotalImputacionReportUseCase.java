package um.tesoreria.report.hexagonal.contable.domain.ports.in;

public interface GenerateLectivoTotalImputacionReportUseCase {
    String generateReport(Integer lectivoId);
}
