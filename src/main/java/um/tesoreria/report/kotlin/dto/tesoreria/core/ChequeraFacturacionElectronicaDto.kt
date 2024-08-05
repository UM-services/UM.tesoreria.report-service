package um.tesoreria.report.kotlin.dto.tesoreria.core

data class ChequeraFacturacionElectronicaDto(

    var chequeraFacturacionElectronicaId: Long? = null,
    var chequeraId: Long? = null,
    var cuit: String = "",
    var razonSocial: String = "",
    var domicilio: String = "",
    var email: String = "",
    var condicionIva: String = ""

)
