package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contrato de {@code GET /api/tesoreria/core/chequera/estado/...} de {@code core-service}
 * (mismos nombres de campo que {@code EstadoChequeraResponse}).
 */
public record EstadoChequeraDto(
        Integer facultadId,
        String facultadNombre,
        Integer tipoChequeraId,
        String tipoChequeraNombre,
        Long chequeraSerieId,
        BigDecimal personaId,
        String personaApellido,
        String personaNombre,
        String arancelTipoDescripcion,
        String lectivoNombre,
        BigDecimal becaPorcentaje,
        String tipoImpresionNombre,
        Integer alternativaId,
        List<ProductoEstadoDto> productos,
        List<DebitoEstadoDto> debitos) {
}