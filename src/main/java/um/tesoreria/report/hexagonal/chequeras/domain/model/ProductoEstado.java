package um.tesoreria.report.hexagonal.chequeras.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Un producto de la chequera (Matrícula, Arancel, ...), con el título y la cantidad de cuotas
 * de su alternativa y los totales oficiales de {@code chequera_total}.
 */
public record ProductoEstado(
        Integer productoId,
        String nombre,
        String tituloCuota,
        Integer totalCuotas,
        BigDecimal total,
        BigDecimal pagado,
        List<CuotaEstado> cuotas) {

    public ProductoEstado {
        cuotas = cuotas == null ? List.of() : List.copyOf(cuotas);
    }
}