package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Legajo {
    private Long legajoId;
    private BigDecimal personaId;
    private Integer documentoId;
    private Integer facultadId;
    private Long numeroLegajo;
    private OffsetDateTime fecha;
    private Integer lectivoId;
    private Integer planId;
    private Integer carreraId;
    private Byte tieneCarrera;
    private Integer geograficaId;
    private String contrasenha;
    private Byte intercambio;
    private Carrera carrera;
}
