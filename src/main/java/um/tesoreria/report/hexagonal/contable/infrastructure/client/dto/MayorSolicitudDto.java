package um.tesoreria.report.hexagonal.contable.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MayorSolicitudDto {
    private OffsetDateTime desde;
    private OffsetDateTime hasta;
    private List<BigDecimal> cuentas;
    private BigDecimal cuentaDesde;
    private BigDecimal cuentaHasta;
}