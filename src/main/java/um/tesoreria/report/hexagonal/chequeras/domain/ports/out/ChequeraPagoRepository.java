package um.tesoreria.report.hexagonal.chequeras.domain.ports.out;

import um.tesoreria.report.hexagonal.chequeras.domain.model.ChequeraPago;
import java.util.List;

public interface ChequeraPagoRepository {
    List<ChequeraPago> findAllByFacultadIdAndTipoChequeraIdAndLectivoId(Integer facultadId, Integer tipoChequeraId, Integer lectivoId);
}
