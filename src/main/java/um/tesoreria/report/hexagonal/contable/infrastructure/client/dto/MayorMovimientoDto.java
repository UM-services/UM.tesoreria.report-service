package um.tesoreria.report.hexagonal.contable.infrastructure.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class MayorMovimientoDto {
    private OffsetDateTime fechaContable;
    private Integer ordenContable;
    private Integer ejercicioId;
    private String ejercicioNombre;
    private String comprobante;
    private String proveedor;
    private String concepto;
    private String ordenPago;
    private String beneficiario;
    private BigDecimal debe;
    private BigDecimal haber;
    private BigDecimal saldo;
    private String ordenPagoValor;
    private String valor;
    private Long numeroValor;
    private BigDecimal importeValor;
}