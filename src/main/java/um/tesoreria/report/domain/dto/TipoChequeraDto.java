package um.tesoreria.report.domain.dto;

import lombok.Data;

@Data
public class TipoChequeraDto {
    private Integer tipoChequeraId;
    private String nombre = "";
    private String prefijo = "";
    private Integer geograficaId = 1;
    private Integer claseChequeraId = 2;
    private Byte imprimir = 0;
    private Byte contado = 0;
    private Byte multiple = 0;
    private GeograficaDto geografica;
    private ClaseChequeraDto claseChequera;
}
