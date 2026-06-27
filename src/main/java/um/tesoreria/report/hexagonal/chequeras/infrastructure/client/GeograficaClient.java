package um.tesoreria.report.hexagonal.chequeras.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.GeograficaDto;

@FeignClient(name = "tesoreria-core-service", contextId = "geograficaClient", path = "/api/tesoreria/core/geografica")
public interface GeograficaClient {

    @GetMapping("/{geograficaId}")
    GeograficaDto findByGeograficaId(@PathVariable Integer geograficaId);

}
