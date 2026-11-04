package um.tesoreria.report.hexagonal.contable.domain.ports.in;

public interface GeneratePlanCuentasExcelUseCase {

    /** Devuelve el contenido de un {@code .xlsx} con todo el plan de cuentas, ordenado por número de cuenta. */
    byte[] generate();
}