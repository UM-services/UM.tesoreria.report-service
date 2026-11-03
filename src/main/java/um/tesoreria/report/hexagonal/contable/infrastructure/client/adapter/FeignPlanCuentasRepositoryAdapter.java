package um.tesoreria.report.hexagonal.contable.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.contable.domain.model.CuentaPlan;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.PlanCuentasRepository;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.CuentaClient;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.SedeClient;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.SedeDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FeignPlanCuentasRepositoryAdapter implements PlanCuentasRepository {

    private final CuentaClient cuentaClient;
    private final SedeClient sedeClient;

    @Override
    public List<CuentaPlan> findAllCuentas() {
        var dtos = cuentaClient.findAll();
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .filter(Objects::nonNull)
                .map(dto -> new CuentaPlan(dto.getNumeroCuenta(), dto.getNombre(), dto.getGrado(),
                        dto.getIntegradora(), dto.getVisible(), dto.getGeograficaId()))
                .toList();
    }

    @Override
    public Map<Integer, String> findNombresSedes() {
        Map<Integer, String> sedes = new HashMap<>();
        List<SedeDto> dtos = sedeClient.findAll();
        if (dtos != null) {
            for (SedeDto dto : dtos) {
                if (dto != null && dto.getGeograficaId() != null && dto.getNombre() != null) {
                    sedes.put(dto.getGeograficaId(), dto.getNombre());
                }
            }
        }
        return sedes;
    }
}