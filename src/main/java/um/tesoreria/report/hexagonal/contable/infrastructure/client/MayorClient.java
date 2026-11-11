package um.tesoreria.report.hexagonal.contable.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.MayorConsultaDto;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.MayorSolicitudDto;

@FeignClient(name = "tesoreria-core-service", contextId = "mayorClient", path = "/api/tesoreria/core/balance")
public interface MayorClient {

    @PostMapping("/mayor/consulta")
    MayorConsultaDto consultar(@RequestBody MayorSolicitudDto solicitud);
}