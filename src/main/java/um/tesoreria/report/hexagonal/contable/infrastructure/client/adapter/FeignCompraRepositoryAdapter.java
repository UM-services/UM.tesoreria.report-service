package um.tesoreria.report.hexagonal.contable.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.contable.domain.model.Compra;
import um.tesoreria.report.hexagonal.contable.domain.model.TipoComprobante;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.CompraRepository;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.ComprobanteTipoRepository;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.CompraClient;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.CompraImpresionDto;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FeignCompraRepositoryAdapter implements CompraRepository, ComprobanteTipoRepository {

    private final CompraClient client;

    @Override
    public Compra findById(Long proveedorMovimientoId) {
        CompraImpresionDto dto = client.impresion(proveedorMovimientoId);
        return new Compra(dto.getProveedorMovimientoId(), dto.getComprobanteDescripcion(), dto.getNumero(),
                dto.getFechaComprobante(), dto.getFechaAnulacion(), dto.getConcepto(), dto.getRazonSocial(),
                dto.getCuit(), dto.getImporte(),
                lista(dto.getDetalle()).stream().map(d -> new Compra.Detalle(d.getArticulo(), d.getConcepto(),
                        d.getCantidad(), d.getPrecioUnitario(), d.getPrecioFinal())).toList(),
                dto.getFechaContable(), dto.getOrdenContable(),
                lista(dto.getAsiento()).stream().map(a -> new Compra.Asiento(a.getNumeroCuenta(), a.getNombreCuenta(),
                        a.getDebe(), a.getHaber())).toList(),
                dto.getTotalDebe(), dto.getTotalHaber());
    }

    @Override
    public TipoComprobante findTipo(Long proveedorMovimientoId) {
        return TipoComprobante.desde(client.detalle(proveedorMovimientoId).getTipo());
    }

    private static <T> List<T> lista(List<T> items) {
        return items == null ? List.of() : items.stream().filter(Objects::nonNull).toList();
    }
}