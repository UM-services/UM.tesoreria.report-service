package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper;

import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.*;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChequerasMapper {

    public ArancelTipo toDomain(ArancelTipoDto dto) {
        if (dto == null) return null;
        return ArancelTipo.builder()
                .arancelTipoId(dto.getArancelTipoId())
                .descripcion(dto.getDescripcion())
                .medioArancel(dto.getMedioArancel())
                .arancelTipoIdCompleto(dto.getArancelTipoIdCompleto())
                .build();
    }

    public ClaseChequera toDomain(ClaseChequeraDto dto) {
        if (dto == null) return null;
        return ClaseChequera.builder()
                .claseChequeraId(dto.getClaseChequeraId())
                .nombre(dto.getNombre())
                .preuniversitario(dto.getPreuniversitario())
                .grado(dto.getGrado())
                .posgrado(dto.getPosgrado())
                .curso(dto.getCurso())
                .secundario(dto.getSecundario())
                .titulo(dto.getTitulo())
                .build();
    }

    public Geografica toDomain(GeograficaDto dto) {
        if (dto == null) return null;
        return Geografica.builder()
                .geograficaId(dto.getGeograficaId())
                .nombre(dto.getNombre())
                .sinChequera(dto.getSinChequera())
                .build();
    }

    public Lectivo toDomain(LectivoDto dto) {
        if (dto == null) return null;
        return Lectivo.builder()
                .lectivoId(dto.getLectivoId())
                .nombre(dto.getNombre())
                .fechaInicio(dto.getFechaInicio())
                .fechaFinal(dto.getFechaFinal())
                .build();
    }

    public Plan toDomain(PlanDto dto) {
        if (dto == null) return null;
        return Plan.builder()
                .uniqueId(dto.getUniqueId())
                .facultadId(dto.getFacultadId())
                .planId(dto.getPlanId())
                .nombre(dto.getNombre())
                .fecha(dto.getFecha())
                .publicar(dto.getPublicar())
                .build();
    }

    public Producto toDomain(ProductoDto dto) {
        if (dto == null) return null;
        return Producto.builder()
                .productoId(dto.getProductoId())
                .nombre(dto.getNombre())
                .build();
    }

    public TipoPago toDomain(TipoPagoDto dto) {
        if (dto == null) return null;
        return TipoPago.builder()
                .tipoPagoId(dto.getTipoPagoId())
                .nombre(dto.getNombre())
                .build();
    }

    public CuotaPeriodo toDomain(CuotaPeriodoDto dto) {
        if (dto == null) return null;
        return CuotaPeriodo.builder()
                .mes(dto.getMes())
                .anho(dto.getAnho())
                .cantidad(dto.getCantidad())
                .build();
    }

    public Persona toDomain(PersonaDto dto) {
        if (dto == null) return null;
        return Persona.builder()
                .uniqueId(dto.getUniqueId())
                .personaId(dto.getPersonaId())
                .documentoId(dto.getDocumentoId())
                .apellido(dto.getApellido())
                .nombre(dto.getNombre())
                .sexo(dto.getSexo())
                .primero(dto.getPrimero())
                .cuit(dto.getCuit())
                .cbu(dto.getCbu())
                .password(dto.getPassword())
                .build();
    }

    public Facultad toDomain(FacultadDto dto) {
        if (dto == null) return null;
        return Facultad.builder()
                .facultadId(dto.getFacultadId())
                .nombre(dto.getNombre())
                .codigoempresa(dto.getCodigoempresa())
                .server(dto.getServer())
                .dbadm(dto.getDbadm())
                .dsn(dto.getDsn())
                .cuentacontable(dto.getCuentacontable())
                .apiserver(dto.getApiserver())
                .apiport(dto.getApiport())
                .build();
    }

    public Domicilio toDomain(DomicilioDto dto) {
        if (dto == null) return null;
        return Domicilio.builder()
                .domicilioId(dto.getDomicilioId())
                .personaId(dto.getPersonaId())
                .documentoId(dto.getDocumentoId())
                .fecha(dto.getFecha())
                .calle(dto.getCalle())
                .puerta(dto.getPuerta())
                .piso(dto.getPiso())
                .dpto(dto.getDpto())
                .telefono(dto.getTelefono())
                .movil(dto.getMovil())
                .observaciones(dto.getObservaciones())
                .codigoPostal(dto.getCodigoPostal())
                .facultadId(dto.getFacultadId())
                .provinciaId(dto.getProvinciaId())
                .localidadId(dto.getLocalidadId())
                .emailPersonal(dto.getEmailPersonal())
                .emailInstitucional(dto.getEmailInstitucional())
                .laboral(dto.getLaboral())
                .build();
    }

    public TipoChequera toDomain(TipoChequeraDto dto) {
        if (dto == null) return null;
        return TipoChequera.builder()
                .tipoChequeraId(dto.getTipoChequeraId())
                .nombre(dto.getNombre())
                .prefijo(dto.getPrefijo())
                .geograficaId(dto.getGeograficaId())
                .claseChequeraId(dto.getClaseChequeraId())
                .imprimir(dto.getImprimir())
                .contado(dto.getContado())
                .multiple(dto.getMultiple())
                .emailCopia(dto.getEmailCopia())
                .geografica(toDomain(dto.getGeografica()))
                .claseChequera(toDomain(dto.getClaseChequera()))
                .build();
    }

    public ChequeraSerie toDomain(ChequeraSerieDto dto) {
        if (dto == null) return null;
        return ChequeraSerie.builder()
                .chequeraId(dto.getChequeraId())
                .facultadId(dto.getFacultadId())
                .tipoChequeraId(dto.getTipoChequeraId())
                .chequeraSerieId(dto.getChequeraSerieId())
                .personaId(dto.getPersonaId())
                .documentoId(dto.getDocumentoId())
                .lectivoId(dto.getLectivoId())
                .arancelTipoId(dto.getArancelTipoId())
                .cursoId(dto.getCursoId())
                .asentado(dto.getAsentado())
                .geograficaId(dto.getGeograficaId())
                .fecha(dto.getFecha())
                .cuotasPagadas(dto.getCuotasPagadas())
                .observaciones(dto.getObservaciones())
                .alternativaId(dto.getAlternativaId())
                .algoPagado(dto.getAlgoPagado())
                .tipoImpresionId(dto.getTipoImpresionId())
                .flagPayperTic(dto.getFlagPayperTic())
                .usuarioId(dto.getUsuarioId())
                .enviado(dto.getEnviado())
                .retenida(dto.getRetenida())
                .version(dto.getVersion())
                .hpum(dto.getHpum())
                .becaPorcentaje(dto.getBecaPorcentaje())
                .becaResolucion(dto.getBecaResolucion())
                .becaFecha(dto.getBecaFecha())
                .becaUserId(dto.getBecaUserId())
                .cuotasDeuda(dto.getCuotasDeuda())
                .importeDeuda(dto.getImporteDeuda())
                .ultimoEnvio(dto.getUltimoEnvio())
                .facultad(toDomain(dto.getFacultad()))
                .tipoChequera(toDomain(dto.getTipoChequera()))
                .persona(toDomain(dto.getPersona()))
                .domicilio(toDomain(dto.getDomicilio()))
                .lectivo(toDomain(dto.getLectivo()))
                .arancelTipo(toDomain(dto.getArancelTipo()))
                .geografica(toDomain(dto.getGeografica()))
                .build();
    }

    public ChequeraCuota toDomain(ChequeraCuotaDto dto) {
        if (dto == null) return null;
        return ChequeraCuota.builder()
                .chequeraCuotaId(dto.getChequeraCuotaId())
                .chequeraId(dto.getChequeraId())
                .facultadId(dto.getFacultadId())
                .tipoChequeraId(dto.getTipoChequeraId())
                .chequeraSerieId(dto.getChequeraSerieId())
                .productoId(dto.getProductoId())
                .alternativaId(dto.getAlternativaId())
                .cuotaId(dto.getCuotaId())
                .mes(dto.getMes())
                .anho(dto.getAnho())
                .arancelTipoId(dto.getArancelTipoId())
                .vencimiento1(dto.getVencimiento1())
                .importe1(dto.getImporte1())
                .importe1Original(dto.getImporte1Original())
                .vencimiento2(dto.getVencimiento2())
                .importe2(dto.getImporte2())
                .importe2Original(dto.getImporte2Original())
                .vencimiento3(dto.getVencimiento3())
                .importe3(dto.getImporte3())
                .importe3Original(dto.getImporte3Original())
                .codigoBarras(dto.getCodigoBarras())
                .i2Of5(dto.getI2Of5())
                .pagado(dto.getPagado())
                .baja(dto.getBaja())
                .manual(dto.getManual())
                .compensada(dto.getCompensada())
                .tramoId(dto.getTramoId())
                .facultad(toDomain(dto.getFacultad()))
                .tipoChequera(toDomain(dto.getTipoChequera()))
                .producto(toDomain(dto.getProducto()))
                .chequeraSerie(toDomain(dto.getChequeraSerie()))
                .build();
    }

    public Carrera toDomain(CarreraDto dto) {
        if (dto == null) return null;
        return Carrera.builder()
                .uniqueId(dto.getUniqueId())
                .facultadId(dto.getFacultadId())
                .planId(dto.getPlanId())
                .carreraId(dto.getCarreraId())
                .nombre(dto.getNombre())
                .iniciales(dto.getIniciales())
                .titulo(dto.getTitulo())
                .trabajofinal(dto.getTrabajofinal())
                .resolucion(dto.getResolucion())
                .chequeraunica(dto.getChequeraunica())
                .bloqueId(dto.getBloqueId())
                .obligatorias(dto.getObligatorias())
                .optativas(dto.getOptativas())
                .vigente(dto.getVigente())
                .plan(toDomain(dto.getPlan()))
                .build();
    }

    public Legajo toDomain(LegajoDto dto) {
        if (dto == null) return null;
        return Legajo.builder()
                .legajoId(dto.getLegajoId())
                .personaId(dto.getPersonaId())
                .documentoId(dto.getDocumentoId())
                .facultadId(dto.getFacultadId())
                .numeroLegajo(dto.getNumeroLegajo())
                .fecha(dto.getFecha())
                .lectivoId(dto.getLectivoId())
                .planId(dto.getPlanId())
                .carreraId(dto.getCarreraId())
                .tieneCarrera(dto.getTieneCarrera())
                .geograficaId(dto.getGeograficaId())
                .contrasenha(dto.getContrasenha())
                .intercambio(dto.getIntercambio())
                .carrera(toDomain(dto.getCarrera()))
                .build();
    }

    public ChequeraPago toDomain(ChequeraPagoDto dto) {
        if (dto == null) return null;
        return ChequeraPago.builder()
                .chequeraPagoId(dto.getChequeraPagoId())
                .chequeraCuotaId(dto.getChequeraCuotaId())
                .facultadId(dto.getFacultadId())
                .tipoChequeraId(dto.getTipoChequeraId())
                .chequeraSerieId(dto.getChequeraSerieId())
                .productoId(dto.getProductoId())
                .alternativaId(dto.getAlternativaId())
                .cuotaId(dto.getCuotaId())
                .orden(dto.getOrden())
                .mes(dto.getMes())
                .anho(dto.getAnho())
                .fecha(dto.getFecha())
                .acreditacion(dto.getAcreditacion())
                .importe(dto.getImporte())
                .path(dto.getPath())
                .archivo(dto.getArchivo())
                .observaciones(dto.getObservaciones())
                .archivoBancoId(dto.getArchivoBancoId())
                .archivoBancoIdAcreditacion(dto.getArchivoBancoIdAcreditacion())
                .verificador(dto.getVerificador())
                .tipoPagoId(dto.getTipoPagoId())
                .idMercadoPago(dto.getIdMercadoPago())
                .tipoPago(toDomain(dto.getTipoPago()))
                .producto(toDomain(dto.getProducto()))
                .chequeraCuota(toDomain(dto.getChequeraCuota()))
                .build();
    }

    public ChequeraCuotaPagos toDomain(ChequeraCuotaPagosDto dto) {
        if (dto == null) return null;
        List<ChequeraPago> pagos = dto.getChequeraPagos() != null
                ? dto.getChequeraPagos().stream().map(this::toDomain).collect(Collectors.toList())
                : Collections.emptyList();

        return ChequeraCuotaPagos.builder()
                .chequeraCuotaId(dto.getChequeraCuotaId())
                .chequeraId(dto.getChequeraId())
                .facultadId(dto.getFacultadId())
                .tipoChequeraId(dto.getTipoChequeraId())
                .chequeraSerieId(dto.getChequeraSerieId())
                .productoId(dto.getProductoId())
                .alternativaId(dto.getAlternativaId())
                .cuotaId(dto.getCuotaId())
                .mes(dto.getMes())
                .anho(dto.getAnho())
                .arancelTipoId(dto.getArancelTipoId())
                .vencimiento1(dto.getVencimiento1())
                .importe1(dto.getImporte1())
                .importe1Original(dto.getImporte1Original())
                .vencimiento2(dto.getVencimiento2())
                .importe2(dto.getImporte2())
                .importe2Original(dto.getImporte2Original())
                .vencimiento3(dto.getVencimiento3())
                .importe3(dto.getImporte3())
                .importe3Original(dto.getImporte3Original())
                .codigoBarras(dto.getCodigoBarras())
                .i2Of5(dto.getI2Of5())
                .pagado(dto.getPagado())
                .baja(dto.getBaja())
                .manual(dto.getManual())
                .compensada(dto.getCompensada())
                .tramoId(dto.getTramoId())
                .producto(toDomain(dto.getProducto()))
                .chequeraPagos(pagos)
                .build();
    }
}
