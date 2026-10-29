package um.tesoreria.report.hexagonal.recibos.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.ChequeraPagoClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.ChequeraSerieClient;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.ChequeraPagoDto;
import um.tesoreria.report.hexagonal.recibos.domain.model.Recibo;
import um.tesoreria.report.hexagonal.recibos.domain.ports.out.RecibosRepository;
import um.tesoreria.report.hexagonal.recibos.infrastructure.client.FacturacionElectronicaClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeignRecibosRepositoryAdapter implements RecibosRepository {

    private final ChequeraSerieClient chequeraSerieClient;
    private final ChequeraPagoClient chequeraPagoClient;
    private final FacturacionElectronicaClient facturacionElectronicaClient;

    @Override
    public List<ChequeraRef> findChequeras(BigDecimal personaId, Integer documentoId, Integer lectivoId) {
        return chequeraSerieClient.findAllByPersonaLectivo(personaId, documentoId, lectivoId).stream()
                .map(chequera -> new ChequeraRef(chequera.getFacultadId(), chequera.getTipoChequeraId(),
                        chequera.getChequeraSerieId()))
                .toList();
    }

    @Override
    public List<Recibo> findRecibosByChequera(ChequeraRef chequera) {
        // Año, mes y producto salen del pago de la chequera; se une por chequeraPagoId.
        Map<Long, ChequeraPagoDto> pagos = chequeraPagoClient
                .findAllByChequera(chequera.facultadId(), chequera.tipoChequeraId(), chequera.chequeraSerieId())
                .stream()
                .collect(Collectors.toMap(ChequeraPagoDto::getChequeraPagoId, Function.identity(), (a, b) -> a));
        return facturacionElectronicaClient
                .findAllByChequera(chequera.facultadId(), chequera.tipoChequeraId(), chequera.chequeraSerieId())
                .stream()
                .map(dto -> {
                    ChequeraPagoDto pago = pagos.get(dto.getChequeraPagoId());
                    return new Recibo(dto.getFacturacionElectronicaId(), chequera.facultadId(),
                            chequera.tipoChequeraId(), chequera.chequeraSerieId(), dto.getChequeraPagoId(),
                            pago == null ? null : pago.getProductoId(),
                            pago == null || pago.getProducto() == null ? null : pago.getProducto().getNombre(),
                            pago == null ? null : pago.getAnho(), pago == null ? null : pago.getMes(),
                            dto.getComprobanteId(), dto.getNumeroComprobante(), dto.getImporte(),
                            dto.getFechaRecibo(), dto.getCae());
                })
                .toList();
    }
}