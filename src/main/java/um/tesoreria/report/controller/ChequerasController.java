package um.tesoreria.report.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.report.service.ChequerasService;

import java.io.FileNotFoundException;

import static um.tesoreria.report.util.Tool.generateFile;

@RestController
@RequestMapping("/api/tesoreria/report/chequeras")
@Slf4j
public class ChequerasController {

    private final ChequerasService service;

    public ChequerasController(ChequerasService service) {
        this.service = service;
    }

    @GetMapping("/planilla/detalle/facultad/{facultadId}/lectivo/{lectivoId}/geografica/{geograficaId}")
    public ResponseEntity<Resource> generatePlanillaDetalle(@PathVariable Integer facultadId,
                                                            @PathVariable Integer lectivoId,
                                                            @PathVariable Integer geograficaId) throws FileNotFoundException {
        log.debug("Processing ChequerasController.generatePlanillaDetalle");
        return generateFile(service.generatePlanillaDetalle(facultadId, lectivoId, geograficaId), "planilla.xlsx");
    }

    @GetMapping("/planilla/pagos/facultad/{facultadId}/tipoChequera/{tipoChequeraId}/lectivo/{lectivoId}")
    public ResponseEntity<Resource> generatePlanillaPagos(@PathVariable Integer facultadId,
                                                          @PathVariable Integer tipoChequeraId,
                                                          @PathVariable Integer lectivoId) throws FileNotFoundException {
        log.debug("Processing ChequerasController.generatePlanillaPagos");
        return generateFile(service.generatePlanillaPagos(facultadId, tipoChequeraId, lectivoId), "pagos.xlsx");
    }

}
