package um.tesoreria.report.hexagonal.contable.infrastructure.client.adapter;

import org.junit.jupiter.api.Test;
import um.tesoreria.report.hexagonal.contable.domain.model.OrdenPago;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.OrdenPagoClient;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.OrdenPagoImpresionDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeignOrdenPagoRepositoryAdapterTest {

    private final OrdenPagoClient client = mock(OrdenPagoClient.class);
    private final FeignOrdenPagoRepositoryAdapter adapter = new FeignOrdenPagoRepositoryAdapter(client);

    private static OffsetDateTime fecha(int anio, int mes, int dia) {
        return OffsetDateTime.of(anio, mes, dia, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    // protects=la respuesta del core pasa al dominio campo por campo y el importe de cada comprobante es el que aplicó la orden; fails_when=se pierde un campo, se confunde importe aplicado con importe del comprobante o se recalcula un total; no_claim=no prueba el contenido contable
    @Test
    void findById_mapsEveryFieldAndUsesTheAppliedAmount() {
        OrdenPagoImpresionDto dto = new OrdenPagoImpresionDto();
        dto.setProveedorMovimientoId(10L);
        dto.setPrefijo(15);
        dto.setNumeroComprobante(3684L);
        dto.setFechaComprobante(fecha(2026, 11, 2));
        dto.setConcepto("Compra");
        dto.setRazonSocial("ACME SA");
        dto.setDomicilio("Calle 1");
        dto.setCuit("30-1-2");
        OrdenPagoImpresionDto.Comprobante comprobante = new OrdenPagoImpresionDto.Comprobante();
        comprobante.setComprobanteDescripcion("Factura A");
        comprobante.setPrefijo(1);
        comprobante.setNumeroComprobante(5L);
        comprobante.setFechaComprobante(fecha(2026, 10, 1));
        comprobante.setImporte(new BigDecimal("700"));
        comprobante.setImporteAplicado(new BigDecimal("300"));
        dto.setComprobantes(List.of(comprobante));
        dto.setTotalComprobantes(new BigDecimal("300"));
        OrdenPagoImpresionDto.Valor valor = new OrdenPagoImpresionDto.Valor();
        valor.setConcepto("Transferencia");
        valor.setNombreTitular("ACME SA");
        valor.setNumero(77L);
        valor.setFechaEmision(fecha(2026, 11, 2));
        valor.setImporte(new BigDecimal("300"));
        dto.setValores(List.of(valor));
        dto.setTotalValores(new BigDecimal("300"));
        OrdenPagoImpresionDto.Traza traza = new OrdenPagoImpresionDto.Traza();
        traza.setNumeroCuenta(new BigDecimal("2110101"));
        traza.setNombreCuenta("Proveedores");
        traza.setDebe(new BigDecimal("300"));
        traza.setHaber(BigDecimal.ZERO);
        dto.setTraza(List.of(traza));
        dto.setTotalDebe(new BigDecimal("300"));
        dto.setTotalHaber(new BigDecimal("300"));
        when(client.impresion(10L)).thenReturn(dto);

        OrdenPago orden = adapter.findById(10L);

        assertThat(orden.prefijo()).isEqualTo(15);
        assertThat(orden.numeroComprobante()).isEqualTo(3684L);
        assertThat(orden.razonSocial()).isEqualTo("ACME SA");
        assertThat(orden.comprobantes()).singleElement().satisfies(c -> {
            assertThat(c.descripcion()).isEqualTo("Factura A");
            assertThat(c.importe()).isEqualByComparingTo("300");
        });
        assertThat(orden.totalComprobantes()).isEqualByComparingTo("300");
        assertThat(orden.valores()).singleElement().satisfies(v -> {
            assertThat(v.titular()).isEqualTo("ACME SA");
            assertThat(v.numero()).isEqualTo(77L);
        });
        assertThat(orden.traza()).singleElement().satisfies(t -> assertThat(t.nombreCuenta()).isEqualTo("Proveedores"));
        assertThat(orden.totalDebe()).isEqualByComparingTo("300");
        assertThat(orden.totalHaber()).isEqualByComparingTo("300");
    }

    // protects=si el core no manda listas el dominio recibe listas vacías; fails_when=un null llega al generador y corta la impresión; no_claim=no prueba que el core mande siempre las listas
    @Test
    void findById_turnsMissingListsIntoEmptyOnes() {
        OrdenPagoImpresionDto dto = new OrdenPagoImpresionDto();
        dto.setProveedorMovimientoId(10L);
        when(client.impresion(10L)).thenReturn(dto);

        OrdenPago orden = adapter.findById(10L);

        assertThat(orden.comprobantes()).isEmpty();
        assertThat(orden.valores()).isEmpty();
        assertThat(orden.traza()).isEmpty();
    }
}