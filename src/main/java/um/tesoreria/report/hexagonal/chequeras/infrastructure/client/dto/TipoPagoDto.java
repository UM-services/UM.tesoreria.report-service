package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoPagoDto {
    private Integer tipoPagoId;
    private String nombre;
}
