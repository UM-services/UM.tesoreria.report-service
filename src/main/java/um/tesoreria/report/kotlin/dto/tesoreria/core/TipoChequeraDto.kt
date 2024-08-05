package um.tesoreria.report.kotlin.dto.tesoreria.core

data class TipoChequeraDto(

    var tipoChequeraId: Int? = null,
    var nombre: String = "",
    var prefijo: String = "",
    var geograficaId: Int = 1,
    var claseChequeraId: Int = 2,
    var imprimir: Byte = 0,
    var contado: Byte = 0,
    var multiple: Byte = 0,
    var geografica: GeograficaDto? = null,
    var claseChequera: ClaseChequeraDto? = null

)
