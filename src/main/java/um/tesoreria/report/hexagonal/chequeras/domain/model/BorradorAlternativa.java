package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;

/**
 * Alternativa (producto + alternativa) del borrador con el título que se muestra en la planilla.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorradorAlternativa {

    private Integer alternativaId;
    private Integer productoId;
    private String titulo;
}
