package um.tesoreria.factura.rest.kotlin.model

import um.tesoreria.factura.rest.model.Auditable
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.Column

@Entity
@Table
data class Geografica(

	@Id
	@Column(name = "geo_id")
	var geograficaId: Int? = null,

	@Column(name = "geo_nombre")
	var nombre: String = "",

	@Column(name = "geo_sinchequera")
	var sinChequera: Byte = 0

) : Auditable()