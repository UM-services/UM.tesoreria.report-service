package um.tesoreria.report.hexagonal.contable.infrastructure.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CuentaPlanDto {
    private BigDecimal numeroCuenta;
    private String nombre;
    private Integer grado;
    private Byte integradora;
    private Byte visible;
    private Integer geograficaId;
}