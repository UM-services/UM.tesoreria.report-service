package um.tesoreria.report.hexagonal.recibos.infrastructure.web;

import org.junit.jupiter.api.Test;
import um.tesoreria.report.hexagonal.recibos.domain.model.Recibo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReciboResponseTest {

    // protects=pdfUrl apunta al PDF del sender para ese recibo; fails_when=la ruta o el id cambian; no_claim=no verifica que el gateway enrute esa ruta
    @Test
    void from_buildsPdfUrlPointingToTheSenderRecibo() {
        Recibo recibo = new Recibo(55L, 1, 2, 100L, 7L, 1, "Arancel", 2026, 3, 3, 12L, new BigDecimal("1500.00"),
                OffsetDateTime.parse("2026-03-01T00:00:00Z"), "CAE1");

        ReciboResponse response = ReciboResponse.from(recibo);

        assertThat(response.pdfUrl()).isEqualTo("/api/tesoreria/sender/recibo/pdf/55");
        assertThat(response.facturacionElectronicaId()).isEqualTo(55L);
        assertThat(response.cae()).isEqualTo("CAE1");
        assertThat(response.producto()).isEqualTo("Arancel");
        assertThat(response.anho()).isEqualTo(2026);
        assertThat(response.mes()).isEqualTo(3);
    }
}