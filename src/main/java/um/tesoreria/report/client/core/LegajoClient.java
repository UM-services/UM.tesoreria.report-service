package um.tesoreria.report.client.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.domain.dto.core.LegajoDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "legajoClient", path = "/api/tesoreria/core/legajo")
public interface LegajoClient {

    @GetMapping("/facultad/{facultadId}")
    List<LegajoDto> findAllByFacultadId(@PathVariable Integer facultadId);

}
