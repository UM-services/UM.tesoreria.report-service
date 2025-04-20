package um.tesoreria.report.domain.dto;

import lombok.Data;

@Data
public class GeograficaDto {
    private Integer geograficaId;
    private String nombre = "";
    private Byte sinChequera = 0;
}
