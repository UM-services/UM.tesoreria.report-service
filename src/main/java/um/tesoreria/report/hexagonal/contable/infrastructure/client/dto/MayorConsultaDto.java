package um.tesoreria.report.hexagonal.contable.infrastructure.client.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class MayorConsultaDto {
    private OffsetDateTime desde;
    private OffsetDateTime hasta;
    private List<MayorCuentaDto> cuentas;
}