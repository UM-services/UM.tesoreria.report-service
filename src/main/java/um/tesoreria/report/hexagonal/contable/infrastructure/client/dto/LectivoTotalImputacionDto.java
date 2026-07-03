package um.tesoreria.report.hexagonal.contable.infrastructure.client.dto;

import lombok.Data;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.FacultadDto;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.LectivoDto;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.ProductoDto;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.TipoChequeraDto;

import java.math.BigDecimal;

@Data
public class LectivoTotalImputacionDto {
    private Long lectivoTotalImputacionId;
    private Integer facultadId;
    private Integer lectivoId;
    private Integer tipoChequeraId;
    private Integer productoId;
    private BigDecimal numeroCuenta;

    private FacultadDto facultad;
    private LectivoDto lectivo;
    private TipoChequeraDto tipoChequera;
    private ProductoDto producto;
    private CuentaDto cuenta;
}
