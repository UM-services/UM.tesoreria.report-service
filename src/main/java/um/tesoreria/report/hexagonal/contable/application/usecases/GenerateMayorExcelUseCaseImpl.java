package um.tesoreria.report.hexagonal.contable.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.contable.domain.model.Mayor;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorCuenta;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorMovimiento;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorSaldoInicial;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorSeleccion;
import um.tesoreria.report.hexagonal.contable.domain.ports.in.GenerateMayorExcelUseCase;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.MayorRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Arma el Excel del mayor con lo que informa el core: una sola hoja con todas las cuentas, y la cuenta y el
 * ejercicio en cada línea. No calcula nada: saldo inicial, saldo acumulado, totales y saldo final se escriben tal
 * como llegan, y el orden de cuentas y movimientos es el del core.
 *
 * <p>Se escribe con {@link SXSSFWorkbook} (ventana de filas en memoria) para que un mayor grande no cargue el libro
 * entero. El tope real de memoria sigue siendo el JSON que devuelve el core.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateMayorExcelUseCaseImpl implements GenerateMayorExcelUseCase {

    static final String HOJA = "Mayor";
    static final int FILA_ENCABEZADO = 3;
    static final String[] ENCABEZADOS = {"Cuenta", "Nombre de cuenta", "Ejercicio", "Fecha", "Asiento", "Comprobante",
            "Proveedor", "Concepto", "Orden Pago / Pago", "Beneficiario", "Debe", "Haber", "Saldo", "Orden Pago",
            "Valor", "Numero", "Importe"};
    static final String SALDO_INICIAL = "Saldo inicial";
    static final String TOTALES = "Totales";

    private static final int C_CUENTA = 0;
    private static final int C_NOMBRE = 1;
    private static final int C_EJERCICIO = 2;
    private static final int C_FECHA = 3;
    private static final int C_ASIENTO = 4;
    private static final int C_COMPROBANTE = 5;
    private static final int C_PROVEEDOR = 6;
    private static final int C_CONCEPTO = 7;
    private static final int C_ORDEN_PAGO = 8;
    private static final int C_BENEFICIARIO = 9;
    private static final int C_DEBE = 10;
    private static final int C_HABER = 11;
    private static final int C_SALDO = 12;
    private static final int C_ORDEN_PAGO_VALOR = 13;
    private static final int C_VALOR = 14;
    private static final int C_NUMERO = 15;
    private static final int C_IMPORTE = 16;

    // Ancho en caracteres de cada columna; fijo porque autoSizeColumn no sirve con filas en streaming.
    private static final int[] ANCHOS = {16, 40, 26, 12, 9, 22, 30, 40, 34, 30, 16, 16, 16, 18, 24, 12, 16};
    private static final int VENTANA_DE_FILAS = 200;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final MayorRepository repository;

    @Override
    public byte[] generate(MayorSeleccion seleccion) {
        log.debug("Processing GenerateMayorExcelUseCaseImpl.generate");
        Mayor mayor = repository.consultar(seleccion);
        List<MayorCuenta> cuentas = mayor.cuentas() == null ? List.of() : mayor.cuentas();

        try (SXSSFWorkbook book = new SXSSFWorkbook(VENTANA_DE_FILAS);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            CellStyle negrita = book.createCellStyle();
            Font fuenteNegrita = book.createFont();
            fuenteNegrita.setBold(true);
            negrita.setFont(fuenteNegrita);

            short formatoImporte = book.createDataFormat().getFormat("#,##0.00");
            CellStyle importe = book.createCellStyle();
            importe.setDataFormat(formatoImporte);
            CellStyle importeNegrita = book.createCellStyle();
            importeNegrita.setDataFormat(formatoImporte);
            importeNegrita.setFont(fuenteNegrita);

            Sheet sheet = book.createSheet(HOJA);
            sheet.createRow(0).createCell(0).setCellValue("Desde " + fecha(mayor.desde()));
            sheet.createRow(1).createCell(0).setCellValue("Hasta " + fecha(mayor.hasta()));

            Row encabezado = sheet.createRow(FILA_ENCABEZADO);
            for (int i = 0; i < ENCABEZADOS.length; i++) {
                Cell cell = encabezado.createCell(i);
                cell.setCellValue(ENCABEZADOS[i]);
                cell.setCellStyle(negrita);
                sheet.setColumnWidth(i, ANCHOS[i] * 256);
            }

            int fila = FILA_ENCABEZADO;
            for (MayorCuenta cuenta : cuentas) {
                MayorSaldoInicial inicial = cuenta.saldoInicial();
                Row filaInicial = sheet.createRow(++fila);
                identificar(filaInicial, cuenta);
                texto(filaInicial, C_CONCEPTO, SALDO_INICIAL, negrita);
                if (inicial != null) {
                    numero(filaInicial, C_DEBE, inicial.debe(), importeNegrita);
                    numero(filaInicial, C_HABER, inicial.haber(), importeNegrita);
                    numero(filaInicial, C_SALDO, inicial.saldo(), importeNegrita);
                }

                List<MayorMovimiento> movimientos = cuenta.movimientos() == null ? List.of() : cuenta.movimientos();
                for (MayorMovimiento movimiento : movimientos) {
                    Row row = sheet.createRow(++fila);
                    identificar(row, cuenta);
                    texto(row, C_EJERCICIO, movimiento.ejercicioNombre(), null);
                    texto(row, C_FECHA, fecha(movimiento.fechaContable()), null);
                    if (movimiento.ordenContable() != null) {
                        row.createCell(C_ASIENTO).setCellValue(movimiento.ordenContable());
                    }
                    texto(row, C_COMPROBANTE, movimiento.comprobante(), null);
                    texto(row, C_PROVEEDOR, movimiento.proveedor(), null);
                    texto(row, C_CONCEPTO, movimiento.concepto(), null);
                    texto(row, C_ORDEN_PAGO, movimiento.ordenPago(), null);
                    texto(row, C_BENEFICIARIO, movimiento.beneficiario(), null);
                    numero(row, C_DEBE, movimiento.debe(), importe);
                    numero(row, C_HABER, movimiento.haber(), importe);
                    numero(row, C_SALDO, movimiento.saldo(), importe);
                    texto(row, C_ORDEN_PAGO_VALOR, movimiento.ordenPagoValor(), null);
                    texto(row, C_VALOR, movimiento.valor(), null);
                    if (movimiento.numeroValor() != null) {
                        row.createCell(C_NUMERO).setCellValue(movimiento.numeroValor());
                    }
                    numero(row, C_IMPORTE, movimiento.importeValor(), importe);
                }

                Row filaTotales = sheet.createRow(++fila);
                identificar(filaTotales, cuenta);
                texto(filaTotales, C_CONCEPTO, TOTALES, negrita);
                numero(filaTotales, C_DEBE, cuenta.totalDebe(), importeNegrita);
                numero(filaTotales, C_HABER, cuenta.totalHaber(), importeNegrita);
                numero(filaTotales, C_SALDO, cuenta.saldoFinal(), importeNegrita);
            }

            sheet.createFreezePane(0, FILA_ENCABEZADO + 1);
            sheet.setAutoFilter(new CellRangeAddress(FILA_ENCABEZADO, fila, 0, ENCABEZADOS.length - 1));

            book.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el Excel del mayor", e);
        }
    }

    /** Cuenta y nombre en cada línea, para poder filtrar y ordenar la hoja sin perder a qué cuenta corresponde. */
    private static void identificar(Row row, MayorCuenta cuenta) {
        // El número va como texto: son 11 dígitos y Excel los pasaría a notación científica.
        row.createCell(C_CUENTA).setCellValue(
                cuenta.numeroCuenta() == null ? "" : cuenta.numeroCuenta().toPlainString());
        texto(row, C_NOMBRE, cuenta.nombre(), null);
    }

    private static void texto(Row row, int columna, String valor, CellStyle estilo) {
        if (valor == null) {
            return;
        }
        Cell cell = row.createCell(columna);
        cell.setCellValue(valor);
        if (estilo != null) {
            cell.setCellStyle(estilo);
        }
    }

    private static void numero(Row row, int columna, BigDecimal valor, CellStyle estilo) {
        if (valor == null) {
            return;
        }
        Cell cell = row.createCell(columna);
        cell.setCellValue(valor.doubleValue());
        cell.setCellStyle(estilo);
    }

    private static String fecha(OffsetDateTime fecha) {
        return fecha == null ? "" : FORMATO_FECHA.format(fecha.withOffsetSameInstant(ZoneOffset.UTC));
    }
}