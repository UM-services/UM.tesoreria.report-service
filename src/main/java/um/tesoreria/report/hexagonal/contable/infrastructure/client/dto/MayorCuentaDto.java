package um.tesoreria.report.hexagonal.contable.infrastructure.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MayorCuentaDto {
    private BigDecimal numeroCuenta;
    private String nombre;
    private MayorSaldoInicialDto saldoInicial;
    private List<MayorMovimientoDto> movimientos;
    private BigDecimal totalDebe;
    private BigDecimal totalHaber;
    private BigDecimal saldoFinal;
}