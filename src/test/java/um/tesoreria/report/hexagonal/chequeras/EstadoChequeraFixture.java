package um.tesoreria.report.hexagonal.chequeras;

import um.tesoreria.report.hexagonal.chequeras.domain.model.EstadoChequera;
import um.tesoreria.report.hexagonal.chequeras.domain.model.CuotaEstado;
import um.tesoreria.report.hexagonal.chequeras.domain.model.DebitoEstado;
import um.tesoreria.report.hexagonal.chequeras.domain.model.ProductoEstado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos de prueba, todos ficticios: una chequera 1/2/12345 con Matrícula de 2 cuotas y Arancel de 12
 * (las primeras 7 cuotas de Arancel pagas) y 10 cuotas adheridas al débito automático, mezclando
 * Débito Directo y Débito VISA a propósito. No usar datos de personas reales acá (nombres,
 * documentos, CBU ni números de chequera).
 */
public final class EstadoChequeraFixture {

    public static final String CBU = "1234567890123456789012";

    private EstadoChequeraFixture() {
    }

    public static EstadoChequera referencia() {
        return referencia(12);
    }

    /** Igual que {@link #referencia()} pero con {@code cuotasArancel} cuotas de Arancel (las primeras 7 pagas). */
    public static EstadoChequera referencia(int cuotasArancel) {
        List<CuotaEstado> matricula = List.of(
                new CuotaEstado(1, 6, 2026, LocalDate.of(2026, 6, 10), importe("182000"), 0,
                        LocalDate.of(2026, 6, 19), importe("182000"), "D2026062301_30000000000"),
                new CuotaEstado(2, 11, 2026, LocalDate.of(2026, 11, 10), importe("193000"), null, null, null, null));

        String[][] pagas = {
                {"331000", "2026-03-19", "MercadoPago"},
                {"331000", "2026-04-10", "D2026041401_30000000000"},
                {"331000", "2026-05-08", "D2026051201_30000000000"},
                {"364000", "2026-06-10", "D2026061201_30000000000"},
                {"364000", "2026-07-21", "MercadoPago"},
                {"364000", "2026-08-10", "D2026081201_30000000000"},
                {"386000", "2026-09-10", "D2026091201_30000000000"}};

        List<CuotaEstado> arancel = new ArrayList<>();
        for (int i = 0; i < cuotasArancel; i++) {
            int mes = (i + 2) % 12 + 1;
            int anho = i < 10 ? 2026 : 2027 + (i - 10) / 12;
            LocalDate vencimiento = LocalDate.of(anho, mes, 10);
            if (i < pagas.length) {
                String[] pago = pagas[i];
                arancel.add(new CuotaEstado(i + 1, mes, anho, vencimiento, importe(pago[0]), 0,
                        LocalDate.parse(pago[1]), importe(pago[0]), pago[2]));
            } else {
                arancel.add(new CuotaEstado(i + 1, mes, anho, vencimiento,
                        i < 9 ? importe("386000") : importe("0"), null, null, null, null));
            }
        }

        List<ProductoEstado> productos = List.of(
                new ProductoEstado(1, "Matrícula", "Matrícula", 2, importe("375000"), importe("182000"), matricula),
                new ProductoEstado(2, "Arancel", "Arancel Mensual", cuotasArancel, importe("3243000"), importe("2471000"), arancel));

        // Mezcla a propósito Débito Directo y Débito VISA, para probar que el PDF los muestra a
        // los dos juntos (con la columna "Tipo" para distinguirlos) en vez de uno solo.
        List<DebitoEstado> debitos = List.of(
                debito(1, "182000", LocalDate.of(2026, 6, 19), LocalDateTime.of(2026, 6, 4, 0, 0), false, "", "Débito Directo"),
                debito(2, "193000", LocalDate.of(2026, 11, 19), null, false, "", "Débito Directo"),
                debito(2, "331000", LocalDate.of(2026, 4, 8), LocalDateTime.of(2026, 4, 6, 0, 0), false, "", "Débito Directo"),
                debito(3, "331000", LocalDate.of(2026, 5, 8), LocalDateTime.of(2026, 5, 4, 0, 0), false, "", "Débito Directo"),
                debito(4, "364000", LocalDate.of(2026, 6, 10), LocalDateTime.of(2026, 6, 4, 0, 0), false, "", "Débito Directo"),
                debito(5, "364000", LocalDate.of(2026, 7, 22), LocalDateTime.of(2026, 7, 3, 0, 0), true, "0011000100", "Débito VISA"),
                debito(6, "364000", LocalDate.of(2026, 8, 10), LocalDateTime.of(2026, 8, 3, 0, 0), false, "", "Débito VISA"),
                debito(7, "386000", LocalDate.of(2026, 9, 10), LocalDateTime.of(2026, 9, 2, 0, 0), false, "", "Débito VISA"),
                debito(8, "386000", LocalDate.of(2026, 10, 10), null, false, "", "Débito VISA"),
                debito(9, "386000", LocalDate.of(2026, 11, 10), null, false, "", "Débito VISA"));

        return new EstadoChequera(1, "Facultad de Ingeniería", 2, "Matrícula y Arancel", 12345L,
                new BigDecimal("12345678"), "MUÑOZ", "Ana Ejemplo", "Ciclo Completo", "Lectivo 2026 - 2027",
                new BigDecimal("0.15"), "Rapipago", 1, true, productos, debitos);
    }

    private static DebitoEstado debito(int cuotaId, String importe, LocalDate vencimiento, LocalDateTime envio,
                                       boolean rechazado, String motivo, String tipoDebito) {
        return new DebitoEstado(cuotaId, importe(importe), vencimiento, CBU, tipoDebito, envio, rechazado, motivo);
    }

    private static BigDecimal importe(String value) {
        return new BigDecimal(value).setScale(2);
    }
}