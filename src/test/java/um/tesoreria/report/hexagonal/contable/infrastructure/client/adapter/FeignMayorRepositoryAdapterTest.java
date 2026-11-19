package um.tesoreria.report.hexagonal.contable.infrastructure.client.adapter;

import org.junit.jupiter.api.Test;
import um.tesoreria.report.hexagonal.contable.domain.model.Mayor;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorSeleccion;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.MayorClient;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.MayorConsultaDto;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.MayorCuentaDto;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.MayorMovimientoDto;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.MayorSaldoInicialDto;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.MayorSolicitudDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeignMayorRepositoryAdapterTest {

    private final MayorClient client = mock(MayorClient.class);
    private final FeignMayorRepositoryAdapter adapter = new FeignMayorRepositoryAdapter(client);

    private static OffsetDateTime fecha(int anio, int mes, int dia) {
        return OffsetDateTime.of(anio, mes, dia, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    // protects=el pedido al core lleva exactamente las fechas y la selección recibidas (lista o rango); fails_when=se pierde un campo, se completa con valores por defecto o se mezclan lista y rango; no_claim=no prueba la validación (es del core)
    @Test
    void consultar_sendsTheSelectionAsReceived() {
        MayorSeleccion seleccion = new MayorSeleccion(fecha(2026, 3, 1), fecha(2026, 3, 31),
                List.of(new BigDecimal("10101010001")), null, null);
        when(client.consultar(new MayorSolicitudDto(fecha(2026, 3, 1), fecha(2026, 3, 31),
                List.of(new BigDecimal("10101010001")), null, null))).thenReturn(new MayorConsultaDto());

        adapter.consultar(seleccion);

        verify(client).consultar(new MayorSolicitudDto(fecha(2026, 3, 1), fecha(2026, 3, 31),
                List.of(new BigDecimal("10101010001")), null, null));
    }

    // protects=la respuesta del core se pasa al dominio campo por campo, con cuenta, ejercicio, saldos y totales; fails_when=se pierde un campo o se cambia de lugar un importe; no_claim=no prueba el contenido contable
    @Test
    void consultar_mapsEveryField() {
        MayorSaldoInicialDto inicial = new MayorSaldoInicialDto();
        inicial.setDebe(new BigDecimal("100.00"));
        inicial.setHaber(new BigDecimal("40.00"));
        inicial.setSaldo(new BigDecimal("60.00"));
        MayorMovimientoDto movimiento = new MayorMovimientoDto();
        movimiento.setFechaContable(fecha(2026, 3, 2));
        movimiento.setOrdenContable(7);
        movimiento.setEjercicioId(26);
        movimiento.setEjercicioNombre("Ejercicio Económico 2026");
        movimiento.setComprobante("Factura");
        movimiento.setProveedor("Proveedor SA");
        movimiento.setConcepto("Compra");
        movimiento.setOrdenPago("OP / 1 / 55");
        movimiento.setBeneficiario("Juan");
        movimiento.setDebe(new BigDecimal("50.00"));
        movimiento.setSaldo(new BigDecimal("110.00"));
        movimiento.setOrdenPagoValor("1/56");
        movimiento.setValor("Cheque");
        movimiento.setNumeroValor(12345L);
        movimiento.setImporteValor(new BigDecimal("50.00"));
        MayorCuentaDto cuenta = new MayorCuentaDto();
        cuenta.setNumeroCuenta(new BigDecimal("10101010001"));
        cuenta.setNombre("Caja");
        cuenta.setSaldoInicial(inicial);
        cuenta.setMovimientos(List.of(movimiento));
        cuenta.setTotalDebe(new BigDecimal("50.00"));
        cuenta.setTotalHaber(new BigDecimal("0.00"));
        cuenta.setSaldoFinal(new BigDecimal("110.00"));
        MayorConsultaDto respuesta = new MayorConsultaDto();
        respuesta.setDesde(fecha(2026, 3, 1));
        respuesta.setHasta(fecha(2026, 3, 31));
        respuesta.setCuentas(List.of(cuenta));
        when(client.consultar(new MayorSolicitudDto(null, null, null, null, null))).thenReturn(respuesta);

        Mayor mayor = adapter.consultar(new MayorSeleccion(null, null, null, null, null));

        assertThat(mayor.desde()).isEqualTo(fecha(2026, 3, 1));
        var cuentaDominio = mayor.cuentas().getFirst();
        assertThat(cuentaDominio.numeroCuenta()).isEqualByComparingTo("10101010001");
        assertThat(cuentaDominio.nombre()).isEqualTo("Caja");
        assertThat(cuentaDominio.saldoInicial().saldo()).isEqualByComparingTo("60.00");
        assertThat(cuentaDominio.totalDebe()).isEqualByComparingTo("50.00");
        assertThat(cuentaDominio.saldoFinal()).isEqualByComparingTo("110.00");
        var linea = cuentaDominio.movimientos().getFirst();
        assertThat(linea.ejercicioId()).isEqualTo(26);
        assertThat(linea.ejercicioNombre()).isEqualTo("Ejercicio Económico 2026");
        assertThat(linea.ordenPago()).isEqualTo("OP / 1 / 55");
        assertThat(linea.beneficiario()).isEqualTo("Juan");
        assertThat(linea.debe()).isEqualByComparingTo("50.00");
        assertThat(linea.haber()).isNull();
        assertThat(linea.numeroValor()).isEqualTo(12345L);
    }

    // protects=una respuesta vacía o sin cuentas da un mayor sin cuentas y no una excepción; fails_when=NullPointerException; no_claim=el core no devuelve cuerpo vacío en la práctica
    @Test
    void consultar_handlesAnEmptyResponse() {
        when(client.consultar(new MayorSolicitudDto(null, null, null, null, null))).thenReturn(null);

        Mayor mayor = adapter.consultar(new MayorSeleccion(null, null, null, null, null));

        assertThat(mayor.cuentas()).isEmpty();
    }
}