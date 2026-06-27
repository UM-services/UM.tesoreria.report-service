package um.tesoreria.report.hexagonal.chequeras.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.FacultadDto;

@FeignClient(name = "tesoreria-core-service", contextId = "facultadClient", path = "/api/tesoreria/core/lectivo")
public interface FacultadClient {

    @GetMapping("/{facultadId}")
    FacultadDto findByFacultadId(@PathVariable Integer facultadId);

}
