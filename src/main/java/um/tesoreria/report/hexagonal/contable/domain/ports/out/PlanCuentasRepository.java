package um.tesoreria.report.hexagonal.contable.domain.ports.out;

import um.tesoreria.report.hexagonal.contable.domain.model.CuentaPlan;

import java.util.List;
import java.util.Map;

public interface PlanCuentasRepository {

    List<CuentaPlan> findAllCuentas();

    /** Nombre de cada sede por su id. */
    Map<Integer, String> findNombresSedes();
}