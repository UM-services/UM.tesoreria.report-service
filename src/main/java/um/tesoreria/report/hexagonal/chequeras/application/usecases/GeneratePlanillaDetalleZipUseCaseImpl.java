package um.tesoreria.report.hexagonal.chequeras.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.DestinoPlanilla;
import um.tesoreria.report.hexagonal.chequeras.domain.model.FormatoPlanilla;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaDetalleUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaDetalleVerticalUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaDetalleZipUseCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratePlanillaDetalleZipUseCaseImpl implements GeneratePlanillaDetalleZipUseCase {

    static final int MAX_DESTINOS = 50;

    private final GeneratePlanillaDetalleUseCase planillaDetalleUseCase;
    private final GeneratePlanillaDetalleVerticalUseCase planillaDetalleVerticalUseCase;

    @Override
    public String generatePlanillaDetalleZip(Integer lectivoId, FormatoPlanilla formato,
                                             List<DestinoPlanilla> destinos) {
        log.debug("Processing GeneratePlanillaDetalleZipUseCaseImpl.generatePlanillaDetalleZip");
        List<DestinoPlanilla> seleccion = validar(lectivoId, formato, destinos);

        Path zip = null;
        List<Path> generados = new ArrayList<>();
        try {
            zip = Files.createTempFile("detalle-cuotas-" + lectivoId + "-", ".zip");
            try (ZipOutputStream salida = new ZipOutputStream(Files.newOutputStream(zip))) {
                for (DestinoPlanilla destino : seleccion) {
                    Path archivo = Path.of(generarPlanilla(lectivoId, formato, destino));
                    generados.add(archivo);
                    String nombre = MessageFormat.format("detalle-cuotas-{0}-{1}-{2}.xlsx",
                            destino.facultadId(), lectivoId, destino.geograficaId());
                    salida.putNextEntry(new ZipEntry(nombre));
                    Files.copy(archivo, salida);
                    salida.closeEntry();
                }
            }
            return zip.toString();
        } catch (IOException e) {
            eliminar(zip);
            throw new IllegalStateException("No se pudo generar el ZIP de planillas de detalle", e);
        } catch (RuntimeException e) {
            eliminar(zip);
            throw e;
        } finally {
            generados.forEach(this::eliminar);
        }
    }

    private List<DestinoPlanilla> validar(Integer lectivoId, FormatoPlanilla formato,
                                          List<DestinoPlanilla> destinos) {
        if (lectivoId == null) {
            throw new IllegalArgumentException("El lectivo es obligatorio");
        }
        if (formato == null) {
            throw new IllegalArgumentException("El formato es obligatorio");
        }
        if (destinos == null || destinos.isEmpty()) {
            throw new IllegalArgumentException("Seleccioná al menos una facultad y sede");
        }
        if (destinos.stream().anyMatch(destino -> destino == null
                || destino.facultadId() == null || destino.geograficaId() == null)) {
            throw new IllegalArgumentException("Cada destino debe informar facultad y sede");
        }
        List<DestinoPlanilla> seleccion = destinos.stream().distinct().toList();
        if (seleccion.size() > MAX_DESTINOS) {
            throw new IllegalArgumentException(
                    MessageFormat.format("No se pueden generar más de {0} planillas por pedido", MAX_DESTINOS));
        }
        return seleccion;
    }

    private String generarPlanilla(Integer lectivoId, FormatoPlanilla formato, DestinoPlanilla destino) {
        return switch (formato) {
            case HORIZONTAL -> planillaDetalleUseCase.generatePlanillaDetalle(
                    destino.facultadId(), lectivoId, destino.geograficaId());
            case VERTICAL -> planillaDetalleVerticalUseCase.generatePlanillaDetalle(
                    destino.facultadId(), lectivoId, destino.geograficaId());
        };
    }

    private void eliminar(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo temporal {}", path, e);
        }
    }
}
