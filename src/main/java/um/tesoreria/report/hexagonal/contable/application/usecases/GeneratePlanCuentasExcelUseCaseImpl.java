package um.tesoreria.report.hexagonal.contable.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.contable.domain.model.CuentaPlan;
import um.tesoreria.report.hexagonal.contable.domain.ports.in.GeneratePlanCuentasExcelUseCase;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.PlanCuentasRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Arma el Excel del plan de cuentas con lo que informa el core: número de cuenta, nombre, sede, grado,
 * integradora y visible. No calcula ni valida nada del plan; solo lo ordena por número y lo presenta.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratePlanCuentasExcelUseCaseImpl implements GeneratePlanCuentasExcelUseCase {

    static final String HOJA = "Plan de cuentas";
    static final String[] ENCABEZADOS = {"Número de cuenta", "Nombre", "Sede", "Grado", "Integradora", "Visible"};
    // Ancho en caracteres de cada columna; fijo para no depender de las fuentes del servidor (autoSizeColumn).
    private static final int[] ANCHOS = {18, 60, 24, 8, 13, 10};

    private final PlanCuentasRepository repository;

    @Override
    public byte[] generate() {
        log.debug("Processing GeneratePlanCuentasExcelUseCaseImpl.generate");
        List<CuentaPlan> cuentas = repository.findAllCuentas().stream()
                .sorted(Comparator.comparing(CuentaPlan::numeroCuenta, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        Map<Integer, String> sedes = repository.findNombresSedes();

        try (Workbook book = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            CellStyle negrita = book.createCellStyle();
            Font font = book.createFont();
            font.setBold(true);
            negrita.setFont(font);

            Sheet sheet = book.createSheet(HOJA);
            Row encabezado = sheet.createRow(0);
            for (int i = 0; i < ENCABEZADOS.length; i++) {
                Cell cell = encabezado.createCell(i);
                cell.setCellValue(ENCABEZADOS[i]);
                cell.setCellStyle(negrita);
                sheet.setColumnWidth(i, ANCHOS[i] * 256);
            }

            int fila = 1;
            for (CuentaPlan cuenta : cuentas) {
                Row row = sheet.createRow(fila++);
                // El número va como texto: son 11 dígitos y Excel los pasaría a notación científica.
                row.createCell(0).setCellValue(cuenta.numeroCuenta() == null ? "" : cuenta.numeroCuenta().toPlainString());
                row.createCell(1).setCellValue(cuenta.nombre() == null ? "" : cuenta.nombre());
                row.createCell(2).setCellValue(sede(cuenta.geograficaId(), sedes));
                if (cuenta.grado() != null) {
                    row.createCell(3).setCellValue(cuenta.grado());
                }
                row.createCell(4).setCellValue(siNo(cuenta.integradora()));
                row.createCell(5).setCellValue(siNo(cuenta.visible()));
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, Math.max(fila - 1, 0), 0, ENCABEZADOS.length - 1));

            book.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el Excel del plan de cuentas", e);
        }
    }

    private static String sede(Integer geograficaId, Map<Integer, String> sedes) {
        if (geograficaId == null || geograficaId == 0) {
            return "";
        }
        String nombre = sedes.get(geograficaId);
        return nombre != null ? nombre : String.valueOf(geograficaId);
    }

    private static String siNo(Byte valor) {
        if (valor == null) {
            return "";
        }
        return valor == 0 ? "No" : "Sí";
    }
}