package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;
import um.tesoreria.report.util.Jsonifier;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuotaPeriodo {
    private Integer mes;
    private Integer anho;
    private Long cantidad;

    public String jsonify() {
        return Jsonifier.builder(this).build();
    }
}
