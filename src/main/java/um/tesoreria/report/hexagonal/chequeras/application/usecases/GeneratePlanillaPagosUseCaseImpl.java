package um.tesoreria.report.hexagonal.chequeras.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaPagosUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.ChequeraPagoRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratePlanillaPagosUseCaseImpl implements GeneratePlanillaPagosUseCase {

    private final Environment environment;
    private final ChequeraPagoRepository chequeraPagoRepository;

    @Override
    public String generatePlanillaPagos(Integer facultadId, Integer tipoChequeraId, Integer lectivoId) {
        log.debug("Processing GeneratePlanillaPagosUseCaseImpl.generatePlanillaPagos");
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

        for (var chequeraPago : chequeraPagoRepository.findAllByFacultadIdAndTipoChequeraIdAndLectivoId(facultadId, tipoChequeraId, lectivoId)) {
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

        for (int column = 0; column < sheet.getRow(0).getPhysicalNumberOfCells(); column++) {
            sheet.autoSizeColumn(column);
        }

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
}
