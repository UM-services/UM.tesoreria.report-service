package um.tesoreria.report.hexagonal.chequeras.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.MateriasEstadoDto;
import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "materiasInscriptasClient",
        path = "/api/tesoreria/core/materiasInscriptas")
public interface MateriasInscriptasClient {
    @GetMapping("/{facultadId}/{tipoChequeraId}/{chequeraSerieId}")
    List<MateriasEstadoDto> findMaterias(@PathVariable Integer facultadId,
                                       @PathVariable Integer tipoChequeraId,
                                       @PathVariable Long chequeraSerieId);
}
