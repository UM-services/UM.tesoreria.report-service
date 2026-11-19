package um.tesoreria.report.hexagonal.contable.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/** Respuesta de {@code GET /proveedorMovimiento/{id}/impresion-compra} del core. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompraImpresionDto {

    private Long proveedorMovimientoId;
    private String comprobanteDescripcion;
    private String numero;
    private OffsetDateTime fechaComprobante;
    private OffsetDateTime fechaAnulacion;
    private String concepto;
    private String razonSocial;
    private String cuit;
    private BigDecimal importe;
    private List<Detalle> detalle;
    private OffsetDateTime fechaContable;
    private Integer ordenContable;
    private List<Asiento> asiento;
    private BigDecimal totalDebe;
    private BigDecimal totalHaber;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Detalle {
        private String articulo;
        private String concepto;
        private BigDecimal cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal precioFinal;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Asiento {
        private BigDecimal numeroCuenta;
        private String nombreCuenta;
        private BigDecimal debe;
        private BigDecimal haber;
    }
}