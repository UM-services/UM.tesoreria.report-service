package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Carrera {
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
    private Plan plan;
}
