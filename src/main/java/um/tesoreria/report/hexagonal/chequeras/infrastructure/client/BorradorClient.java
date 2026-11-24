package um.tesoreria.report.hexagonal.chequeras.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.BorradorDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "borradorClient",
        path = "/api/tesoreria/core/emision/chequera/borradores")
public interface BorradorClient {

    @GetMapping("/sede/facultad/{facultadId}/lectivo/{lectivoId}/geografica/{geograficaId}")
    List<BorradorDto> findAllBySede(@PathVariable Integer facultadId,
                                    @PathVariable Integer lectivoId,
                                    @PathVariable Integer geograficaId);
}
