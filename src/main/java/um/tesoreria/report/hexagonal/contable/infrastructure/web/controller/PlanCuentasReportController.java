package um.tesoreria.report.hexagonal.contable.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.report.hexagonal.contable.domain.ports.in.GeneratePlanCuentasExcelUseCase;

@RestController
@RequestMapping("/api/tesoreria/report/contable/plan-cuentas")
@RequiredArgsConstructor
@Slf4j
public class PlanCuentasReportController {

    static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final GeneratePlanCuentasExcelUseCase useCase;

    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel() {
        log.debug("Processing PlanCuentasReportController.excel");
        byte[] contenido = useCase.generate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plan-de-cuentas.xlsx")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(XLSX)
                .contentLength(contenido.length)
                .body(contenido);
    }
}