package um.tesoreria.report.hexagonal.chequeras.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.CuotaPeriodoDto;
import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "chequeraCuotaClient", path = "/api/tesoreria/core/chequeraCuota")
public interface ChequeraCuotaClient {

    @GetMapping("/periodos/lectivo/{lectivoId}")
    List<CuotaPeriodoDto> findAllPeriodosLectivo(@PathVariable Integer lectivoId);

}
