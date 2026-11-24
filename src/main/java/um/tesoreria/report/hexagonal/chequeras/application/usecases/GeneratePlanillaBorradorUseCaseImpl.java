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
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaBorradorUseCase;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Planilla horizontal de borradores abiertos: una fila por borrador (y por cuota cuando un período
 * tiene más de un producto). Muestra importe original e importe a emitir; no hay pagos.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratePlanillaBorradorUseCaseImpl implements GeneratePlanillaBorradorUseCase {

    static final int C_FIN = 13;

    private final Environment environment;
    private final BorradorRepository borradorRepository;
    private final LegajoRepository legajoRepository;

    @Override
    public String generatePlanillaBorrador(Integer facultadId, Integer lectivoId, Integer geograficaId) {
        log.debug("Processing GeneratePlanillaBorradorUseCaseImpl.generatePlanillaBorrador");
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

        List<Periodo> periodos = periodos(borradores);
        encabezado(sheet.createRow(0), periodos, bold);

        int fila = 0;
        for (Borrador borrador : borradores) {
            Map<Periodo, List<BorradorCuota>> porPeriodo = borrador.getCuotas().stream()
                    .filter(cuota -> cuota.getAnio() != null && cuota.getMes() != null)
                    .collect(Collectors.groupingBy(cuota -> new Periodo(cuota.getAnio(), cuota.getMes()),
                            LinkedHashMap::new, Collectors.toList()));
            int filas = porPeriodo.values().stream().mapToInt(List::size).max().orElse(1);

            for (int offset = 0; offset < filas; offset++) {
                Row row = sheet.createRow(++fila);
                escribirFijo(row, borrador, legajos, normal);
                int columna = C_FIN;
                for (Periodo periodo : periodos) {
                    List<BorradorCuota> cuotas = porPeriodo.getOrDefault(periodo, List.of());
                    if (offset < cuotas.size()) {
                        BorradorCuota cuota = cuotas.get(offset);
                        setCellString(row, columna, productoTitulo(borrador, cuota), normal);
                        setCellBigDecimal(row, columna + 1, cuota.getImporte1Original(), normal);
                        setCellBigDecimal(row, columna + 2, cuota.getImporte1Borrador(), normal);
                        setCellOffsetDateTime(row, columna + 3, cuota.getVencimiento1(), fecha);
                    }
                    columna += 4;
                }
                if (offset == 0) {
                    setCellBigDecimal(row, columna, total(borrador, false), normal);
                    setCellBigDecimal(row, columna + 1, total(borrador, true), normal);
                }
            }
        }

        for (int columna = 0; columna < C_FIN; columna++) {
            sheet.autoSizeColumn(columna);
        }
        for (int columna = C_FIN; columna < C_FIN + periodos.size() * 4 + 2; columna++) {
            sheet.setColumnWidth(columna, 4000);
        }

        return escribir(book, facultadId, lectivoId, geograficaId);
    }

    private void encabezado(Row row, List<Periodo> periodos, CellStyle bold) {
        String[] fijos = {"Borrador", "Estado", "DU", "Apellido, Nombre", "Facultad", "Sede",
                "Carrera", "Curso", "Tipo Chequera", "Tipo Arancel", "HPUM", "Beca", "Alternativa"};
        for (int i = 0; i < fijos.length; i++) {
            setCellString(row, i, fijos[i], bold);
        }
        int columna = C_FIN;
        for (Periodo periodo : periodos) {
            String prefijo = MessageFormat.format("{0}/{1,number,#}", periodo.mes(), periodo.anio());
            setCellString(row, columna, prefijo + " Producto", bold);
            setCellString(row, columna + 1, prefijo + " Importe original", bold);
            setCellString(row, columna + 2, prefijo + " Importe borrador", bold);
            setCellString(row, columna + 3, prefijo + " Vencimiento", bold);
            columna += 4;
        }
        setCellString(row, columna, "Total original", bold);
        setCellString(row, columna + 1, "Total borrador", bold);
    }

    private void escribirFijo(Row row, Borrador borrador, Map<String, Legajo> legajos, CellStyle normal) {
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
        setCellString(row, 12, alternativaTitulo(borrador, borrador.getAlternativaId()), normal);
    }

    private List<Periodo> periodos(List<Borrador> borradores) {
        List<Periodo> periodos = new ArrayList<>();
        borradores.stream()
                .flatMap(borrador -> borrador.getCuotas().stream())
                .filter(cuota -> cuota.getAnio() != null && cuota.getMes() != null)
                .map(cuota -> new Periodo(cuota.getAnio(), cuota.getMes()))
                .distinct()
                .sorted(Comparator.comparing(Periodo::anio).thenComparing(Periodo::mes))
                .forEach(periodos::add);
        return periodos;
    }

    private BigDecimal total(Borrador borrador, boolean borradorTotal) {
        return borrador.getCuotas().stream()
                .map(cuota -> borradorTotal ? cuota.getImporte1Borrador() : cuota.getImporte1Original())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String productoTitulo(Borrador borrador, BorradorCuota cuota) {
        return alternativaTitulo(borrador, cuota.getAlternativaId(), cuota.getProductoId());
    }

    private String alternativaTitulo(Borrador borrador, Integer alternativaId) {
        return alternativaTitulo(borrador, alternativaId, null);
    }

    private String alternativaTitulo(Borrador borrador, Integer alternativaId, Integer productoId) {
        return borrador.getAlternativas().stream()
                .filter(alternativa -> Objects.equals(alternativa.getAlternativaId(), alternativaId)
                        && (productoId == null || Objects.equals(alternativa.getProductoId(), productoId)))
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

    static String clave(BigDecimal personaId, Integer documentoId) {
        return personaId == null ? "-" : personaId.stripTrailingZeros().toPlainString() + "." + documentoId;
    }

    private String escribir(Workbook book, Integer facultadId, Integer lectivoId, Integer geograficaId) {
        try {
            Path directorio = Path.of(environment.getProperty("path.reports", "/tmp/"));
            Path archivo = Files.createTempFile(directorio,
                    "detalle-borradores-" + facultadId + "-" + lectivoId + "-" + geograficaId + "-", ".xlsx");
            try (FileOutputStream output = new FileOutputStream(archivo.toFile())) {
                book.write(output);
            }
            book.close();
            return archivo.toString();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar la planilla de borradores", e);
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

    record Periodo(Integer anio, Integer mes) {
    }
}
