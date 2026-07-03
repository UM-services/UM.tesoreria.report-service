package um.tesoreria.report.hexagonal.contable.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.LectivoTotalImputacionDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "lectivoTotalImputacionClient", path = "/api/tesoreria/core/lectivototalimputacion")
public interface LectivoTotalImputacionClient {

    @GetMapping("/lectivo/{lectivoId}")
    List<LectivoTotalImputacionDto> findAllByLectivo(@PathVariable("lectivoId") Integer lectivoId);

}
