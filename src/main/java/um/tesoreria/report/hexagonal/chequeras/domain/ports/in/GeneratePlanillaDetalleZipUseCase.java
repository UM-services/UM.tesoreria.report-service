package um.tesoreria.report.hexagonal.chequeras.domain.ports.in;

import um.tesoreria.report.hexagonal.chequeras.domain.model.DestinoPlanilla;
import um.tesoreria.report.hexagonal.chequeras.domain.model.FormatoPlanilla;
import um.tesoreria.report.hexagonal.chequeras.domain.model.OrigenPlanilla;

import java.util.List;

public interface GeneratePlanillaDetalleZipUseCase {

    String generatePlanillaDetalleZip(Integer lectivoId, FormatoPlanilla formato, OrigenPlanilla origen,
                                      List<DestinoPlanilla> destinos);
}
