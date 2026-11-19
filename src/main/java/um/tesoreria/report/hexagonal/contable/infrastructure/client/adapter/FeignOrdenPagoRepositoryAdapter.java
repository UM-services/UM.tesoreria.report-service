package um.tesoreria.report.hexagonal.contable.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.contable.domain.model.OrdenPago;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.OrdenPagoRepository;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.OrdenPagoClient;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.OrdenPagoImpresionDto;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FeignOrdenPagoRepositoryAdapter implements OrdenPagoRepository {

    private final OrdenPagoClient client;

    @Override
    public OrdenPago findById(Long proveedorMovimientoId) {
        OrdenPagoImpresionDto dto = client.impresion(proveedorMovimientoId);
        return new OrdenPago(dto.getProveedorMovimientoId(), dto.getPrefijo(), dto.getNumeroComprobante(),
                dto.getFechaComprobante(), dto.getFechaAnulacion(), dto.getConcepto(), dto.getRazonSocial(),
                dto.getDomicilio(), dto.getCuit(),
                lista(dto.getComprobantes()).stream().map(c -> new OrdenPago.Comprobante(c.getComprobanteDescripcion(),
                        c.getPrefijo(), c.getNumeroComprobante(), c.getFechaComprobante(), c.getImporteAplicado())).toList(),
                dto.getTotalComprobantes(),
                lista(dto.getValores()).stream().map(v -> new OrdenPago.Valor(v.getConcepto(), v.getNombreTitular(),
                        v.getNumero(), v.getFechaEmision(), v.getImporte())).toList(),
                dto.getTotalValores(),
                lista(dto.getTraza()).stream().map(t -> new OrdenPago.Traza(t.getNumeroCuenta(), t.getNombreCuenta(),
                        t.getDebe(), t.getHaber())).toList(),
                dto.getTotalDebe(), dto.getTotalHaber());
    }

    private static <T> List<T> lista(List<T> items) {
        return items == null ? List.of() : items.stream().filter(Objects::nonNull).toList();
    }
}