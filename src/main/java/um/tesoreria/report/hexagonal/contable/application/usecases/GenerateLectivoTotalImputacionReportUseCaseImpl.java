package um.tesoreria.report.hexagonal.contable.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.LectivoRepository;
import um.tesoreria.report.hexagonal.contable.domain.model.LectivoTotalImputacion;
import um.tesoreria.report.hexagonal.contable.domain.ports.in.GenerateLectivoTotalImputacionReportUseCase;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.LectivoTotalImputacionRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateLectivoTotalImputacionReportUseCaseImpl implements GenerateLectivoTotalImputacionReportUseCase {

    private final Environment environment;
    private final LectivoTotalImputacionRepository repository;
    private final LectivoRepository lectivoRepository;

    @Override
    public String generateReport(Integer lectivoId) {
        log.debug("Processing GenerateLectivoTotalImputacionReportUseCaseImpl.generateReport for lectivoId: {}", lectivoId);
        String path = environment.getProperty("path.reports");

        String filename = path + MessageFormat.format("lectivototalimputacion.{0}.xlsx", String.valueOf(lectivoId));

        Workbook book = new XSSFWorkbook();
        CellStyle styleNormal = book.createCellStyle();
        Font fontNormal = book.createFont();
        fontNormal.setBold(false);
        styleNormal.setFont(fontNormal);

        CellStyle styleBold = book.createCellStyle();
        Font fontBold = book.createFont();
        fontBold.setBold(true);
        styleBold.setFont(fontBold);

        String sheetName = "Imputaciones";
        try {
            var lectivo = lectivoRepository.findByLectivoId(lectivoId);
            if (lectivo != null && lectivo.getNombre() != null) {
                sheetName = lectivo.getNombre();
            }
        } catch (Exception e) {
            log.warn("Could not retrieve lectivo details for sheet name fallback to default", e);
        }

        Sheet sheet = book.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        setCellString(headerRow, 0, "Facultad", styleBold);
        setCellString(headerRow, 1, "Tipo Chequera", styleBold);
        setCellString(headerRow, 2, "Geográfica", styleBold);
        setCellString(headerRow, 3, "Producto", styleBold);
        setCellString(headerRow, 4, "Cuenta Contable", styleBold);
        setCellString(headerRow, 5, "Nombre Cuenta", styleBold);

        List<LectivoTotalImputacion> imputaciones = repository.findAllByLectivo(lectivoId);

        int rowIndex = 1;
        for (LectivoTotalImputacion item : imputaciones) {
            Row row = sheet.createRow(rowIndex++);

            String facultadVal = "";
            if (item.getFacultad() != null && item.getFacultad().getNombre() != null) {
                facultadVal = item.getFacultad().getNombre();
            } else if (item.getFacultadId() != null) {
                facultadVal = String.valueOf(item.getFacultadId());
            }

            String tipoChequeraVal = "";
            if (item.getTipoChequera() != null && item.getTipoChequera().getNombre() != null) {
                tipoChequeraVal = item.getTipoChequera().getNombre();
            } else if (item.getTipoChequeraId() != null) {
                tipoChequeraVal = String.valueOf(item.getTipoChequeraId());
            }

            String geograficaVal = "";
            if (item.getTipoChequera() != null) {
                if (item.getTipoChequera().getGeografica() != null && item.getTipoChequera().getGeografica().getNombre() != null) {
                    geograficaVal = item.getTipoChequera().getGeografica().getNombre();
                } else if (item.getTipoChequera().getGeograficaId() != null) {
                    geograficaVal = String.valueOf(item.getTipoChequera().getGeograficaId());
                }
            }

            String productoVal = "";
            if (item.getProducto() != null && item.getProducto().getNombre() != null) {
                productoVal = item.getProducto().getNombre();
            } else if (item.getProductoId() != null) {
                productoVal = String.valueOf(item.getProductoId());
            }

            String cuentaContableVal = "";
            if (item.getNumeroCuenta() != null) {
                cuentaContableVal = item.getNumeroCuenta().toPlainString();
            }

            String nombreCuentaVal = "";
            if (item.getCuenta() != null && item.getCuenta().getNombre() != null) {
                nombreCuentaVal = item.getCuenta().getNombre();
            }

            setCellString(row, 0, facultadVal, styleNormal);
            setCellString(row, 1, tipoChequeraVal, styleNormal);
            setCellString(row, 2, geograficaVal, styleNormal);
            setCellString(row, 3, productoVal, styleNormal);
            setCellString(row, 4, cuentaContableVal, styleNormal);
            setCellString(row, 5, nombreCuentaVal, styleNormal);
        }

        // Auto size columns
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        try {
            File file = new File(filename);
            // Ensure parent directories exist
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileOutputStream output = new FileOutputStream(file)) {
                book.write(output);
                output.flush();
            }
            book.close();
        } catch (Exception e) {
            log.error("Error writing Excel report for LectivoTotalImputacion", e);
        }

        return filename;
    }

    private void setCellString(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
