package um.tesoreria.report.client.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.domain.dto.LectivoDto;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/lectivo")
public interface LectivoClient {

    @GetMapping("/{lectivoId}")
    LectivoDto findByLectivoId(@PathVariable Integer lectivoId);

}
