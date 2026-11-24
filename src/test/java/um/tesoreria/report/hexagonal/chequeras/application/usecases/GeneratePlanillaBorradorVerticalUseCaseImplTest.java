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

class GeneratePlanillaBorradorVerticalUseCaseImplTest {

    private final Environment environment = mock(Environment.class);
    private final BorradorRepository borradorRepository = mock(BorradorRepository.class);
    private final LegajoRepository legajoRepository = mock(LegajoRepository.class);
    private final GeneratePlanillaBorradorVerticalUseCaseImpl useCase =
            new GeneratePlanillaBorradorVerticalUseCaseImpl(environment, borradorRepository, legajoRepository);

    @TempDir
    Path directorio;

    // protects=el layout vertical propio de borradores: una fila por cuota, importes original/borrador, tres
    // vencimientos y flags; fails_when=cambia el orden de columnas o inventa columnas de pagos;
    // no_claim=no valida estilos ni anchos.
    @Test
    void generaUnaFilaPorCuotaConImportesVencimientosYFlags() throws Exception {
        when(environment.getProperty("path.reports", "/tmp/")).thenReturn(directorio.toString());
        when(borradorRepository.findAllBySede(14, 38, 1)).thenReturn(List.of(borrador()));
        when(legajoRepository.findAllByFacultadId(14)).thenReturn(List.of());

        String archivo = useCase.generatePlanillaBorrador(14, 38, 1);

        try (Workbook book = WorkbookFactory.create(new File(archivo))) {
            Sheet sheet = book.getSheetAt(0);
            Row encabezado = sheet.getRow(0);
            assertThat(encabezado.getCell(0).getStringCellValue()).isEqualTo("Borrador");
            assertThat(encabezado.getCell(12).getStringCellValue()).isEqualTo("Alternativa");
            assertThat(encabezado.getCell(13).getStringCellValue()).isEqualTo("Producto");
            assertThat(encabezado.getCell(14).getStringCellValue()).isEqualTo("Cuota");
            assertThat(encabezado.getCell(17).getStringCellValue()).isEqualTo("Importe original");
            assertThat(encabezado.getCell(18).getStringCellValue()).isEqualTo("Importe borrador");
            assertThat(encabezado.getCell(19).getStringCellValue()).isEqualTo("Vencimiento 1");
            assertThat(encabezado.getCell(22).getStringCellValue()).isEqualTo("Pagado");
            assertThat(encabezado.getCell(25).getStringCellValue()).isEqualTo("Bloqueada");
            for (int columna = 0; columna < encabezado.getLastCellNum(); columna++) {
                String titulo = encabezado.getCell(columna) == null ? "" : encabezado.getCell(columna).getStringCellValue();
                assertThat(titulo).doesNotContain("Pago").doesNotContain("id MP");
            }

            assertThat(sheet.getLastRowNum()).isEqualTo(2);
            Row fila = sheet.getRow(1);
            assertThat(fila.getCell(0).getStringCellValue()).isEqualTo("B-7");
            assertThat(fila.getCell(1).getStringCellValue()).isEqualTo("ABIERTO");
            assertThat(fila.getCell(6).getStringCellValue()).isEmpty();
            assertThat(fila.getCell(9).getStringCellValue()).isEqualTo("Arancel completo");
            assertThat(fila.getCell(12).getNumericCellValue()).isEqualTo(1d);
            assertThat(fila.getCell(13).getStringCellValue()).isEqualTo("Cuota completa");
            assertThat(fila.getCell(14).getNumericCellValue()).isEqualTo(1d);
            assertThat(fila.getCell(15).getNumericCellValue()).isEqualTo(2027d);
            assertThat(fila.getCell(16).getNumericCellValue()).isEqualTo(3d);
            assertThat(fila.getCell(17).getNumericCellValue()).isEqualTo(1000.50d);
            assertThat(fila.getCell(18).getNumericCellValue()).isEqualTo(900.00d);
            assertThat(fila.getCell(22).getStringCellValue()).isEqualTo("X");
            assertThat(fila.getCell(23).getStringCellValue()).isEmpty();
            assertThat(fila.getCell(24).getStringCellValue()).isEmpty();
            assertThat(fila.getCell(25).getStringCellValue()).isEmpty();
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
                .hpum((byte) 1)
                .becaPorcentaje(new BigDecimal("25"))
                .cuotas(List.of(
                        BorradorCuota.builder().productoId(1).alternativaId(1).cuotaId(1).mes(3).anio(2027)
                                .vencimiento1(OffsetDateTime.parse("2027-03-15T00:00:00Z"))
                                .vencimiento2(OffsetDateTime.parse("2027-03-25T00:00:00Z"))
                                .importe1Original(new BigDecimal("1000.50"))
                                .importe1Borrador(new BigDecimal("900.00"))
                                .pagado((byte) 1).baja((byte) 0).compensada((byte) 0).bloqueado((byte) 0)
                                .build(),
                        BorradorCuota.builder().productoId(1).alternativaId(1).cuotaId(2).mes(4).anio(2027)
                                .importe1Original(new BigDecimal("500.00"))
                                .importe1Borrador(new BigDecimal("500.00"))
                                .pagado((byte) 0).baja((byte) 0).compensada((byte) 0).bloqueado((byte) 0)
                                .build()))
                .alternativas(List.of(BorradorAlternativa.builder().alternativaId(1).productoId(1)
                        .titulo("Cuota completa").build()))
                .arancelTipos(List.of(BorradorArancelTipo.builder().arancelTipoId(2)
                        .nombre("Arancel completo").build()))
                .build();
    }
}
