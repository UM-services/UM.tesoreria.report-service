package um.tesoreria.report.hexagonal.recibos.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacturacionElectronicaDto {
    private Long facturacionElectronicaId;
    private Long chequeraPagoId;
    private Integer comprobanteId;
    private Long numeroComprobante;
    private BigDecimal importe;
    private String cae;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fechaRecibo;
}