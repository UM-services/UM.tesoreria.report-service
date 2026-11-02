package um.tesoreria.report.hexagonal.chequeras.domain.ports.in;

public interface GenerateMateriasInscriptasUseCase {
    String generateMateriasInscriptas(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                     Integer alternativaId);
}
