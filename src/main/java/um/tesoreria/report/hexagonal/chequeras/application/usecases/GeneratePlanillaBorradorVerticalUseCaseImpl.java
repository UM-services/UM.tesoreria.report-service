package um.tesoreria.report.hexagonal.chequeras.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.Borrador;
import um.tesoreria.report.hexagonal.chequeras.domain.model.BorradorArancelTipo;
import um.tesoreria.report.hexagonal.chequeras.domain.model.BorradorCuota;
import um.tesoreria.report.hexagonal.chequeras.domain.model.Legajo;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaBorradorVerticalUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.BorradorRepository;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.LegajoRepository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Planilla vertical de borradores: una fila por cuota, con importe original, importe a emitir,
 * vencimientos y flags. No hay columnas de pagos (un borrador no tiene pagos).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratePlanillaBorradorVerticalUseCaseImpl implements GeneratePlanillaBorradorVerticalUseCase {

    private final Environment environment;
    private final BorradorRepository borradorRepository;
    private final LegajoRepository legajoRepository;

    @Override
    public String generatePlanillaBorrador(Integer facultadId, Integer lectivoId, Integer geograficaId) {
        log.debug("Processing GeneratePlanillaBorradorVerticalUseCaseImpl.generatePlanillaBorrador");
        List<Borrador> borradores = borradorRepository.findAllBySede(facultadId, lectivoId, geograficaId);
        Map<String, Legajo> legajos = legajos(facultadId);

        Workbook book = new XSSFWorkbook();
        CellStyle normal = estilo(book, false);
        CellStyle bold = estilo(book, true);
        CellStyle fecha = fecha(book, normal);

        String hoja = borradores.stream()
                .map(Borrador::getLectivoNombre)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Borradores");
        Sheet sheet = book.createSheet(hoja.length() > 31 ? hoja.substring(0, 31) : hoja);

        encabezado(sheet.createRow(0), bold);

        int fila = 0;
        for (Borrador borrador : borradores) {
            List<BorradorCuota> cuotas = borrador.getCuotas().stream()
                    .sorted(Comparator.comparing(BorradorCuota::getProductoId,
                                    Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(BorradorCuota::getAlternativaId,
                                    Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(BorradorCuota::getCuotaId,
                                    Comparator.nullsLast(Integer::compareTo)))
                    .toList();
            for (BorradorCuota cuota : cuotas) {
                Row row = sheet.createRow(++fila);
                setCellString(row, 0, "B-" + borrador.getBorradorId(), normal);
                setCellString(row, 1, borrador.getEstado(), normal);
                setCellBigDecimal(row, 2, borrador.getPersonaId(), normal);
                setCellString(row, 3, MessageFormat.format("{0}, {1}",
                        nvl(borrador.getPersonaApellido()), nvl(borrador.getPersonaNombre())), normal);
                setCellString(row, 4, borrador.getFacultadNombre(), normal);
                setCellString(row, 5, borrador.getGeograficaNombre(), normal);
                setCellString(row, 6, carrera(borrador, legajos), normal);
                setCellInteger(row, 7, borrador.getCursoId(), normal);
                setCellString(row, 8, borrador.getTipoChequeraNombre(), normal);
                setCellString(row, 9, arancelNombre(borrador), normal);
                setCellString(row, 10, esVerdadero(borrador.getHpum()) ? "X" : "", normal);
                setCellBigDecimal(row, 11, borrador.getBecaPorcentaje(), normal);
                setCellInteger(row, 12, cuota.getAlternativaId(), normal);
                setCellString(row, 13, alternativaTitulo(borrador, cuota.getAlternativaId(), cuota.getProductoId()),
                        normal);
                setCellInteger(row, 14, cuota.getCuotaId(), normal);
                setCellInteger(row, 15, cuota.getAnio(), normal);
                setCellInteger(row, 16, cuota.getMes(), normal);
                setCellBigDecimal(row, 17, cuota.getImporte1Original(), normal);
                setCellBigDecimal(row, 18, cuota.getImporte1Borrador(), normal);
                setCellOffsetDateTime(row, 19, cuota.getVencimiento1(), fecha);
                setCellOffsetDateTime(row, 20, cuota.getVencimiento2(), fecha);
                setCellOffsetDateTime(row, 21, cuota.getVencimiento3(), fecha);
                setCellString(row, 22, esVerdadero(cuota.getPagado()) ? "X" : "", normal);
                setCellString(row, 23, esVerdadero(cuota.getBaja()) ? "X" : "", normal);
                setCellString(row, 24, esVerdadero(cuota.getCompensada()) ? "X" : "", normal);
                setCellString(row, 25, esVerdadero(cuota.getBloqueado()) ? "X" : "", normal);
            }
        }

        for (int columna = 0; columna < 26; columna++) {
            sheet.autoSizeColumn(columna);
        }

        return escribir(book, facultadId, lectivoId, geograficaId);
    }

    private void encabezado(Row row, CellStyle bold) {
        String[] titulos = {"Borrador", "Estado", "DU", "Apellido, Nombre", "Facultad", "Sede",
                "Carrera", "Curso", "Tipo Chequera", "Tipo Arancel", "HPUM", "Beca", "Alternativa",
                "Producto", "Cuota", "Año", "Mes", "Importe original", "Importe borrador",
                "Vencimiento 1", "Vencimiento 2", "Vencimiento 3", "Pagado", "Baja", "Compensada",
                "Bloqueada"};
        for (int i = 0; i < titulos.length; i++) {
            setCellString(row, i, titulos[i], bold);
        }
    }

    private String alternativaTitulo(Borrador borrador, Integer alternativaId, Integer productoId) {
        return borrador.getAlternativas().stream()
                .filter(alternativa -> Objects.equals(alternativa.getAlternativaId(), alternativaId)
                        && Objects.equals(alternativa.getProductoId(), productoId))
                .map(alternativa -> alternativa.getTitulo())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(productoId == null ? null : "Producto " + productoId);
    }

    private String arancelNombre(Borrador borrador) {
        return borrador.getArancelTipos().stream()
                .filter(tipo -> Objects.equals(tipo.getArancelTipoId(), borrador.getArancelTipoId()))
                .map(BorradorArancelTipo::getNombre)
                .findFirst()
                .orElse(null);
    }

    private String carrera(Borrador borrador, Map<String, Legajo> legajos) {
        Legajo legajo = legajos.get(clave(borrador.getPersonaId(), borrador.getDocumentoId()));
        if (legajo == null || legajo.getCarrera() == null) {
            return "";
        }
        String plan = legajo.getCarrera().getPlan() == null ? "" : legajo.getCarrera().getPlan().getNombre();
        return MessageFormat.format("{0}/{1}", plan, legajo.getCarrera().getNombre());
    }

    private Map<String, Legajo> legajos(Integer facultadId) {
        return legajoRepository.findAllByFacultadId(facultadId).stream()
                .collect(Collectors.toMap(legajo -> clave(legajo.getPersonaId(), legajo.getDocumentoId()),
                        legajo -> legajo, (actual, repetido) -> actual));
    }

    private static String clave(BigDecimal personaId, Integer documentoId) {
        return personaId == null ? "-" : personaId.stripTrailingZeros().toPlainString() + "." + documentoId;
    }

    private String escribir(Workbook book, Integer facultadId, Integer lectivoId, Integer geograficaId) {
        try {
            Path directorio = Path.of(environment.getProperty("path.reports", "/tmp/"));
            Path archivo = Files.createTempFile(directorio,
                    "detalle-borradores-vertical-" + facultadId + "-" + lectivoId + "-" + geograficaId + "-",
                    ".xlsx");
            try (FileOutputStream output = new FileOutputStream(archivo.toFile())) {
                book.write(output);
            }
            book.close();
            return archivo.toString();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar la planilla vertical de borradores", e);
        }
    }

    private CellStyle estilo(Workbook book, boolean negrita) {
        CellStyle style = book.createCellStyle();
        Font font = book.createFont();
        font.setBold(negrita);
        style.setFont(font);
        return style;
    }

    private CellStyle fecha(Workbook book, CellStyle base) {
        CellStyle style = book.createCellStyle();
        style.cloneStyleFrom(base);
        style.setDataFormat(book.createDataFormat().getFormat("dd/MM/yyyy"));
        return style;
    }

    private void setCellOffsetDateTime(Row row, int column, OffsetDateTime value, CellStyle style) {
        if (value == null) {
            return;
        }
        Cell cell = row.createCell(column);
        cell.setCellValue(new Date(value.toInstant().toEpochMilli()));
        cell.setCellStyle(style);
    }

    private void setCellBigDecimal(Row row, int column, BigDecimal value, CellStyle style) {
        if (value == null) {
            return;
        }
        Cell cell = row.createCell(column);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }

    private void setCellString(Row row, int column, String value, CellStyle style) {
        if (value == null) {
            return;
        }
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void setCellInteger(Row row, int column, Integer value, CellStyle style) {
        if (value == null) {
            return;
        }
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static boolean esVerdadero(Byte valor) {
        return valor != null && valor != 0;
    }

    private static String nvl(String valor) {
        return valor == null ? "" : valor;
    }
}
