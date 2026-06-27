package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lectivo {
    private Integer lectivoId;
    private String nombre;
    private OffsetDateTime fechaInicio;
    private OffsetDateTime fechaFinal;
}
