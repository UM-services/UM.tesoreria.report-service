package um.tesoreria.report.hexagonal.contable.application.usecases;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import um.tesoreria.report.hexagonal.contable.domain.model.CuentaPlan;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.PlanCuentasRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeneratePlanCuentasExcelUseCaseImplTest {

    private final PlanCuentasRepository repository = mock(PlanCuentasRepository.class);
    private final GeneratePlanCuentasExcelUseCaseImpl useCase = new GeneratePlanCuentasExcelUseCaseImpl(repository);

    private static CuentaPlan cuenta(String numero, String nombre, Integer grado, Integer integradora, Integer visible,
                                     Integer sede) {
        return new CuentaPlan(new BigDecimal(numero), nombre, grado,
                integradora == null ? null : integradora.byteValue(),
                visible == null ? null : visible.byteValue(), sede);
    }

    private XSSFSheet hoja(byte[] contenido) throws IOException {
        // El libro queda abierto: los tests solo leen y la JVM lo libera al terminar.
        return new XSSFWorkbook(new ByteArrayInputStream(contenido)).getSheet(GeneratePlanCuentasExcelUseCaseImpl.HOJA);
    }

    private static String texto(XSSFSheet sheet, int fila, int columna) {
        return sheet.getRow(fila).getCell(columna).getStringCellValue();
    }

    // protects=el Excel trae las columnas pedidas (número, nombre, sede) más grado, integradora y visible, en ese orden; fails_when=falta una columna, cambia el orden o se pierde la hoja; no_claim=no prueba el aspecto visual en Excel
    @Test
    void generate_writesTheExpectedHeaders() throws IOException {
        when(repository.findAllCuentas()).thenReturn(List.of());
        when(repository.findNombresSedes()).thenReturn(Map.of());

        XSSFSheet sheet = hoja(useCase.generate());

        assertThat(sheet).isNotNull();
        assertThat(List.of(texto(sheet, 0, 0), texto(sheet, 0, 1), texto(sheet, 0, 2), texto(sheet, 0, 3),
                texto(sheet, 0, 4), texto(sheet, 0, 5)))
                .containsExactly("Número de cuenta", "Nombre", "Sede", "Grado", "Integradora", "Visible");
    }

    // protects=las cuentas salen por número ascendente aunque el core las mande desordenadas y el número queda como texto de 11 dígitos; fails_when=se respeta el orden de entrada o el número pasa a numérico y Excel lo muestra en notación científica; no_claim=no prueba la jerarquía (la calcula el core)
    @Test
    void generate_sortsByNumeroAndKeepsTheNumeroAsText() throws IOException {
        when(repository.findAllCuentas()).thenReturn(List.of(
                cuenta("40101010000", "Ingresos", 3, 0, 1, null),
                cuenta("10101010000", "Caja", 3, 0, 1, null),
                cuenta("10100000000", "Activo corriente", 1, 1, 1, null)));
        when(repository.findNombresSedes()).thenReturn(Map.of());

        XSSFSheet sheet = hoja(useCase.generate());

        assertThat(List.of(texto(sheet, 1, 0), texto(sheet, 2, 0), texto(sheet, 3, 0)))
                .containsExactly("10100000000", "10101010000", "40101010000");
        assertThat(sheet.getRow(1).getCell(0).getCellType()).isEqualTo(CellType.STRING);
        assertThat(texto(sheet, 1, 1)).isEqualTo("Activo corriente");
        assertThat(sheet.getRow(1).getCell(3).getNumericCellValue()).isEqualTo(1.0);
    }

    // protects=la sede sale con su nombre, vacía si la cuenta no tiene sede (null o 0) y con el id si el core no la lista; fails_when=aparece el id en vez del nombre, "null" o "0"; no_claim=no prueba que el core devuelva todas las sedes
    @Test
    void generate_resolvesTheSedeName() throws IOException {
        when(repository.findAllCuentas()).thenReturn(List.of(
                cuenta("10101010000", "Con sede", 3, 0, 1, 1),
                cuenta("10101020000", "Sin sede null", 3, 0, 1, null),
                cuenta("10101030000", "Sin sede cero", 3, 0, 1, 0),
                cuenta("10101040000", "Sede desconocida", 3, 0, 1, 9)));
        when(repository.findNombresSedes()).thenReturn(Map.of(1, "Mendoza"));

        XSSFSheet sheet = hoja(useCase.generate());

        assertThat(List.of(texto(sheet, 1, 2), texto(sheet, 2, 2), texto(sheet, 3, 2), texto(sheet, 4, 2)))
                .containsExactly("Mendoza", "", "", "9");
    }

    // protects=integradora y visible salen como Sí/No y vacío si el core no los informa; fails_when=se muestran 1/0 o "null", o un dato ausente se lee como "No"; no_claim=no prueba qué significa visible para tesorería
    @Test
    void generate_rendersFlagsAsSiNo() throws IOException {
        when(repository.findAllCuentas()).thenReturn(List.of(
                cuenta("10100000000", "Integradora visible", 1, 1, 1, null),
                cuenta("10101010000", "Detalle oculta", 3, 0, 0, null),
                cuenta("10101020000", "Sin datos", null, null, null, null)));
        when(repository.findNombresSedes()).thenReturn(Map.of());

        XSSFSheet sheet = hoja(useCase.generate());

        assertThat(List.of(texto(sheet, 1, 4), texto(sheet, 1, 5))).containsExactly("Sí", "Sí");
        assertThat(List.of(texto(sheet, 2, 4), texto(sheet, 2, 5))).containsExactly("No", "No");
        assertThat(List.of(texto(sheet, 3, 4), texto(sheet, 3, 5))).containsExactly("", "");
        assertThat(sheet.getRow(3).getCell(3)).isNull();
    }

    // protects=la fila de títulos queda fija y con filtro sobre todos los datos, para filtrar por sede en Excel; fails_when=falta el filtro, no abarca las filas o los títulos se pierden al bajar; no_claim=no prueba el filtro desde la pantalla (el Excel siempre trae el plan completo)
    @Test
    void generate_freezesTheHeaderAndAddsAnAutoFilter() throws IOException {
        when(repository.findAllCuentas()).thenReturn(List.of(
                cuenta("10100000000", "A", 1, 1, 1, null),
                cuenta("10101010000", "B", 3, 0, 1, null)));
        when(repository.findNombresSedes()).thenReturn(Map.of());

        XSSFSheet sheet = hoja(useCase.generate());

        var panel = sheet.getCTWorksheet().getSheetViews().getSheetViewArray(0).getPane();
        assertThat(panel.getYSplit()).isEqualTo(1.0);
        assertThat(panel.getXSplit()).isZero();
        assertThat(sheet.getCTWorksheet().getAutoFilter().getRef()).isEqualTo("A1:F3");
    }
}