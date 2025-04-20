package um.tesoreria.report.client.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.domain.dto.core.CuotaPeriodoDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/chequeraCuota")
public interface ChequeraCuotaClient {

    @GetMapping("/periodos/lectivo/{lectivoId}")
    List<CuotaPeriodoDto> findAllPeriodosLectivo(@PathVariable Integer lectivoId);

}
