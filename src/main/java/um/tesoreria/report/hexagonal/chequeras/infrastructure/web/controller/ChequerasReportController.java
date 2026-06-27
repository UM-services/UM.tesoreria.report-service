package um.tesoreria.report.hexagonal.chequeras.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.report.hexagonal.chequeras.application.service.ChequerasReportService;

import java.io.FileNotFoundException;

import static um.tesoreria.report.util.Tool.generateFile;

@RestController
@RequestMapping("/api/tesoreria/report/chequeras")
@RequiredArgsConstructor
@Slf4j
public class ChequerasReportController {

    private final ChequerasReportService service;

    @GetMapping("/planilla/detalle/facultad/{facultadId}/lectivo/{lectivoId}/geografica/{geograficaId}")
    public ResponseEntity<Resource> generatePlanillaDetalle(@PathVariable Integer facultadId,
                                                            @PathVariable Integer lectivoId,
                                                            @PathVariable Integer geograficaId) throws FileNotFoundException {
        log.debug("Processing ChequerasReportController.generatePlanillaDetalle");
        return generateFile(service.generatePlanillaDetalle(facultadId, lectivoId, geograficaId), "planilla.xlsx");
    }

    @GetMapping("/planilla/detalle/vertical/facultad/{facultadId}/lectivo/{lectivoId}/geografica/{geograficaId}")
    public ResponseEntity<Resource> generatePlanillaDetalleVertical(@PathVariable Integer facultadId,
                                                            @PathVariable Integer lectivoId,
                                                            @PathVariable Integer geograficaId) throws FileNotFoundException {
        log.debug("Processing ChequerasReportController.generatePlanillaDetalleVertical");
        return generateFile(service.generatePlanillaDetalleVertical(facultadId, lectivoId, geograficaId), "planilla.xlsx");
    }

    @GetMapping("/planilla/pagos/facultad/{facultadId}/tipoChequera/{tipoChequeraId}/lectivo/{lectivoId}")
    public ResponseEntity<Resource> generatePlanillaPagos(@PathVariable Integer facultadId,
                                                          @PathVariable Integer tipoChequeraId,
                                                          @PathVariable Integer lectivoId) throws FileNotFoundException {
        log.debug("Processing ChequerasReportController.generatePlanillaPagos");
        return generateFile(service.generatePlanillaPagos(facultadId, tipoChequeraId, lectivoId), "pagos.xlsx");
    }

}
