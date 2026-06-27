package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    private Integer productoId;
    private String nombre;
}
