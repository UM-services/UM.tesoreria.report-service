package um.tesoreria.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import um.tesoreria.report.client.core.ChequeraCuotaClient;
import um.tesoreria.report.client.core.ChequeraSerieClient;
import um.tesoreria.report.client.core.LectivoClient;
import um.tesoreria.report.client.core.LegajoClient;
import um.tesoreria.report.client.core.facade.ChequeraClient;
import um.tesoreria.report.domain.dto.ChequeraSerieDto;
import um.tesoreria.report.domain.dto.core.ChequeraPagoDto;
import um.tesoreria.report.domain.dto.core.CuotaPeriodoDto;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChequerasService {

    private final Environment environment;
    private final ChequeraSerieClient chequeraSerieClient;
    private final LectivoClient lectivoClient;
    private final ChequeraCuotaClient chequeraCuotaClient;
    private final ChequeraClient chequeraClient;
    private final LegajoClient legajoClient;

    public ChequerasService(Environment environment, ChequeraSerieClient chequeraSerieClient, LectivoClient lectivoClient, ChequeraCuotaClient chequeraCuotaClient, ChequeraClient chequeraClient, LegajoClient legajoClient) {
        this.environment = environment;
        this.chequeraSerieClient = chequeraSerieClient;
        this.lectivoClient = lectivoClient;
        this.chequeraCuotaClient = chequeraCuotaClient;
        this.chequeraClient = chequeraClient;
        this.legajoClient = legajoClient;
    }

    public String generatePlanillaDetalle(Integer facultadId, Integer lectivoId) {
        log.debug("Processing ChequerasService.generatePlanillaDetalle");
        String path = environment.getProperty("path.reports");

        String filename = path + MessageFormat.format("cuotas.{0}.{1}.xlsx", facultadId, lectivoId);

        Workbook book = new XSSFWorkbook();
        CellStyle styleNormal = book.createCellStyle();
        Font fontNormal = book.createFont();
        fontNormal.setBold(false);
        styleNormal.setFont(fontNormal);

        CellStyle styleBold = book.createCellStyle();
        Font fontBold = book.createFont();
        fontBold.setBold(true);
        styleBold.setFont(fontBold);

        var lectivo = lectivoClient.findByLectivoId(lectivoId);
        var legajos = legajoClient.findAllByFacultadId(facultadId)
                .stream()
                .collect(Collectors.toMap(
                    legajo -> legajo.getPersonaId() + "." + legajo.getDocumentoId(),
                    legajo -> legajo,
                    (legajo, replacement) -> legajo
                ));

        Sheet sheet = book.createSheet(lectivo.getNombre());
        Row row;
        int fila = 0;
        row = sheet.createRow(fila);
        this.setCellString(row, 0, "Chequera", styleBold);
        this.setCellString(row, 1, "DU", styleBold);
        this.setCellString(row, 2, "Apellido, Nombre", styleBold);
        this.setCellString(row, 3, "Facultad", styleBold);
        this.setCellString(row, 4, "Sede", styleBold);
        this.setCellString(row, 5, "Carrera", styleBold);
        this.setCellString(row, 6, "Curso", styleBold);
        this.setCellString(row, 7, "Tipo Chequera", styleBold);

        var periodos = chequeraCuotaClient.findAllPeriodosLectivo(lectivoId);

        // Add period headers starting from column 8
        int periodoColumn = 8;
        for (CuotaPeriodoDto periodo : periodos) {
            this.setCellString(row, periodoColumn++, MessageFormat.format("{0}/{1,number,#} Producto", periodo.getMes(), periodo.getAnho()), styleBold);
            this.setCellString(row, periodoColumn++, MessageFormat.format("{0}/{1,number,#} Importe", periodo.getMes(), periodo.getAnho()), styleBold);
            this.setCellString(row, periodoColumn++, MessageFormat.format("{0}/{1,number,#} Vencimiento", periodo.getMes(), periodo.getAnho()), styleBold);
            this.setCellString(row, periodoColumn++, MessageFormat.format("{0}/{1,number,#} Pago", periodo.getMes(), periodo.getAnho()), styleBold);
            this.setCellString(row, periodoColumn++, MessageFormat.format("{0}/{1,number,#} Medio", periodo.getMes(), periodo.getAnho()), styleBold);
            this.setCellString(row, periodoColumn++, MessageFormat.format("{0}/{1,number,#} Pagado", periodo.getMes(), periodo.getAnho()), styleBold);
        }

        for (ChequeraSerieDto chequeraSerie : chequeraSerieClient.findAllByLectivo(facultadId, lectivoId)) {
            // determina carrera
            var carrera = "";
            var key = chequeraSerie.getPersonaId() + "." + chequeraSerie.getDocumentoId();
            if (legajos.containsKey(key)) {
                var legajo = legajos.get(key);
                if (legajo.getCarrera() != null) {
                    var plan = "";
                    if (legajo.getCarrera().getPlan() != null) {
                        plan = legajo.getCarrera().getPlan().getNombre();
                    }
                    carrera = MessageFormat.format("{0}/{1}", plan, legajo.getCarrera().getNombre());
                }
            }

            row = sheet.createRow(++fila);
            this.setCellString(row, 0, MessageFormat.format("{0}/{1}/{2,number,#}", chequeraSerie.getFacultadId(), chequeraSerie.getTipoChequeraId(), chequeraSerie.getChequeraSerieId()), styleNormal);
            this.setCellBigDecimal(row, 1, chequeraSerie.getPersona().getPersonaId(), styleNormal);
            this.setCellString(row, 2, MessageFormat.format("{0}, {1}", chequeraSerie.getPersona().getApellido(), chequeraSerie.getPersona().getNombre()), styleNormal);
            this.setCellString(row, 3, chequeraSerie.getFacultad().getNombre(), styleNormal);
            this.setCellString(row, 4, chequeraSerie.getGeografica().getNombre(), styleNormal);
            this.setCellString(row, 5, carrera, styleNormal);
            this.setCellInteger(row, 6, chequeraSerie.getCursoId(), styleNormal);
            this.setCellString(row, 7, chequeraSerie.getTipoChequera().getNombre(), styleNormal);
            var cuotas = chequeraClient.findAllCuotaPagosByChequera(chequeraSerie.getFacultadId(), chequeraSerie.getTipoChequeraId(), chequeraSerie.getChequeraSerieId(), chequeraSerie.getAlternativaId());
            // Crear un Map para búsqueda eficiente de cuotas
            var cuotasMap = cuotas.stream()
                    .collect(Collectors.toMap(
                            cuota -> cuota.getMes() + "." + cuota.getAnho(), // clave compuesta de mes-año
                            cuota -> cuota,
                            (cuota, replacement) -> cuota // en caso de duplicados, tomar la primera
                    ));

            periodoColumn = 8;
            for (CuotaPeriodoDto periodo : periodos) {
                var cuota = cuotasMap.get(periodo.getMes() + "." + periodo.getAnho());

                if (cuota == null) {
                    periodoColumn += 6;
                } else {
                    this.setCellString(row, periodoColumn++, cuota.getProducto().getNombre(), styleNormal);
                    this.setCellBigDecimal(row, periodoColumn++, cuota.getImporte1(), styleNormal);
                    this.setCellOffsetDateTime(row, periodoColumn++, cuota.getVencimiento1(), styleNormal);
                    if (!cuota.getChequeraPagos().isEmpty()) {
                        var primerPago = cuota.getChequeraPagos().getFirst();
                        if (primerPago != null) {
                            this.setCellOffsetDateTime(row, periodoColumn++, primerPago.getFecha(), styleNormal);
                            this.setCellString(row, periodoColumn++, primerPago.getTipoPago().getNombre(), styleNormal);
                            this.setCellBigDecimal(row, periodoColumn++, primerPago.getImporte(), styleNormal);
                        } else {
                            periodoColumn += 3;
                        }
                    } else {
                        periodoColumn += 3;
                    }
                }

            }
        }

        for (int column = 0; column < sheet.getRow(0).getPhysicalNumberOfCells(); column++)
            sheet.autoSizeColumn(column);

        try {
            File file = new File(filename);
            FileOutputStream output = new FileOutputStream(file);
            book.write(output);
            output.flush();
            output.close();
            book.close();
        } catch (Exception e) {
            log.debug("Error escribiendo cuotas");
        }
        return filename;
    }

    private void logPagos(List<ChequeraPagoDto> chequeraPagos) {
        log.debug("Processing ChequerasService.logPagos");
        try {
            log.debug("Pagos -> {}", JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(chequeraPagos));
        } catch (JsonProcessingException e) {
            log.debug("Pagos jsonify error -> {}", e.getMessage());
        }
    }

    private void setCellOffsetDateTime(Row row, int column, OffsetDateTime value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(new Date(value.toInstant().toEpochMilli()));
        
        // Crear un estilo específico para fechas
        CellStyle dateStyle = row.getSheet().getWorkbook().createCellStyle();
        dateStyle.cloneStyleFrom(style);
        dateStyle.setDataFormat(row.getSheet().getWorkbook().createDataFormat().getFormat("dd/MM/yyyy"));
        
        cell.setCellStyle(dateStyle);
    }

    private void setCellLong(Row row, int column, Long value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void setCellBigDecimal(Row row, int column, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }

    private void setCellString(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void setCellInteger(Row row, int column, Integer value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

}
