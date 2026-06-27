package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDto {
    private Long uniqueId;
    private Integer facultadId;
    private Integer planId;
    private String nombre;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fecha;
    
    private Byte publicar;
}
