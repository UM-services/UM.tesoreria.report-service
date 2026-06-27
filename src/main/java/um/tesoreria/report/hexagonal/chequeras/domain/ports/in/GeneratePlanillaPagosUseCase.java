package um.tesoreria.report.hexagonal.chequeras.domain.ports.in;

public interface GeneratePlanillaPagosUseCase {
    String generatePlanillaPagos(Integer facultadId, Integer tipoChequeraId, Integer lectivoId);
}
