package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;

/**
 * Tipo de arancel disponible para el borrador (descripción para la planilla).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorradorArancelTipo {

    private Integer arancelTipoId;
    private String nombre;
}
