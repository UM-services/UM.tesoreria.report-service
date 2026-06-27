package um.tesoreria.report.hexagonal.chequeras.domain.ports.in;

public interface GeneratePlanillaDetalleVerticalUseCase {
    String generatePlanillaDetalle(Integer facultadId, Integer lectivoId, Integer geograficaId);
}
