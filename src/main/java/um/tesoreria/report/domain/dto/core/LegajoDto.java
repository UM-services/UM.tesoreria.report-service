package um.tesoreria.report.domain.dto.core;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegajoDto {
    private Long legajoId;
    private BigDecimal personaId;
    private Integer documentoId;
    private Integer facultadId;
    private Long numeroLegajo;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fecha;
    
    private Integer lectivoId;
    private Integer planId;
    private Integer carreraId;
    private Byte tieneCarrera;
    private Integer geograficaId;
    private String contrasenha;
    private Byte intercambio;
    private CarreraDto carrera;
}
