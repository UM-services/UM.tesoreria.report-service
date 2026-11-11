package um.tesoreria.report.hexagonal.contable.domain.ports.in;

import um.tesoreria.report.hexagonal.contable.domain.model.MayorSeleccion;

public interface GenerateMayorPdfUseCase {

    /**
     * Devuelve el contenido de un {@code .pdf} con el mayor de todas las cuentas pedidas y la cuenta y el ejercicio
     * de cada bloque. Los importes, saldos y totales son los que informa el core.
     */
    byte[] generate(MayorSeleccion seleccion);
}