package um.tesoreria.report.hexagonal.chequeras.application.usecases;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import um.tesoreria.report.hexagonal.chequeras.domain.model.DestinoPlanilla;
import um.tesoreria.report.hexagonal.chequeras.domain.model.FormatoPlanilla;
import um.tesoreria.report.hexagonal.chequeras.domain.model.OrigenPlanilla;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaBorradorUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaBorradorVerticalUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaDetalleUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaDetalleVerticalUseCase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GeneratePlanillaDetalleZipUseCaseImplTest {

    private final GeneratePlanillaDetalleUseCase planillaDetalle = mock(GeneratePlanillaDetalleUseCase.class);
    private final GeneratePlanillaDetalleVerticalUseCase planillaDetalleVertical =
            mock(GeneratePlanillaDetalleVerticalUseCase.class);
    private final GeneratePlanillaBorradorUseCase planillaBorrador = mock(GeneratePlanillaBorradorUseCase.class);
    private final GeneratePlanillaBorradorVerticalUseCase planillaBorradorVertical =
            mock(GeneratePlanillaBorradorVerticalUseCase.class);
    private final GeneratePlanillaDetalleZipUseCaseImpl useCase = new GeneratePlanillaDetalleZipUseCaseImpl(
            planillaDetalle, planillaDetalleVertical, planillaBorrador, planillaBorradorVertical);

    @TempDir
    Path directorio;

    // protects=un ZIP horizontal con una entrada por destino y el contenido de cada XLSX; fails_when=mezcla formatos,
    // omite un destino o cambia el nombre de la entrada; no_claim=no prueba el contenido real que genera cada planilla.
    @Test
    void generaUnZipHorizontalConUnaEntradaPorDestino() throws Exception {
        String primero = archivo("horizontal-1.xlsx", "uno");
        String segundo = archivo("horizontal-2.xlsx", "dos");
        when(planillaDetalle.generatePlanillaDetalle(6, 38, 1)).thenReturn(primero);
        when(planillaDetalle.generatePlanillaDetalle(6, 38, 2)).thenReturn(segundo);

        String zip = useCase.generatePlanillaDetalleZip(38, FormatoPlanilla.HORIZONTAL, OrigenPlanilla.CHEQUERAS,
                List.of(new DestinoPlanilla(6, 1), new DestinoPlanilla(6, 2)));

        try (ZipFile zipFile = new ZipFile(zip)) {
            assertThat(zipFile.size()).isEqualTo(2);
            assertThat(contenido(zipFile, "detalle-cuotas-6-38-1.xlsx")).isEqualTo("uno");
            assertThat(contenido(zipFile, "detalle-cuotas-6-38-2.xlsx")).isEqualTo("dos");
        }
        verifyNoInteractions(planillaDetalleVertical, planillaBorrador, planillaBorradorVertical);
        assertThat(Path.of(primero)).doesNotExist();
        assertThat(Path.of(segundo)).doesNotExist();
    }

    // protects=el formato vertical usa el generador vertical; fails_when=usa el horizontal; no_claim=no prueba el
    // contenido real de la planilla vertical.
    @Test
    void usaElGeneradorVerticalCuandoSePideEseFormato() throws Exception {
        String archivo = archivo("vertical.xlsx", "vertical");
        when(planillaDetalleVertical.generatePlanillaDetalle(6, 38, 1)).thenReturn(archivo);

        String zip = useCase.generatePlanillaDetalleZip(38, FormatoPlanilla.VERTICAL, OrigenPlanilla.CHEQUERAS,
                List.of(new DestinoPlanilla(6, 1)));

        try (ZipFile zipFile = new ZipFile(zip)) {
            assertThat(zipFile.size()).isEqualTo(1);
            assertThat(contenido(zipFile, "detalle-cuotas-6-38-1.xlsx")).isEqualTo("vertical");
        }
        verify(planillaDetalleVertical).generatePlanillaDetalle(6, 38, 1);
        verify(planillaDetalle, never()).generatePlanillaDetalle(6, 38, 1);
    }

    // protects=el origen BORRADORES usa sus generadores y nombra las entradas; fails_when=usa los generadores de
    // chequeras o conserva el prefijo detalle-cuotas; no_claim=no prueba el contenido interno del XLSX.
    @Test
    void usaLosGeneradoresDeBorradorYNombraLasEntradasConSuPrefijo() throws Exception {
        String horizontal = archivo("borrador-h.xlsx", "borrador-horizontal");
        String vertical = archivo("borrador-v.xlsx", "borrador-vertical");
        when(planillaBorrador.generatePlanillaBorrador(14, 38, 1)).thenReturn(horizontal);
        when(planillaBorradorVertical.generatePlanillaBorrador(14, 38, 2)).thenReturn(vertical);

        String zipHorizontal = useCase.generatePlanillaDetalleZip(38, FormatoPlanilla.HORIZONTAL,
                OrigenPlanilla.BORRADORES, List.of(new DestinoPlanilla(14, 1)));
        String zipVertical = useCase.generatePlanillaDetalleZip(38, FormatoPlanilla.VERTICAL,
                OrigenPlanilla.BORRADORES, List.of(new DestinoPlanilla(14, 2)));

        try (ZipFile zipFile = new ZipFile(zipHorizontal)) {
            assertThat(contenido(zipFile, "detalle-borradores-14-38-1.xlsx")).isEqualTo("borrador-horizontal");
        }
        try (ZipFile zipFile = new ZipFile(zipVertical)) {
            assertThat(contenido(zipFile, "detalle-borradores-14-38-2.xlsx")).isEqualTo("borrador-vertical");
        }
        verifyNoInteractions(planillaDetalle, planillaDetalleVertical);
    }

    // protects=un origen ausente se interpreta como chequeras (compatibilidad del contrato); fails_when=falla con
    // null o usa los generadores de borradores; no_claim=no valida el nombre del ZIP temporal.
    @Test
    void unOrigenAusenteSeInterpretaComoChequeras() throws Exception {
        String archivo = archivo("sin-origen.xlsx", "chequeras");
        when(planillaDetalle.generatePlanillaDetalle(6, 38, 1)).thenReturn(archivo);

        String zip = useCase.generatePlanillaDetalleZip(38, FormatoPlanilla.HORIZONTAL, null,
                List.of(new DestinoPlanilla(6, 1)));

        try (ZipFile zipFile = new ZipFile(zip)) {
            assertThat(contenido(zipFile, "detalle-cuotas-6-38-1.xlsx")).isEqualTo("chequeras");
        }
        verifyNoInteractions(planillaBorrador, planillaBorradorVertical);
    }

    // protects=los destinos repetidos se deduplican; fails_when=genera dos entradas o llama dos veces al generador;
    // no_claim=no valida el orden de los destinos.
    @Test
    void deduplicaDestinosRepetidos() throws Exception {
        String archivo = archivo("unico.xlsx", "unico");
        when(planillaDetalle.generatePlanillaDetalle(6, 38, 1)).thenReturn(archivo);

        String zip = useCase.generatePlanillaDetalleZip(38, FormatoPlanilla.HORIZONTAL, OrigenPlanilla.CHEQUERAS,
                List.of(new DestinoPlanilla(6, 1), new DestinoPlanilla(6, 1)));

        try (ZipFile zipFile = new ZipFile(zip)) {
            assertThat(zipFile.size()).isEqualTo(1);
        }
        verify(planillaDetalle).generatePlanillaDetalle(6, 38, 1);
    }

    // protects=rechaza una selección vacía antes de tocar el disco; fails_when=genera un ZIP vacío o llama al
    // generador; no_claim=no valida el mensaje exacto.
    @Test
    void rechazaUnaSeleccionVacia() {
        assertThatThrownBy(() -> useCase.generatePlanillaDetalleZip(38, FormatoPlanilla.HORIZONTAL,
                OrigenPlanilla.CHEQUERAS, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(planillaDetalle, planillaDetalleVertical, planillaBorrador, planillaBorradorVertical);
    }

    // protects=rechaza más de 50 destinos; fails_when=acepta un pedido sin límite; no_claim=no mide tiempos reales.
    @Test
    void rechazaMasDeCincuentaDestinos() {
        List<DestinoPlanilla> destinos = IntStream.rangeClosed(1, 51)
                .mapToObj(geograficaId -> new DestinoPlanilla(6, geograficaId))
                .toList();

        assertThatThrownBy(() -> useCase.generatePlanillaDetalleZip(38, FormatoPlanilla.HORIZONTAL,
                OrigenPlanilla.BORRADORES, destinos))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(planillaDetalle, planillaDetalleVertical, planillaBorrador, planillaBorradorVertical);
    }

    // protects=si falla un destino se propaga el error y se limpian los XLSX ya generados; fails_when=deja basura
    // temporal o devuelve un ZIP parcial; no_claim=no prueba el borrado del ZIP parcial.
    @Test
    void limpiaLosArchivosSiFallaUnDestino() throws Exception {
        String primero = archivo("primero.xlsx", "uno");
        when(planillaDetalle.generatePlanillaDetalle(6, 38, 1)).thenReturn(primero);
        when(planillaDetalle.generatePlanillaDetalle(6, 38, 2)).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> useCase.generatePlanillaDetalleZip(38, FormatoPlanilla.HORIZONTAL,
                OrigenPlanilla.CHEQUERAS, List.of(new DestinoPlanilla(6, 1), new DestinoPlanilla(6, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("boom");
        assertThat(Path.of(primero)).doesNotExist();
    }

    private String archivo(String nombre, String contenido) throws IOException {
        Path archivo = directorio.resolve(nombre);
        Files.writeString(archivo, contenido, StandardCharsets.UTF_8);
        return archivo.toString();
    }

    private String contenido(ZipFile zipFile, String nombre) throws IOException {
        return new String(zipFile.getInputStream(zipFile.getEntry(nombre)).readAllBytes(),
                StandardCharsets.UTF_8);
    }
}
