package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto;

import lombok.Data;

@Data
public class GeograficaDto {
    private Integer geograficaId;
    private String nombre = "";
    private Byte sinChequera = 0;
}
