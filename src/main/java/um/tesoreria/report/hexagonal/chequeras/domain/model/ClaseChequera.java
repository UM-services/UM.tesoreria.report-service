package um.tesoreria.report.hexagonal.chequeras.domain.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaseChequera {
    private Integer claseChequeraId;
    private String nombre;
    private Byte preuniversitario;
    private Byte grado;
    private Byte posgrado;
    private Byte curso;
    private Byte secundario;
    private Byte titulo;
}
