package um.tesoreria.report.domain.dto.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
