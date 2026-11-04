package um.tesoreria.report.hexagonal.contable.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.CuentaPlanDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "cuentaPlanClient", path = "/api/tesoreria/core/cuenta")
public interface CuentaClient {

    @GetMapping("/")
    List<CuentaPlanDto> findAll();
}