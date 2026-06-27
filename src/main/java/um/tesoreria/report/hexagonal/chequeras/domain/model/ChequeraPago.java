package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeraPago {
    private Long chequeraPagoId;
    private Long chequeraCuotaId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private Integer productoId;
    private Integer alternativaId;
    private Integer cuotaId;
    private Integer orden;
    private Integer mes;
    private Integer anho;
    private OffsetDateTime fecha;
    private OffsetDateTime acreditacion;
    private BigDecimal importe;
    private String path;
    private String archivo;
    private String observaciones;
    private Long archivoBancoId;
    private Long archivoBancoIdAcreditacion;
    private Integer verificador;
    private Integer tipoPagoId;
    private String idMercadoPago;
    private TipoPago tipoPago;
    private Producto producto;
    private ChequeraCuota chequeraCuota;
}
