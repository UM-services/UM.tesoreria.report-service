package um.tesoreria.report.hexagonal.contable.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.contable.domain.model.Mayor;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorCuenta;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorMovimiento;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorSaldoInicial;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorSeleccion;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.MayorRepository;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.MayorClient;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.MayorConsultaDto;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.MayorCuentaDto;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.MayorMovimientoDto;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.MayorSaldoInicialDto;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.MayorSolicitudDto;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FeignMayorRepositoryAdapter implements MayorRepository {

    private final MayorClient client;

    @Override
    public Mayor consultar(MayorSeleccion seleccion) {
        MayorConsultaDto dto = client.consultar(new MayorSolicitudDto(seleccion.desde(), seleccion.hasta(),
                seleccion.cuentas(), seleccion.cuentaDesde(), seleccion.cuentaHasta()));
        if (dto == null) {
            return new Mayor(seleccion.desde(), seleccion.hasta(), List.of());
        }
        List<MayorCuentaDto> cuentas = dto.getCuentas() == null ? List.of() : dto.getCuentas();
        return new Mayor(dto.getDesde(), dto.getHasta(), cuentas.stream().filter(Objects::nonNull)
                .map(FeignMayorRepositoryAdapter::cuenta).toList());
    }

    private static MayorCuenta cuenta(MayorCuentaDto dto) {
        MayorSaldoInicialDto inicial = dto.getSaldoInicial();
        List<MayorMovimientoDto> movimientos = dto.getMovimientos() == null ? List.of() : dto.getMovimientos();
        return new MayorCuenta(dto.getNumeroCuenta(), dto.getNombre(),
                inicial == null ? null : new MayorSaldoInicial(inicial.getDebe(), inicial.getHaber(), inicial.getSaldo()),
                movimientos.stream().filter(Objects::nonNull).map(FeignMayorRepositoryAdapter::movimiento).toList(),
                dto.getTotalDebe(), dto.getTotalHaber(), dto.getSaldoFinal());
    }

    private static MayorMovimiento movimiento(MayorMovimientoDto dto) {
        return new MayorMovimiento(dto.getFechaContable(), dto.getOrdenContable(), dto.getEjercicioId(),
                dto.getEjercicioNombre(), dto.getComprobante(), dto.getProveedor(), dto.getConcepto(),
                dto.getOrdenPago(), dto.getBeneficiario(), dto.getDebe(), dto.getHaber(), dto.getSaldo(),
                dto.getOrdenPagoValor(), dto.getValor(), dto.getNumeroValor(), dto.getImporteValor());
    }
}