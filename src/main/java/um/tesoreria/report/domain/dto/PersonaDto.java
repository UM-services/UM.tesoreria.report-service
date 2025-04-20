package um.tesoreria.report.domain.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PersonaDto {
    private Long uniqueId;
    private BigDecimal personaId;
    private Integer documentoId;
    private String apellido;
    private String nombre;
    private String sexo;
    private Byte primero = 0;
    private String cuit = "";
    private String cbu = "";
    private String password;
}
