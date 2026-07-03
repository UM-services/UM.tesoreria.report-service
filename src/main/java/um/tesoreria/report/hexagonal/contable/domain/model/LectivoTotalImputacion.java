package um.tesoreria.report.hexagonal.contable.domain.model;

import lombok.*;
import um.tesoreria.report.hexagonal.chequeras.domain.model.Facultad;
import um.tesoreria.report.hexagonal.chequeras.domain.model.Lectivo;
import um.tesoreria.report.hexagonal.chequeras.domain.model.Producto;
import um.tesoreria.report.hexagonal.chequeras.domain.model.TipoChequera;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectivoTotalImputacion {
    private Long lectivoTotalImputacionId;
    private Integer facultadId;
    private Integer lectivoId;
    private Integer tipoChequeraId;
    private Integer productoId;
    private BigDecimal numeroCuenta;

    private Facultad facultad;
    private Lectivo lectivo;
    private TipoChequera tipoChequera;
    private Producto producto;
    private Cuenta cuenta;
}
