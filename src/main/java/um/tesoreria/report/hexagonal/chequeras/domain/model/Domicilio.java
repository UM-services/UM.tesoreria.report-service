package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Domicilio {
    private Long domicilioId;
    private BigDecimal personaId;
    private Integer documentoId;
    private OffsetDateTime fecha;
    private String calle;
    private String puerta;
    private String piso;
    private String dpto;
    private String telefono;
    private String movil;
    private String observaciones;
    private String codigoPostal;
    private Integer facultadId;
    private Integer provinciaId;
    private Integer localidadId;
    private String emailPersonal;
    private String emailInstitucional;
    private String laboral;
}
