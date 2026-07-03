package um.tesoreria.report.hexagonal.contable.infrastructure.client.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CuentaDto {
    private BigDecimal numeroCuenta;
    private String nombre = "";
}
