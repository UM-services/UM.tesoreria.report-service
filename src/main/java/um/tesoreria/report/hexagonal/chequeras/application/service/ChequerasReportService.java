package um.tesoreria.report.hexagonal.chequeras.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.report.hexagonal.chequeras.domain.model.DestinoPlanilla;
import um.tesoreria.report.hexagonal.chequeras.domain.model.FormatoPlanilla;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GenerateEstadoChequeraUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GenerateMateriasInscriptasUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaDetalleUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaDetalleVerticalUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaDetalleZipUseCase;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.in.GeneratePlanillaPagosUseCase;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChequerasReportService {

    private final GeneratePlanillaDetalleUseCase generatePlanillaDetalleUseCase;
    private final GeneratePlanillaDetalleVerticalUseCase generatePlanillaDetalleVerticalUseCase;
    private final GeneratePlanillaDetalleZipUseCase generatePlanillaDetalleZipUseCase;
    private final GeneratePlanillaPagosUseCase generatePlanillaPagosUseCase;
    private final GenerateMateriasInscriptasUseCase generateMateriasInscriptasUseCase;
    private final GenerateEstadoChequeraUseCase generateEstadoChequeraUseCase;

    public String generatePlanillaDetalle(Integer facultadId, Integer lectivoId, Integer geograficaId) {
        return generatePlanillaDetalleUseCase.generatePlanillaDetalle(facultadId, lectivoId, geograficaId);
    }

    public String generatePlanillaDetalleVertical(Integer facultadId, Integer lectivoId, Integer geograficaId) {
        return generatePlanillaDetalleVerticalUseCase.generatePlanillaDetalle(facultadId, lectivoId, geograficaId);
    }

    public String generatePlanillaDetalleZip(Integer lectivoId, FormatoPlanilla formato,
                                             List<DestinoPlanilla> destinos) {
        return generatePlanillaDetalleZipUseCase.generatePlanillaDetalleZip(lectivoId, formato, destinos);
    }

    public String generatePlanillaPagos(Integer facultadId, Integer tipoChequeraId, Integer lectivoId) {
        return generatePlanillaPagosUseCase.generatePlanillaPagos(facultadId, tipoChequeraId, lectivoId);
    }

    public String generateEstadoChequera(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                         Integer alternativaId) {
        return generateEstadoChequeraUseCase.generateEstadoChequera(facultadId, tipoChequeraId, chequeraSerieId,
                alternativaId);
    }

    public String generateMateriasInscriptas(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                            Integer alternativaId) {
        return generateMateriasInscriptasUseCase.generateMateriasInscriptas(facultadId, tipoChequeraId,
                chequeraSerieId, alternativaId);
    }
}
