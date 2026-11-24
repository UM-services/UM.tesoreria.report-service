package um.tesoreria.report.hexagonal.chequeras.domain.ports.in;

public interface GeneratePlanillaBorradorVerticalUseCase {

    /**
     * Genera la planilla vertical (una fila por cuota) de borradores abiertos de una facultad,
     * lectivo y sede.
     *
     * @return ruta del XLSX generado
     */
    String generatePlanillaBorrador(Integer facultadId, Integer lectivoId, Integer geograficaId);
}
