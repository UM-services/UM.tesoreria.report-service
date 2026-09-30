package um.tesoreria.report.hexagonal.chequeras.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.EstadoChequera;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GenerateEstadoChequeraUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.ChequeraRepository;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.EstadoChequeraPdfGenerator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateEstadoChequeraUseCaseImpl implements GenerateEstadoChequeraUseCase {

    private final Environment environment;
    private final ChequeraRepository chequeraRepository;
    private final EstadoChequeraPdfGenerator pdfGenerator;

    @Override
    public String generateEstadoChequera(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                         Integer alternativaId) {
        log.debug("Processing GenerateEstadoChequeraUseCaseImpl.generateEstadoChequera");
        EstadoChequera estadoChequera = chequeraRepository.findEstadoChequera(facultadId, tipoChequeraId,
                chequeraSerieId, alternativaId);

        String path = environment.getProperty("path.reports");
        // Sin MessageFormat: {2} con un Long agregaría separador de miles (12,345).
        String filename = path + "estado-chequera." + facultadId + "." + tipoChequeraId + "." + chequeraSerieId
                + "." + alternativaId + ".pdf";

        File file = new File(filename);
        try (OutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
            pdfGenerator.generate(estadoChequera, output);
        } catch (IOException | RuntimeException e) {
            log.error("Error generando el estado de chequera {}/{}/{}", facultadId, tipoChequeraId, chequeraSerieId, e);
            // No dejar un PDF a medias que después se pueda descargar como si estuviera bien.
            file.delete();
            throw new IllegalStateException("No se pudo generar el estado de chequera "
                    + facultadId + "/" + tipoChequeraId + "/" + chequeraSerieId, e);
        }
        return filename;
    }
}