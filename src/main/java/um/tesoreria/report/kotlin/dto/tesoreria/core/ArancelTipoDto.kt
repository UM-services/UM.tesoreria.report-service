package um.tesoreria.report.kotlin.dto.tesoreria.core

data class ArancelTipoDto(

    var arancelTipoId: Int? = null,
    var descripcion: String = "",
    var medioArancel: Byte = 0,
    var arancelTipoIdCompleto: Int? = null

)
