package um.tesoreria.report.hexagonal.chequeras.application.usecases;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.Environment;
import um.tesoreria.report.hexagonal.chequeras.domain.model.Borrador;
import um.tesoreria.report.hexagonal.chequeras.domain.model.BorradorAlternativa;
import um.tesoreria.report.hexagonal.chequeras.domain.model.BorradorArancelTipo;
import um.tesoreria.report.hexagonal.chequeras.domain.model.BorradorCuota;
import um.tesoreria.report.hexagonal.chequeras.domain.model.Carrera;
import um.tesoreria.report.hexagonal.chequeras.domain.model.Legajo;
import um.tesoreria.report.hexagonal.chequeras.domain.model.Plan;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.BorradorRepository;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.LegajoRepository;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeneratePlanillaBorradorUseCaseImplTest {

    private final Environment environment = mock(Environment.class);
    private final BorradorRepository borradorRepository = mock(BorradorRepository.class);
    private final LegajoRepository legajoRepository = mock(LegajoRepository.class);
    private final GeneratePlanillaBorradorUseCaseImpl useCase =
            new GeneratePlanillaBorradorUseCaseImpl(environment, borradorRepository, legajoRepository);

    @TempDir
    Path directorio;

    // protects=el layout horizontal propio de borradores (sin pagos, con original y borrador) y el total por fila;
    // fails_when=reusa columnas de chequeras, omite el importe original o no resuelve carrera/arancel/alternativa;
    // no_claim=no valida estilos ni anchos de columna.
    @Test
    void generaUnaFilaPorBorradorConImportesOriginalYBorrador() throws Exception {
        when(environment.getProperty("path.reports", "/tmp/")).thenReturn(directorio.toString());
        when(borradorRepository.findAllBySede(14, 38, 1)).thenReturn(List.of(borrador()));
        when(legajoRepository.findAllByFacultadId(14)).thenReturn(List.of(legajo()));

        String archivo = useCase.generatePlanillaBorrador(14, 38, 1);

        assertThat(Path.of(archivo)).exists();
        try (Workbook book = WorkbookFactory.create(new File(archivo))) {
            Sheet sheet = book.getSheetAt(0);
            Row encabezado = sheet.getRow(0);
            assertThat(encabezado.getCell(0).getStringCellValue()).isEqualTo("Borrador");
            assertThat(encabezado.getCell(2).getStringCellValue()).isEqualTo("DU");
            assertThat(encabezado.getCell(12).getStringCellValue()).isEqualTo("Alternativa");
            assertThat(encabezado.getCell(13).getStringCellValue()).isEqualTo("3/2027 Producto");
            assertThat(encabezado.getCell(14).getStringCellValue()).isEqualTo("3/2027 Importe original");
            assertThat(encabezado.getCell(15).getStringCellValue()).isEqualTo("3/2027 Importe borrador");
            assertThat(encabezado.getCell(16).getStringCellValue()).isEqualTo("3/2027 Vencimiento");
            assertThat(encabezado.getCell(21).getStringCellValue()).isEqualTo("Total original");
            assertThat(encabezado.getCell(22).getStringCellValue()).isEqualTo("Total borrador");
            for (int columna = 0; columna < encabezado.getLastCellNum(); columna++) {
                String titulo = encabezado.getCell(columna) == null ? "" : encabezado.getCell(columna).getStringCellValue();
                assertThat(titulo).doesNotContain("Pago").doesNotContain("id MP");
            }

            Row fila = sheet.getRow(1);
            assertThat(fila.getCell(0).getStringCellValue()).isEqualTo("B-7");
            assertThat(fila.getCell(1).getStringCellValue()).isEqualTo("ABIERTO");
            assertThat(fila.getCell(2).getNumericCellValue()).isEqualTo(1234567d);
            assertThat(fila.getCell(3).getStringCellValue()).isEqualTo("Pérez, Ana");
            assertThat(fila.getCell(4).getStringCellValue()).isEqualTo("Facultad de Ciencias Médicas");
            assertThat(fila.getCell(5).getStringCellValue()).isEqualTo("Mendoza");
            assertThat(fila.getCell(6).getStringCellValue()).isEqualTo("Plan 2018/Ingeniería");
            assertThat(fila.getCell(7).getNumericCellValue()).isEqualTo(1d);
            assertThat(fila.getCell(8).getStringCellValue()).isEqualTo("Chequera");
            assertThat(fila.getCell(9).getStringCellValue()).isEqualTo("Arancel completo");
            assertThat(fila.getCell(10).getStringCellValue()).isEqualTo("X");
            assertThat(fila.getCell(11).getNumericCellValue()).isEqualTo(25d);
            assertThat(fila.getCell(12).getStringCellValue()).isEqualTo("Cuota completa");
            assertThat(fila.getCell(13).getStringCellValue()).isEqualTo("Cuota completa");
            assertThat(fila.getCell(14).getNumericCellValue()).isEqualTo(1000.50d);
            assertThat(fila.getCell(15).getNumericCellValue()).isEqualTo(900.00d);
            assertThat(fila.getCell(21).getNumericCellValue()).isEqualTo(1500.50d);
            assertThat(fila.getCell(22).getNumericCellValue()).isEqualTo(1400.00d);
        }
    }

    private Borrador borrador() {
        return Borrador.builder()
                .borradorId(7L)
                .estado("ABIERTO")
                .personaId(new BigDecimal("1234567"))
                .documentoId(1)
                .personaApellido("Pérez")
                .personaNombre("Ana")
                .facultadId(14)
                .facultadNombre("Facultad de Ciencias Médicas")
                .geograficaId(1)
                .geograficaNombre("Mendoza")
                .lectivoId(38)
                .lectivoNombre("Lectivo 2027 - 2028")
                .tipoChequeraId(1)
                .tipoChequeraNombre("Chequera")
                .arancelTipoId(2)
                .alternativaId(1)
                .cursoId(1)
                .hpum((byte) 1)
                .becaPorcentaje(new BigDecimal("25"))
                .cuotas(List.of(
                        BorradorCuota.builder().productoId(1).alternativaId(1).cuotaId(1).mes(3).anio(2027)
                                .vencimiento1(OffsetDateTime.parse("2027-03-15T00:00:00Z"))
                                .importe1Original(new BigDecimal("1000.50"))
                                .importe1Borrador(new BigDecimal("900.00")).pagado((byte) 0).build(),
                        BorradorCuota.builder().productoId(1).alternativaId(1).cuotaId(2).mes(4).anio(2027)
                                .vencimiento1(OffsetDateTime.parse("2027-04-15T00:00:00Z"))
                                .importe1Original(new BigDecimal("500.00"))
                                .importe1Borrador(new BigDecimal("500.00")).pagado((byte) 0).build()))
                .alternativas(List.of(BorradorAlternativa.builder().alternativaId(1).productoId(1)
                        .titulo("Cuota completa").build()))
                .arancelTipos(List.of(BorradorArancelTipo.builder().arancelTipoId(2)
                        .nombre("Arancel completo").build()))
                .build();
    }

    private Legajo legajo() {
        return Legajo.builder()
                .personaId(new BigDecimal("1234567"))
                .documentoId(1)
                .facultadId(14)
                .carrera(Carrera.builder().nombre("Ingeniería")
                        .plan(Plan.builder().nombre("Plan 2018").build()).build())
                .build();
    }
}
