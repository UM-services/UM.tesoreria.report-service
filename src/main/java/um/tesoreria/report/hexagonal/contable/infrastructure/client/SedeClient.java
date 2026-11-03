package um.tesoreria.report.hexagonal.contable.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.SedeDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "sedeClient", path = "/api/tesoreria/core/geografica")
public interface SedeClient {

    @GetMapping("/")
    List<SedeDto> findAll();
}