package um.tesoreria.report.hexagonal.chequeras.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaDetalleVerticalUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.*;
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
import um.tesoreria.report.hexagonal.chequeras.domain.model.ChequeraCuotaPagos;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratePlanillaDetalleVerticalUseCaseImpl implements GeneratePlanillaDetalleVerticalUseCase {

    private final Environment environment;
    private final LectivoRepository lectivoRepository;
    private final LegajoRepository legajoRepository;
    private final ChequeraSerieRepository chequeraSerieRepository;
    private final ChequeraRepository chequeraRepository;

    @Override
    public String generatePlanillaDetalle(Integer facultadId, Integer lectivoId, Integer geograficaId) {
        log.debug("Processing GeneratePlanillaDetalleVerticalUseCaseImpl.generatePlanillaDetalle");
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

        var lectivo = lectivoRepository.findByLectivoId(lectivoId);

        log.debug("Leyendo legajos");
        var legajos = legajoRepository.findAllByFacultadId(facultadId)
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
        this.setCellString(row, 8, "Tipo Arancel", styleBold);
        this.setCellString(row, 9, "HPUM", styleBold);
        this.setCellString(row, 10, "Beca", styleBold);
        this.setCellString(row, 11, "Alternativa", styleBold);
        this.setCellString(row, 12, "Producto", styleBold);
        this.setCellString(row, 13, "Cuota", styleBold);
        this.setCellString(row, 14, "Año", styleBold);
        this.setCellString(row, 15, "Mes", styleBold);
        this.setCellString(row, 16, "Importe", styleBold);
        this.setCellString(row, 17, "Vencimiento", styleBold);
        this.setCellString(row, 18, "Pago", styleBold);
        this.setCellString(row, 19, "Medio", styleBold);
        this.setCellString(row, 20, "Pagado", styleBold);
        this.setCellString(row, 21, "id MP", styleBold);

        var allChequeras = chequeraSerieRepository.findAllBySede(facultadId, lectivoId, geograficaId);

        // --- OPTIMIZACIÓN DE E/S PARALELA ---
        Map<String, List<ChequeraCuotaPagos>> allCuotasMap = new HashMap<>();
        Semaphore semaphore = new Semaphore(20);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = allChequeras.stream()
                .map(chequeraSerie -> CompletableFuture.runAsync(() -> {
                    try {
                        semaphore.acquire();
                        log.debug("\n\nLeyendo pagos\n\n");
                        List<ChequeraCuotaPagos> cuotas = chequeraRepository.findAllCuotaPagosByChequera(
                            chequeraSerie.getFacultadId(),
                            chequeraSerie.getTipoChequeraId(),
                            chequeraSerie.getChequeraSerieId(),
                            chequeraSerie.getAlternativaId()
                        );
                        String key = chequeraSerie.getFacultadId() + "." + chequeraSerie.getTipoChequeraId() + "." + chequeraSerie.getChequeraSerieId() + "." + chequeraSerie.getAlternativaId();
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

        for (var chequeraSerie : allChequeras) {
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

            String cuotasKey = chequeraSerie.getFacultadId() + "." + chequeraSerie.getTipoChequeraId() + "." + chequeraSerie.getChequeraSerieId() + "." + chequeraSerie.getAlternativaId();
            List<ChequeraCuotaPagos> cuotas = allCuotasMap.getOrDefault(cuotasKey, Collections.emptyList());

            for (var cuota : cuotas) {
                if (log.isDebugEnabled()) {
                    log.debug("Cuota determinada -> {}", cuota.jsonify());
                }
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
                this.setCellString(row, 8, chequeraSerie.getArancelTipo().getDescripcion(), styleNormal);
                this.setCellString(row, 9, chequeraSerie.getHpum() == 1 ? "X" : "", styleNormal);
                this.setCellBigDecimal(row, 10, chequeraSerie.getBecaPorcentaje(), styleNormal);
                this.setCellInteger(row, 11, chequeraSerie.getAlternativaId(), styleNormal);
                this.setCellString(row, 12, cuota.getProducto().getNombre(), styleNormal);
                this.setCellInteger(row, 13, cuota.getCuotaId(), styleNormal);
                this.setCellInteger(row, 14, cuota.getAnho(), styleNormal);
                this.setCellInteger(row, 15, cuota.getMes(), styleNormal);
                if (cuota.getBaja() == 0) {
                    this.setCellBigDecimal(row, 16, cuota.getImporte1(), styleNormal);
                } else {
                    this.setCellString(row, 16, "Baja", styleNormal);
                }
                this.setCellOffsetDateTime(row, 17, cuota.getVencimiento1(), dateStyleNormal);

                if (!cuota.getChequeraPagos().isEmpty()) {
                    var pago = cuota.getChequeraPagos().getFirst();
                    if (log.isDebugEnabled()) {
                        log.debug("Pago a escribir -> {}", Jsonifier.builder(pago).build());
                    }
                    this.setCellOffsetDateTime(row, 18, pago.getFecha(), dateStyleNormal);
                    this.setCellString(row, 19, pago.getTipoPago().getNombre(), styleNormal);
                    this.setCellBigDecimal(row, 20, pago.getImporte(), styleNormal);
                    this.setCellString(row, 21, pago.getIdMercadoPago(), styleNormal);
                }
            }
        }

        // Optimización de autoSize
        for (int column = 0; column < sheet.getRow(0).getPhysicalNumberOfCells(); column++) {
            sheet.setColumnWidth(column, 4000);
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

    private void setCellOffsetDateTime(Row row, int column, OffsetDateTime value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(new Date(value.toInstant().toEpochMilli()));
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
