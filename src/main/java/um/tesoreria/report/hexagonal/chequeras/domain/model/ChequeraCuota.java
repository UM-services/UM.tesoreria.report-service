package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeraCuota {
    private Long chequeraCuotaId;
    private Long chequeraId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private Integer productoId;
    private Integer alternativaId;
    private Integer cuotaId;
    private Integer mes;
    private Integer anho;
    private Integer arancelTipoId;
    private OffsetDateTime vencimiento1;
    private BigDecimal importe1;
    private BigDecimal importe1Original;
    private OffsetDateTime vencimiento2;
    private BigDecimal importe2;
    private BigDecimal importe2Original;
    private OffsetDateTime vencimiento3;
    private BigDecimal importe3;
    private BigDecimal importe3Original;
    private String codigoBarras;
    private String i2Of5;
    private Byte pagado;
    private Byte baja;
    private Byte manual;
    private Byte compensada;
    private Integer tramoId;
    private Facultad facultad;
    private TipoChequera tipoChequera;
    private Producto producto;
    private ChequeraSerie chequeraSerie;
}
