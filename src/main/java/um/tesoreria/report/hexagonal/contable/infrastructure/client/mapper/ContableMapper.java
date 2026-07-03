package um.tesoreria.report.hexagonal.contable.infrastructure.client.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper.ChequerasMapper;
import um.tesoreria.report.hexagonal.contable.domain.model.Cuenta;
import um.tesoreria.report.hexagonal.contable.domain.model.LectivoTotalImputacion;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.CuentaDto;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.LectivoTotalImputacionDto;

@Component
@RequiredArgsConstructor
public class ContableMapper {

    private final ChequerasMapper chequerasMapper;

    public Cuenta toDomain(CuentaDto dto) {
        if (dto == null) return null;
        return Cuenta.builder()
                .numeroCuenta(dto.getNumeroCuenta())
                .nombre(dto.getNombre())
                .build();
    }

    public LectivoTotalImputacion toDomain(LectivoTotalImputacionDto dto) {
        if (dto == null) return null;
        return LectivoTotalImputacion.builder()
                .lectivoTotalImputacionId(dto.getLectivoTotalImputacionId())
                .facultadId(dto.getFacultadId())
                .lectivoId(dto.getLectivoId())
                .tipoChequeraId(dto.getTipoChequeraId())
                .productoId(dto.getProductoId())
                .numeroCuenta(dto.getNumeroCuenta())
                .facultad(chequerasMapper.toDomain(dto.getFacultad()))
                .lectivo(chequerasMapper.toDomain(dto.getLectivo()))
                .tipoChequera(chequerasMapper.toDomain(dto.getTipoChequera()))
                .producto(chequerasMapper.toDomain(dto.getProducto()))
                .cuenta(toDomain(dto.getCuenta()))
                .build();
    }
}
