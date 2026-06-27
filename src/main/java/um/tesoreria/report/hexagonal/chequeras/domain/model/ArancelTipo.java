package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArancelTipo {
    private Integer arancelTipoId;
    private String descripcion;
    private Byte medioArancel;
    private Integer arancelTipoIdCompleto;
}
