package um.tesoreria.report.hexagonal.chequeras.domain.ports.in;

public interface GenerateEstadoChequeraUseCase {

    /**
     * Genera el PDF "Estado de Chequera" y devuelve la ruta del archivo generado.
     */
    String generateEstadoChequera(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                  Integer alternativaId);
}