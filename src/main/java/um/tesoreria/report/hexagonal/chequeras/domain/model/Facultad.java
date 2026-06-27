package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Facultad {
    private Integer facultadId;
    private String nombre;
    private String codigoempresa;
    private String server;
    private String dbadm;
    private String dsn;
    private BigDecimal cuentacontable;
    private String apiserver;
    private Long apiport;
}
