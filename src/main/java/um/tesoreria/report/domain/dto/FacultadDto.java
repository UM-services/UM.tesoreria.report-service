package um.tesoreria.report.domain.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FacultadDto {
    private Integer facultadId;
    private String nombre = "";
    private String codigoempresa = "";
    private String server = "";
    private String dbadm = "";
    private String dsn = "";
    private BigDecimal cuentacontable = BigDecimal.ZERO;
    private String apiserver = "";
    private Long apiport = 0L;
}
