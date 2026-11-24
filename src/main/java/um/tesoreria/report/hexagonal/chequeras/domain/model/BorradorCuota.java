package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Cuota de un borrador: importe del diseño ({@code importeNOriginal}) e importe a emitir
 * ({@code importeNBorrador}).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorradorCuota {

    private Long borradorCuotaId;
    private Integer productoId;
    private Integer alternativaId;
    private Integer cuotaId;
    private Integer mes;
    private Integer anio;
    private OffsetDateTime vencimiento1;
    private OffsetDateTime vencimiento2;
    private OffsetDateTime vencimiento3;
    private BigDecimal importe1Original;
    private BigDecimal importe2Original;
    private BigDecimal importe3Original;
    private BigDecimal importe1Borrador;
    private BigDecimal importe2Borrador;
    private BigDecimal importe3Borrador;
    private Byte pagado;
    private Byte baja;
    private Byte manual;
    private Byte compensada;
    private Byte bloqueado;
}
