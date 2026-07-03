package um.tesoreria.report.hexagonal.contable.domain.ports.out;

import um.tesoreria.report.hexagonal.contable.domain.model.LectivoTotalImputacion;

import java.util.List;

public interface LectivoTotalImputacionRepository {
    List<LectivoTotalImputacion> findAllByLectivo(Integer lectivoId);
}
