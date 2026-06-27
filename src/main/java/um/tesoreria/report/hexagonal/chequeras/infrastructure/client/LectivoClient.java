package um.tesoreria.report.hexagonal.chequeras.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.LectivoDto;

@FeignClient(name = "tesoreria-core-service", contextId = "lectivoClient", path = "/api/tesoreria/core/lectivo")
public interface LectivoClient {

    @GetMapping("/{lectivoId}")
    LectivoDto findByLectivoId(@PathVariable Integer lectivoId);

}
