package um.tesoreria.report.hexagonal.contable.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.report.hexagonal.contable.application.service.ContableReportService;

import java.io.FileNotFoundException;

import static um.tesoreria.report.util.Tool.generateFile;

@RestController
@RequestMapping("/api/tesoreria/report/contable")
@RequiredArgsConstructor
@Slf4j
public class ContableReportController {

    private final ContableReportService service;

    @GetMapping("/planilla/lectivo/{lectivoId}")
    public ResponseEntity<Resource> generateReport(@PathVariable Integer lectivoId) throws FileNotFoundException {
        log.debug("Processing ContableReportController.generateReport for lectivoId: {}", lectivoId);
        String filename = service.generateReport(lectivoId);
        return generateFile(filename, "planilla_imputacion_" + lectivoId + ".xlsx");
    }
}
