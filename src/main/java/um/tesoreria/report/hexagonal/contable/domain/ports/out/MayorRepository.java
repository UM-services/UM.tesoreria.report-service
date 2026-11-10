package um.tesoreria.report.hexagonal.contable.domain.ports.out;

import um.tesoreria.report.hexagonal.contable.domain.model.Mayor;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorSeleccion;

public interface MayorRepository {

    /** Consulta el mayor al core. Un rechazo de negocio del core se propaga como excepción del cliente. */
    Mayor consultar(MayorSeleccion seleccion);
}