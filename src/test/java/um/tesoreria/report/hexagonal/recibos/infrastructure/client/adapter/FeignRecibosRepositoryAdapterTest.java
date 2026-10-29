package um.tesoreria.report.hexagonal.recibos.infrastructure.client.adapter;

import org.junit.jupiter.api.Test;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.ChequeraPagoClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.ChequeraSerieClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.ChequeraPagoDto;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.ProductoDto;
import um.tesoreria.report.hexagonal.recibos.domain.model.Recibo;
import um.tesoreria.report.hexagonal.recibos.domain.ports.out.RecibosRepository.ChequeraRef;
import um.tesoreria.report.hexagonal.recibos.infrastructure.client.FacturacionElectronicaClient;
import um.tesoreria.report.hexagonal.recibos.infrastructure.client.dto.FacturacionElectronicaDto;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeignRecibosRepositoryAdapterTest {

    private final ChequeraSerieClient serieClient = mock(ChequeraSerieClient.class);
    private final ChequeraPagoClient pagoClient = mock(ChequeraPagoClient.class);
    private final FacturacionElectronicaClient facturacionClient = mock(FacturacionElectronicaClient.class);
    private final FeignRecibosRepositoryAdapter adapter =
            new FeignRecibosRepositoryAdapter(serieClient, pagoClient, facturacionClient);

    private static FacturacionElectronicaDto factura(long id, long pagoId) {
        FacturacionElectronicaDto dto = new FacturacionElectronicaDto();
        dto.setFacturacionElectronicaId(id);
        dto.setChequeraPagoId(pagoId);
        dto.setImporte(new BigDecimal("1000"));
        dto.setCae("CAE" + id);
        return dto;
    }

    // protects=año, mes y nombre del producto salen del pago que corresponde a cada recibo; fails_when=se cruzan los pagos o se pierde el nombre; no_claim=no prueba la llamada HTTP real al core
    @Test
    void findRecibosByChequera_takesAnhoMesAndProductoFromTheMatchingPago() {
        ChequeraRef chequera = new ChequeraRef(1, 2, 100L);
        ChequeraPagoDto pagoArancel = ChequeraPagoDto.builder().chequeraPagoId(10L).productoId(1).anho(2026).mes(3)
                .producto(new ProductoDto(1, "Arancel")).build();
        ChequeraPagoDto pagoMatricula = ChequeraPagoDto.builder().chequeraPagoId(11L).productoId(2).anho(2026).mes(2)
                .producto(new ProductoDto(2, "Matrícula")).build();
        when(pagoClient.findAllByChequera(1, 2, 100L)).thenReturn(List.of(pagoArancel, pagoMatricula));
        when(facturacionClient.findAllByChequera(1, 2, 100L)).thenReturn(List.of(factura(1, 11L), factura(2, 10L)));

        List<Recibo> recibos = adapter.findRecibosByChequera(chequera);

        assertThat(recibos).extracting(Recibo::facturacionElectronicaId, Recibo::producto, Recibo::anho, Recibo::mes)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(1L, "Matrícula", 2026, 2),
                        org.assertj.core.groups.Tuple.tuple(2L, "Arancel", 2026, 3));
    }

    // protects=un recibo sin pago asociado igual se lista, con producto/año/mes nulos; fails_when=NPE o recibo omitido; no_claim=no explica por qué falta el pago
    @Test
    void findRecibosByChequera_keepsReciboWhenPagoIsMissing() {
        ChequeraRef chequera = new ChequeraRef(1, 2, 100L);
        when(pagoClient.findAllByChequera(1, 2, 100L)).thenReturn(List.of());
        when(facturacionClient.findAllByChequera(1, 2, 100L)).thenReturn(List.of(factura(1, 10L)));

        List<Recibo> recibos = adapter.findRecibosByChequera(chequera);

        assertThat(recibos).hasSize(1);
        assertThat(recibos.get(0).producto()).isNull();
        assertThat(recibos.get(0).anho()).isNull();
        assertThat(recibos.get(0).mes()).isNull();
    }
}