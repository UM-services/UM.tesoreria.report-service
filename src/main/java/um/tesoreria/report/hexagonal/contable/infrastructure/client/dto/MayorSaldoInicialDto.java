package um.tesoreria.report.hexagonal.contable.infrastructure.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MayorSaldoInicialDto {
    private BigDecimal debe;
    private BigDecimal haber;
    private BigDecimal saldo;
}