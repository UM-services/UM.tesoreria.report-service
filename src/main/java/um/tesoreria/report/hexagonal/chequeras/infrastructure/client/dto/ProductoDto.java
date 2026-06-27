package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {
    private Integer productoId;
    private String nombre;
}
