package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;
import um.tesoreria.report.util.Jsonifier;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeraSerie {
    private Long chequeraId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private BigDecimal personaId;
    private Integer documentoId;
    private Integer lectivoId;
    private Integer arancelTipoId;
    private Integer cursoId;
    private Byte asentado;
    private Integer geograficaId;
    private OffsetDateTime fecha;
    private Integer cuotasPagadas;
    private String observaciones;
    private Integer alternativaId;
    private Byte algoPagado;
    private Integer tipoImpresionId;
    private Byte flagPayperTic;
    private String usuarioId;
    private Byte enviado;
    private Byte retenida;
    private Long version;
    private Byte hpum;
    private BigDecimal becaPorcentaje;
    private String becaResolucion;
    private OffsetDateTime becaFecha;
    private Long becaUserId;
    private Integer cuotasDeuda;
    private BigDecimal importeDeuda;
    private OffsetDateTime ultimoEnvio;

    private Facultad facultad;
    private TipoChequera tipoChequera;
    private Persona persona;
    private Domicilio domicilio;
    private Lectivo lectivo;
    private ArancelTipo arancelTipo;
    private Geografica geografica;

    public String jsonify() {
        return Jsonifier.builder(this).build();
    }
}
