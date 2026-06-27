package um.tesoreria.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import um.tesoreria.report.client.core.*;
import um.tesoreria.report.client.core.facade.ChequeraClient;
import um.tesoreria.report.domain.dto.ChequeraSerieDto;
import um.tesoreria.report.domain.dto.core.ChequeraCuotaPagosDto;
import um.tesoreria.report.domain.dto.core.CuotaPeriodoDto;
import um.tesoreria.report.util.Jsonifier;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChequerasService {

    private final Environment environment;
    private final ChequeraSerieClient chequeraSerieClient;
    private final LectivoClient lectivoClient;
    private final ChequeraCuotaClient chequeraCuotaClient;
    private final ChequeraClient chequeraClient;
    private final LegajoClient legajoClient;
    private final ChequeraPagoClient chequeraPagoClient;
    private final GeograficaClient geograficaClient;

    public String generatePlanillaDetalle(Integer facultadId, Integer lectivoId, Integer geograficaId) {
        log.debug("Processing ChequerasService.generatePlanillaDetalle");
        String path = environment.getProperty("path.reports");

        String filename = path + MessageFormat.format("cuotas.{0}.{1}.{2}.xlsx", facultadId, lectivoId, geograficaId);

        Workbook book = new XSSFWorkbook();
        CellStyle styleNormal = book.createCellStyle();
        Font fontNormal = book.createFont();
        fontNormal.setBold(false);
        styleNormal.setFont(fontNormal);

        CellStyle styleBold = book.createCellStyle();
        Font fontBold = book.createFont();
        fontBold.setBold(true);
        styleBold.setFont(fontBold);

        // Crear y configurar el estilo de fecha una sola vez
        CellStyle dateStyleNormal = book.createCellStyle();
        dateStyleNormal.cloneStyleFrom(styleNormal);
        dateStyleNormal.setDataFormat(book.createDataFormat().getFormat("dd/MM/yyyy"));

        var lectivo = lectivoClient.findByLectivoId(lectivoId);
        var geografica = geograficaClient.findByGeograficaId(geograficaId);
        
        log.debug("Leyendo legajos");
        var legajos = legajoClient.findAllByFacultadId(facultadId)
                .stream()
                .collect(Collectors.toMap(
                        legajo -> legajo.getPersonaId() + "." + legajo.getDocumentoId(),
                        legajo -> legajo,
                        (legajo, replacement) -> legajo
                ));
        log.debug("Legajos leídos");

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
        this.setCellString(row, 8, "HPUM", styleBold);
        this.setCellString(row, 9, "Beca", styleBold);

        var allChequeras = chequeraSerieClient.findAllBySede(facultadId, lectivoId, geograficaId);
        var periodos = chequeraCuotaClient.findAllPeriodosLectivo(lectivoId);

        // Add period headers starting from column 8
        int periodoColumn = 10;
        for (CuotaPeriodoDto periodo : periodos) {
            this.setCellString(row, periodoColumn++, MessageFormat.format("{0}/{1,number,#} Producto", periodo.getMes(), periodo.getAnho()), styleBold);
            this.setCellString(row, periodoColumn++, MessageFormat.format("{0}/{1,number,#} Importe", periodo.getMes(), periodo.getAnho()), styleBold);
            this.setCellString(row, periodoColumn++, MessageFormat.format("{0}/{1,number,#} Vencimiento", periodo.getMes(), periodo.getAnho()), styleBold);
            this.setCellString(row, periodoColumn++, MessageFormat.format("{0}/{1,number,#} Pago", periodo.getMes(), periodo.getAnho()), styleBold);
            this.setCellString(row, periodoColumn++, MessageFormat.format("{0}/{1,number,#} Medio", periodo.getMes(), periodo.getAnho()), styleBold);
            this.setCellString(row, periodoColumn++, MessageFormat.format("{0}/{1,number,#} Pagado", periodo.getMes(), periodo.getAnho()), styleBold);
            this.setCellString(row, periodoColumn++, MessageFormat.format("{0}/{1,number,#} id MP", periodo.getMes(), periodo.getAnho()), styleBold);
        }

        // --- OPTIMIZACIÓN DE E/S PARALELA ---
        // Fetch de cuotas en paralelo usando Virtual Threads y limitando la concurrencia a 20 para proteger core-service
        Map<String, List<ChequeraCuotaPagosDto>> allCuotasMap = new HashMap<>();
        Semaphore semaphore = new Semaphore(20);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = allChequeras.stream()
                .map(chequeraSerie -> CompletableFuture.runAsync(() -> {
                    try {
                        semaphore.acquire();
                        List<ChequeraCuotaPagosDto> cuotas = chequeraClient.findAllCuotaPagosByChequera(
                            chequeraSerie.getFacultadId(), 
                            chequeraSerie.getTipoChequeraId(), 
                            chequeraSerie.getChequeraSerieId(), 
                            chequeraSerie.getAlternativaId()
                        );
                        String key = chequeraSerie.getFacultadId() + "." + chequeraSerie.getTipoChequeraId() + "." + chequeraSerie.getChequeraSerieId();
                        synchronized (allCuotasMap) {
                            allCuotasMap.put(key, cuotas);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Hilo interrumpido al obtener cuotas para chequera: {}", chequeraSerie.getChequeraSerieId(), e);
                    } catch (Exception e) {
                        log.error("Error al obtener cuotas para chequera: {}", chequeraSerie.getChequeraSerieId(), e);
                    } finally {
                        semaphore.release();
                    }
                }, executor))
                .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        // Iterar secuencialmente para escribir sobre la hoja (POI no es thread-safe para escritura paralela)
        for (ChequeraSerieDto chequeraSerie : allChequeras) {
            if (log.isDebugEnabled()) {
                log.debug("ChequeraSerie: {}", chequeraSerie.jsonify());
                log.debug("Determinando carrera");
            }
            
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
            if (log.isDebugEnabled()) {
                log.debug("Carrera determinada");
            }

            String cuotasKey = chequeraSerie.getFacultadId() + "." + chequeraSerie.getTipoChequeraId() + "." + chequeraSerie.getChequeraSerieId();
            List<ChequeraCuotaPagosDto> cuotas = allCuotasMap.getOrDefault(cuotasKey, Collections.emptyList());
            
            if (log.isDebugEnabled()) {
                log.debug("Cuotas: {}", Jsonifier.builder(cuotas).build());
            }
            
            var cuotasMap = cuotas.stream()
                    .collect(Collectors.groupingBy(
                            cuota -> cuota.getMes() + "." + cuota.getAnho()
                    ));

            row = sheet.createRow(++fila);
            this.setCellString(row, 0, MessageFormat.format("{0}/{1}/{2,number,#}", chequeraSerie.getFacultadId(), chequeraSerie.getTipoChequeraId(), chequeraSerie.getChequeraSerieId()), styleNormal);
            if (chequeraSerie.getPersona() != null) {
                this.setCellBigDecimal(row, 1, chequeraSerie.getPersona().getPersonaId(), styleNormal);
                this.setCellString(row, 2, MessageFormat.format("{0}, {1}", chequeraSerie.getPersona().getApellido(), chequeraSerie.getPersona().getNombre()), styleNormal);
            }
            this.setCellString(row, 3, chequeraSerie.getFacultad().getNombre(), styleNormal);
            this.setCellString(row, 4, chequeraSerie.getGeografica().getNombre(), styleNormal);
            this.setCellString(row, 5, carrera, styleNormal);
            this.setCellInteger(row, 6, chequeraSerie.getCursoId(), styleNormal);
            this.setCellString(row, 7, chequeraSerie.getTipoChequera().getNombre(), styleNormal);
            this.setCellString(row, 8, chequeraSerie.getHpum() == 1 ? "X" : "", styleNormal);
            this.setCellBigDecimal(row, 9, chequeraSerie.getBecaPorcentaje(), styleNormal);

            periodoColumn = 10;
            int maxOffset = 0;
            for (CuotaPeriodoDto periodo : periodos) {
                if (log.isDebugEnabled()) {
                    log.debug("Periodo: {}", periodo.jsonify());
                }
                String periodoKey = periodo.getMes() + "." + periodo.getAnho();
                var cuotasDelPeriodo = cuotasMap.get(periodoKey);
                
                if (log.isDebugEnabled()) {
                    log.debug("Cuotas del Periodo -> {}", Jsonifier.builder(cuotasDelPeriodo).build());
                }
                
                if (cuotasDelPeriodo != null) {
                    int offset = 0;
                    for (var cuota : cuotasDelPeriodo) {
                        Row innerRow;
                        if (offset == 0) {
                            innerRow = row;
                        } else {
                            var verifyRow = sheet.getRow(fila + offset);
                            innerRow = (verifyRow == null) ? sheet.createRow(fila + offset) : verifyRow;
                        }

                        this.setCellString(innerRow, 0, MessageFormat.format("{0}/{1}/{2,number,#}", chequeraSerie.getFacultadId(), chequeraSerie.getTipoChequeraId(), chequeraSerie.getChequeraSerieId()), styleNormal);
                        if (chequeraSerie.getPersona() != null) {
                            this.setCellBigDecimal(innerRow, 1, chequeraSerie.getPersona().getPersonaId(), styleNormal);
                            this.setCellString(innerRow, 2, MessageFormat.format("{0}, {1}", chequeraSerie.getPersona().getApellido(), chequeraSerie.getPersona().getNombre()), styleNormal);
                        }
                        this.setCellString(innerRow, 3, chequeraSerie.getFacultad().getNombre(), styleNormal);
                        this.setCellString(innerRow, 4, chequeraSerie.getGeografica().getNombre(), styleNormal);
                        this.setCellString(innerRow, 5, carrera, styleNormal);
                        this.setCellInteger(innerRow, 6, chequeraSerie.getCursoId(), styleNormal);
                        this.setCellString(innerRow, 7, chequeraSerie.getTipoChequera().getNombre(), styleNormal);
                        this.setCellString(innerRow, 8, chequeraSerie.getHpum() == 1 ? "X" : "", styleNormal);
                        this.setCellBigDecimal(innerRow, 9, chequeraSerie.getBecaPorcentaje(), styleNormal);

                        if (log.isDebugEnabled()) {
                            log.debug("Cuota a escribir -> {}", Jsonifier.builder(cuota).build());
                        }
                        this.setCellString(innerRow, periodoColumn, cuota.getProducto().getNombre(), styleNormal);
                        if (cuota.getBaja() == 0) {
                            this.setCellBigDecimal(innerRow, periodoColumn + 1, cuota.getImporte1(), styleNormal);
                        } else {
                            this.setCellString(innerRow, periodoColumn + 1, "Baja", styleNormal);
                        }
                        this.setCellOffsetDateTime(innerRow, periodoColumn + 2, cuota.getVencimiento1(), dateStyleNormal);

                        if (!cuota.getChequeraPagos().isEmpty()) {
                            var pago = cuota.getChequeraPagos().getFirst();
                            if (log.isDebugEnabled()) {
                                log.debug("Pago a escribir -> {}", Jsonifier.builder(pago).build());
                            }
                            this.setCellOffsetDateTime(innerRow, periodoColumn + 3, pago.getFecha(), dateStyleNormal);
                            this.setCellString(innerRow, periodoColumn + 4, pago.getTipoPago().getNombre(), styleNormal);
                            this.setCellBigDecimal(innerRow, periodoColumn + 5, pago.getImporte(), styleNormal);
                            this.setCellString(innerRow, periodoColumn + 6, pago.getIdMercadoPago(), styleNormal);
                        }
                        if (++offset > maxOffset) {
                            maxOffset = offset;
                        }
                    }
                }
                periodoColumn += 7;
            }
            fila = fila + maxOffset - 1;
        }

        // Optimización de autoSize: Solo ajustar las primeras columnas descriptivas 
        // (ya que los campos de importes y fechas tienen un ancho predecible)
        for (int column = 0; column < 10; column++) {
            sheet.autoSizeColumn(column);
        }
        // Asignar un ancho fijo estimado a las columnas repetitivas de los períodos para ahorrar tiempo de procesamiento
        for (int column = 10; column < sheet.getRow(0).getPhysicalNumberOfCells(); column++) {
            sheet.setColumnWidth(column, 4000); // ~15-16 caracteres
        }

        try {
            File file = new File(filename);
            FileOutputStream output = new FileOutputStream(file);
            book.write(output);
            output.flush();
            output.close();
            book.close();
        } catch (Exception e) {
            log.error("Error escribiendo cuotas", e);
        }
        return filename;
    }

    public String generatePlanillaPagos(Integer facultadId, Integer tipoChequeraId, Integer lectivoId) {
        log.debug("Processing ChequerasService.generatePlanillaPagos");
        String path = environment.getProperty("path.reports");

        String filename = path + MessageFormat.format("pagos.{0}.{1}.{2}.xlsx", facultadId, tipoChequeraId, lectivoId);

        Workbook book = new XSSFWorkbook();
        CellStyle styleNormal = book.createCellStyle();
        Font fontNormal = book.createFont();
        fontNormal.setBold(false);
        styleNormal.setFont(fontNormal);

        CellStyle styleBold = book.createCellStyle();
        Font fontBold = book.createFont();
        fontBold.setBold(true);
        styleBold.setFont(fontBold);

        // Crear y configurar el estilo de fecha una sola vez
        CellStyle dateStyleNormal = book.createCellStyle();
        dateStyleNormal.cloneStyleFrom(styleNormal);
        dateStyleNormal.setDataFormat(book.createDataFormat().getFormat("dd/MM/yyyy"));

        Sheet sheet = book.createSheet("pagos");
        Row row;
        int fila = 0;
        row = sheet.createRow(fila);
        this.setCellString(row, 0, "Facultad", styleBold);
        this.setCellString(row, 1, "Lectivo", styleBold);
        this.setCellString(row, 2, "Tipo de Chequera", styleBold);
        this.setCellString(row, 3, "Sede", styleBold);
        this.setCellString(row, 4, "Documento", styleBold);
        this.setCellString(row, 5, "Apellido, Nombre", styleBold);
        this.setCellString(row, 6, "Chequera", styleBold);
        this.setCellString(row, 7, "Periodo", styleBold);
        this.setCellString(row, 8, "Fecha Pago", styleBold);
        this.setCellString(row, 9, "Importe Pagado", styleBold);
        this.setCellString(row, 10, "Tipo Pago", styleBold);
        this.setCellString(row, 11, "e-mail Institucional", styleBold);
        this.setCellString(row, 12, "e-mail Personal", styleBold);

        for (var chequeraPago : chequeraPagoClient.findAllByFacultadIdAndTipoChequeraIdAndLectivoId(facultadId, tipoChequeraId, lectivoId)) {
            row = sheet.createRow(++fila);
            this.setCellString(row, 0, chequeraPago.getChequeraCuota().getFacultad().getNombre(), styleNormal);
            this.setCellString(row, 1, chequeraPago.getChequeraCuota().getChequeraSerie().getLectivo().getNombre(), styleNormal);
            this.setCellString(row, 2, chequeraPago.getChequeraCuota().getTipoChequera().getNombre(), styleNormal);
            this.setCellString(row, 3, chequeraPago.getChequeraCuota().getChequeraSerie().getGeografica().getNombre(), styleNormal);
            this.setCellBigDecimal(row, 4, chequeraPago.getChequeraCuota().getChequeraSerie().getPersonaId(), styleNormal);
            this.setCellString(row, 5, MessageFormat.format("{0}, {1}", chequeraPago.getChequeraCuota().getChequeraSerie().getPersona().getApellido(), chequeraPago.getChequeraCuota().getChequeraSerie().getPersona().getNombre()), styleNormal);
            this.setCellString(row, 6, MessageFormat.format("{0,number,#}/{1,number,#}/{2,number,#}", chequeraPago.getFacultadId(), chequeraPago.getTipoChequeraId(), chequeraPago.getChequeraSerieId()), styleNormal);
            this.setCellString(row, 7, MessageFormat.format("{0}/{1,number,#}", chequeraPago.getMes(), chequeraPago.getAnho()), styleNormal);
            this.setCellOffsetDateTime(row, 8, chequeraPago.getFecha(), dateStyleNormal);
            this.setCellBigDecimal(row, 9, chequeraPago.getImporte(), styleNormal);
            this.setCellString(row, 10, chequeraPago.getTipoPago().getNombre(), styleNormal);
            this.setCellString(row, 11, chequeraPago.getChequeraCuota().getChequeraSerie().getDomicilio().getEmailInstitucional(), styleNormal);
            this.setCellString(row, 12, chequeraPago.getChequeraCuota().getChequeraSerie().getDomicilio().getEmailPersonal(), styleNormal);
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
            log.error("Error escribiendo pagos", e);
        }
        return filename;
    }

    private void setCellOffsetDateTime(Row row, int column, OffsetDateTime value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(new Date(value.toInstant().toEpochMilli()));
        cell.setCellStyle(style);
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