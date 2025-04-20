package um.tesoreria.report.domain.dto;

import lombok.Data;

@Data
public class ArancelTipoDto {
    private Integer arancelTipoId;
    private String descripcion = "";
    private Byte medioArancel = 0;
    private Integer arancelTipoIdCompleto;
}
