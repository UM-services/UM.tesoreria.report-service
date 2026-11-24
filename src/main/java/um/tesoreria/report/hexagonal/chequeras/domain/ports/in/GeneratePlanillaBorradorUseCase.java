package um.tesoreria.report.hexagonal.chequeras.domain.ports.in;

public interface GeneratePlanillaBorradorUseCase {

    /**
     * Genera la planilla horizontal de borradores abiertos de una facultad, lectivo y sede.
     *
     * @return ruta del XLSX generado
     */
    String generatePlanillaBorrador(Integer facultadId, Integer lectivoId, Integer geograficaId);
}
