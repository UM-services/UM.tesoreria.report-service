package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoEstadoDto(
        Integer productoId,
        String nombre,
        String tituloCuota,
        Integer totalCuotas,
        BigDecimal total,
        BigDecimal pagado,
        List<CuotaEstadoDto> cuotas) {
}