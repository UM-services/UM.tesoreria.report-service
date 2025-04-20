package um.tesoreria.report.domain.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarreraDto {
    private Long uniqueId;
    private Integer facultadId;
    private Integer planId;
    private Integer carreraId;
    private String nombre;
    private String iniciales;
    private String titulo;
    private Byte trabajofinal;
    private String resolucion;
    private Byte chequeraunica;
    private Integer bloqueId;
    private Integer obligatorias;
    private Integer optativas;
    private Byte vigente;
    private PlanDto plan;
} 