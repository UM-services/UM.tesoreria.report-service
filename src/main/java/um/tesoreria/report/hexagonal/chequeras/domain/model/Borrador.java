package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Borrador de chequera abierto, tal como lo expone el core para la exportación por sede.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Borrador {

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

    @Builder.Default
    private List<BorradorCuota> cuotas = new ArrayList<>();
    @Builder.Default
    private List<BorradorAlternativa> alternativas = new ArrayList<>();
    @Builder.Default
    private List<BorradorArancelTipo> arancelTipos = new ArrayList<>();
}
