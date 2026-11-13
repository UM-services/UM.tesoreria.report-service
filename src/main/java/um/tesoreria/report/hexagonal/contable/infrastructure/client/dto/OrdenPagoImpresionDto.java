package um.tesoreria.report.hexagonal.contable.infrastructure.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/** Respuesta de {@code GET /proveedorMovimiento/{id}/impresion} del core. */
@Data
public class OrdenPagoImpresionDto {

    private Long proveedorMovimientoId;
    private Integer prefijo;
    private Long numeroComprobante;
    private OffsetDateTime fechaComprobante;
    private OffsetDateTime fechaAnulacion;
    private String concepto;
    private String razonSocial;
    private String domicilio;
    private String cuit;
    private List<Comprobante> comprobantes;
    private BigDecimal totalComprobantes;
    private List<Valor> valores;
    private BigDecimal totalValores;
    private List<Traza> traza;
    private BigDecimal totalDebe;
    private BigDecimal totalHaber;

    @Data
    public static class Comprobante {
        private String comprobanteDescripcion;
        private Integer prefijo;
        private Long numeroComprobante;
        private OffsetDateTime fechaComprobante;
        private BigDecimal importe;
        private BigDecimal importeAplicado;
    }

    @Data
    public static class Valor {
        private String concepto;
        private Long numero;
        private OffsetDateTime fechaEmision;
        private BigDecimal importe;
        private String nombreTitular;
    }

    @Data
    public static class Traza {
        private BigDecimal numeroCuenta;
        private String nombreCuenta;
        private BigDecimal debe;
        private BigDecimal haber;
    }
}