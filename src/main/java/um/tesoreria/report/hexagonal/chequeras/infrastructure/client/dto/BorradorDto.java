package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import um.tesoreria.report.util.Jsonifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Espejo de {@code BorradorResponse} del core (sólo los campos que usa report).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorradorDto {

    private Long borradorId;
    private String estado;
    private BigDecimal personaId;
    private Integer documentoId;
    private String personaApellido;
    private String personaNombre;
    private Integer facultadId;
    private String facultadNombre;
    private Integer geograficaId;
    private String geograficaNombre;
    private Integer lectivoId;
    private String lectivoNombre;
    private Integer tipoChequeraId;
    private String tipoChequeraNombre;
    private Integer arancelTipoId;
    private Integer alternativaId;
    private Integer cursoId;
    private Byte hpum;
    private BigDecimal becaPorcentaje;
    private List<CuotaDto> cuotas;
    private List<AlternativaDto> alternativas;
    private List<ArancelTipoDto> arancelTipos;

    public String jsonify() {
        return Jsonifier.builder(this).build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CuotaDto {
        private Long borradorCuotaId;
        private Integer productoId;
        private Integer alternativaId;
        private Integer cuotaId;
        private Integer mes;
        private Integer anio;
        private OffsetDateTime vencimiento1;
        private OffsetDateTime vencimiento2;
        private OffsetDateTime vencimiento3;
        private BigDecimal importe1Original;
        private BigDecimal importe2Original;
        private BigDecimal importe3Original;
        private BigDecimal importe1Borrador;
        private BigDecimal importe2Borrador;
        private BigDecimal importe3Borrador;
        private Byte pagado;
        private Byte baja;
        private Byte manual;
        private Byte compensada;
        private Byte bloqueado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlternativaDto {
        private Integer alternativaId;
        private Integer productoId;
        private String titulo;
        private Integer cuotas;
        private Integer descuentoPagoTermino;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArancelTipoDto {
        private Integer arancelTipoId;
        private String nombre;
    }
}
