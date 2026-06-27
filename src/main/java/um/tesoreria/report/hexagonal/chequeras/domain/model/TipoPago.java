package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoPago {
    private Integer tipoPagoId;
    private String nombre;
}
