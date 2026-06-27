package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto;

import lombok.*;
import um.tesoreria.report.util.Jsonifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuotaPeriodoDto {
    private Integer mes;
    private Integer anho;
    private Long cantidad;

    public String jsonify() {
        return Jsonifier.builder(this).build();
    }
}
