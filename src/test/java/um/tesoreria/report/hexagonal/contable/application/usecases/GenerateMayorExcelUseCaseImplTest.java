package um.tesoreria.report.hexagonal.contable.application.usecases;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import um.tesoreria.report.hexagonal.contable.domain.model.Mayor;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorCuenta;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorMovimiento;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorSaldoInicial;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorSeleccion;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.MayorRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateMayorExcelUseCaseImplTest {

    private static final int INICIAL = GenerateMayorExcelUseCaseImpl.FILA_ENCABEZADO + 1;

    private final MayorRepository repository = mock(MayorRepository.class);
    private final GenerateMayorExcelUseCaseImpl useCase = new GenerateMayorExcelUseCaseImpl(repository);

    private static OffsetDateTime fecha(int anio, int mes, int dia) {
        return OffsetDateTime.of(anio, mes, dia, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    private static MayorSeleccion seleccion() {
        return new MayorSeleccion(fecha(2026, 1, 1), fecha(2026, 4, 30), List.of(new BigDecimal("10101010001")), null, null);
    }

    private static MayorMovimiento movimiento(OffsetDateTime fecha, int ejercicioId, String ejercicio, String debe,
                                              String haber, String saldo) {
        return new MayorMovimiento(fecha, 7, ejercicioId, ejercicio, "Factura", "Proveedor SA", "Compra", null, null,
                debe == null ? null : new BigDecimal(debe), haber == null ? null : new BigDecimal(haber),
                new BigDecimal(saldo), null, null, null, null);
    }

    private static MayorCuenta cuenta(String numero, String nombre, List<MayorMovimiento> movimientos) {
        return new MayorCuenta(new BigDecimal(numero), nombre,
                new MayorSaldoInicial(new BigDecimal("100.00"), new BigDecimal("40.00"), new BigDecimal("60.00")),
                movimientos, new BigDecimal("50.00"), new BigDecimal("30.00"), new BigDecimal("80.00"));
    }

    private XSSFSheet hoja(byte[] contenido) throws IOException {
        // El libro queda abierto: los tests solo leen y la JVM lo libera al terminar.
        return new XSSFWorkbook(new ByteArrayInputStream(contenido)).getSheet(GenerateMayorExcelUseCaseImpl.HOJA);
    }

    private static String texto(XSSFSheet sheet, int fila, int columna) {
        Cell cell = sheet.getRow(fila).getCell(columna);
        return cell == null ? null : cell.getStringCellValue();
    }

    private static Double numero(XSSFSheet sheet, int fila, int columna) {
        Cell cell = sheet.getRow(fila).getCell(columna);
        return cell == null ? null : cell.getNumericCellValue();
    }

    // protects=el Excel trae el rango pedido arriba y las 17 columnas en orden, incluidas Cuenta y Ejercicio; fails_when=falta una columna, cambia el orden o se pierde la hoja; no_claim=no prueba el aspecto en Excel
    @Test
    void generate_writesRangeAndHeaders() throws IOException {
        when(repository.consultar(seleccion())).thenReturn(new Mayor(fecha(2026, 1, 1), fecha(2026, 4, 30), List.of()));

        XSSFSheet sheet = hoja(useCase.generate(seleccion()));

        assertThat(texto(sheet, 0, 0)).isEqualTo("Desde 01-01-2026");
        assertThat(texto(sheet, 1, 0)).isEqualTo("Hasta 30-04-2026");
        assertThat(sheet.getRow(GenerateMayorExcelUseCaseImpl.FILA_ENCABEZADO).getLastCellNum())
                .isEqualTo((short) GenerateMayorExcelUseCaseImpl.ENCABEZADOS.length);
        assertThat(texto(sheet, GenerateMayorExcelUseCaseImpl.FILA_ENCABEZADO, 0)).isEqualTo("Cuenta");
        assertThat(texto(sheet, GenerateMayorExcelUseCaseImpl.FILA_ENCABEZADO, 2)).isEqualTo("Ejercicio");
        assertThat(texto(sheet, GenerateMayorExcelUseCaseImpl.FILA_ENCABEZADO, 12)).isEqualTo("Saldo");
    }

    // protects=cada línea lleva su cuenta y su ejercicio, hay una fila de saldo inicial y otra de totales por cuenta, y el número de cuenta queda como texto de 11 dígitos; fails_when=una línea pierde la cuenta o el ejercicio, falta el inicial o los totales, o el número pasa a numérico (notación científica); no_claim=no prueba el formato visual de los importes
    @Test
    void generate_identifiesAccountAndExerciseOnEveryLine() throws IOException {
        when(repository.consultar(seleccion())).thenReturn(new Mayor(fecha(2026, 1, 1), fecha(2026, 4, 30), List.of(
                cuenta("10101010001", "Caja", List.of(
                        movimiento(fecha(2026, 2, 27), 25, "Ejercicio Económico 2025", "50.00", null, "110.00"),
                        movimiento(fecha(2026, 3, 2), 26, "Ejercicio Económico 2026", null, "30.00", "80.00"))))));

        XSSFSheet sheet = hoja(useCase.generate(seleccion()));

        assertThat(texto(sheet, INICIAL, 0)).isEqualTo("10101010001");
        assertThat(sheet.getRow(INICIAL).getCell(0).getCellType()).isEqualTo(CellType.STRING);
        assertThat(texto(sheet, INICIAL, 7)).isEqualTo(GenerateMayorExcelUseCaseImpl.SALDO_INICIAL);
        for (int fila = INICIAL + 1; fila <= INICIAL + 2; fila++) {
            assertThat(texto(sheet, fila, 0)).isEqualTo("10101010001");
            assertThat(texto(sheet, fila, 1)).isEqualTo("Caja");
        }
        assertThat(texto(sheet, INICIAL + 1, 2)).isEqualTo("Ejercicio Económico 2025");
        assertThat(texto(sheet, INICIAL + 2, 2)).isEqualTo("Ejercicio Económico 2026");
        assertThat(texto(sheet, INICIAL + 1, 3)).isEqualTo("27-02-2026");
        assertThat(texto(sheet, INICIAL + 3, 7)).isEqualTo(GenerateMayorExcelUseCaseImpl.TOTALES);
        assertThat(texto(sheet, INICIAL + 3, 0)).isEqualTo("10101010001");
    }

    // protects=saldo inicial, saldo de cada línea, totales y saldo final se escriben tal como los manda el core aunque no cierren entre sí; fails_when=el report recalcula, redondea o corrige algún importe; no_claim=no prueba que los importes del core sean correctos
    @Test
    void generate_writesTheCoreAmountsWithoutRecalculating() throws IOException {
        when(repository.consultar(seleccion())).thenReturn(new Mayor(fecha(2026, 1, 1), fecha(2026, 4, 30), List.of(
                cuenta("10101010001", "Caja", List.of(
                        movimiento(fecha(2026, 3, 2), 26, "Ejercicio Económico 2026", "50.00", null, "999.99"))))));

        XSSFSheet sheet = hoja(useCase.generate(seleccion()));

        assertThat(numero(sheet, INICIAL, 10)).isEqualTo(100.00);
        assertThat(numero(sheet, INICIAL, 11)).isEqualTo(40.00);
        assertThat(numero(sheet, INICIAL, 12)).isEqualTo(60.00);
        assertThat(numero(sheet, INICIAL + 1, 10)).isEqualTo(50.00);
        assertThat(sheet.getRow(INICIAL + 1).getCell(11)).isNull();
        assertThat(numero(sheet, INICIAL + 1, 12)).isEqualTo(999.99);
        assertThat(numero(sheet, INICIAL + 2, 10)).isEqualTo(50.00);
        assertThat(numero(sheet, INICIAL + 2, 11)).isEqualTo(30.00);
        assertThat(numero(sheet, INICIAL + 2, 12)).isEqualTo(80.00);
    }

    // protects=varias cuentas van en una sola hoja y se respeta el orden en que las manda el core; fails_when=se reordenan, se separan en hojas o se pierde alguna; no_claim=no prueba el orden por número (lo decide el core)
    @Test
    void generate_keepsSeveralAccountsInOneSheetInCoreOrder() throws IOException {
        when(repository.consultar(seleccion())).thenReturn(new Mayor(fecha(2026, 1, 1), fecha(2026, 4, 30), List.of(
                cuenta("10101010001", "Caja", List.of()),
                cuenta("10102010001", "Banco", List.of()))));

        byte[] contenido = useCase.generate(seleccion());
        XSSFSheet sheet = hoja(contenido);

        assertThat(sheet.getWorkbook().getNumberOfSheets()).isEqualTo(1);
        // por cuenta: inicial + totales (sin movimientos)
        assertThat(texto(sheet, INICIAL, 1)).isEqualTo("Caja");
        assertThat(texto(sheet, INICIAL + 1, 7)).isEqualTo(GenerateMayorExcelUseCaseImpl.TOTALES);
        assertThat(texto(sheet, INICIAL + 2, 1)).isEqualTo("Banco");
        assertThat(texto(sheet, INICIAL + 3, 7)).isEqualTo(GenerateMayorExcelUseCaseImpl.TOTALES);
    }

    // protects=un movimiento sin orden de pago ni valor deja esas celdas vacías, nunca el texto "null"; fails_when=aparece "null" o falla con NullPointerException; no_claim=no prueba el armado de la orden de pago (viene hecho del core)
    @Test
    void generate_leavesOptionalCellsEmpty() throws IOException {
        when(repository.consultar(seleccion())).thenReturn(new Mayor(fecha(2026, 1, 1), fecha(2026, 4, 30), List.of(
                cuenta("10101010001", "Caja", List.of(
                        movimiento(fecha(2026, 3, 2), 26, "Ejercicio Económico 2026", "50.00", null, "110.00"))))));

        XSSFSheet sheet = hoja(useCase.generate(seleccion()));

        int fila = INICIAL + 1;
        for (int columna : new int[]{8, 9, 13, 14, 15, 16}) {
            assertThat(sheet.getRow(fila).getCell(columna)).as("columna %d", columna).isNull();
        }
    }

    // protects=un movimiento con orden de pago y con valor vuelca esos datos en sus columnas; fails_when=se pierde algún dato o cambia de columna; no_claim=no prueba cómo el core arma esos textos
    @Test
    void generate_writesPaymentOrderAndValueWhenPresent() throws IOException {
        MayorMovimiento conDatos = new MayorMovimiento(fecha(2026, 3, 2), 7, 26, "Ejercicio Económico 2026", "Factura",
                "Proveedor SA", "Compra", "Orden de Pago / 1 / 55", "Juan Pérez", null, new BigDecimal("30.00"),
                new BigDecimal("80.00"), "1/56", "Cheque", 12345L, new BigDecimal("30.00"));
        when(repository.consultar(seleccion())).thenReturn(new Mayor(fecha(2026, 1, 1), fecha(2026, 4, 30),
                List.of(cuenta("10101010001", "Caja", List.of(conDatos)))));

        XSSFSheet sheet = hoja(useCase.generate(seleccion()));

        int fila = INICIAL + 1;
        assertThat(texto(sheet, fila, 8)).isEqualTo("Orden de Pago / 1 / 55");
        assertThat(texto(sheet, fila, 9)).isEqualTo("Juan Pérez");
        assertThat(texto(sheet, fila, 13)).isEqualTo("1/56");
        assertThat(texto(sheet, fila, 14)).isEqualTo("Cheque");
        assertThat(numero(sheet, fila, 15)).isEqualTo(12345.0);
        assertThat(numero(sheet, fila, 16)).isEqualTo(30.00);
    }

    // protects=el report consulta al core con exactamente la selección recibida, sin completarla ni validarla; fails_when=se altera la lista, el rango de cuentas o las fechas; no_claim=no prueba la validación (es del core)
    @Test
    void generate_passesTheSelectionToTheCoreUnchanged() {
        MayorSeleccion porRango = new MayorSeleccion(fecha(2026, 3, 1), fecha(2026, 3, 31), null,
                new BigDecimal("10100000000"), new BigDecimal("10199999999"));
        when(repository.consultar(porRango)).thenReturn(new Mayor(fecha(2026, 3, 1), fecha(2026, 3, 31), List.of()));

        useCase.generate(porRango);

        verify(repository).consultar(porRango);
    }
}