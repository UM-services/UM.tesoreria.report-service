package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan {
    private Long uniqueId;
    private Integer facultadId;
    private Integer planId;
    private String nombre;
    private OffsetDateTime fecha;
    private Byte publicar;
}
