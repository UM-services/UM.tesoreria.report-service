package um.tesoreria.report.client.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.domain.dto.ChequeraSerieDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "chequeraSerieClient", path = "/api/tesoreria/core/chequeraSerie")
public interface ChequeraSerieClient {

    @GetMapping("/lectivo/{facultadId}/{lectivoId}")
    List<ChequeraSerieDto> findAllByLectivo(@PathVariable Integer facultadId, @PathVariable Integer lectivoId);

    @GetMapping("/lectivo/test/{facultadId}/{lectivoId}")
    List<ChequeraSerieDto> findAllByLectivoTest(@PathVariable Integer facultadId, @PathVariable Integer lectivoId);

}
