package um.tesoreria.report.hexagonal.contable.domain.ports.in;

import um.tesoreria.report.hexagonal.contable.domain.model.MayorSeleccion;

public interface GenerateMayorExcelUseCase {

    /**
     * Devuelve el contenido de un {@code .xlsx} con el mayor de todas las cuentas pedidas, en una sola hoja y con la
     * cuenta y el ejercicio de cada línea. Los importes, saldos y totales son los que informa el core.
     */
    byte[] generate(MayorSeleccion seleccion);
}