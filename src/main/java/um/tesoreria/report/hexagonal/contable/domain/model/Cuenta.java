package um.tesoreria.report.hexagonal.contable.domain.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cuenta {
    private BigDecimal numeroCuenta;
    private String nombre;
}
