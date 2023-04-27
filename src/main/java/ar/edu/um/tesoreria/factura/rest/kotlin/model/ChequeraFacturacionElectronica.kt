package ar.edu.um.tesoreria.factura.rest.kotlin.model

import ar.edu.um.tesoreria.factura.rest.model.Auditable
import jakarta.persistence.*

@Entity
@Table
data class ChequeraFacturacionElectronica(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var chequeraFacturacionElectronicaId: Long? = null,

    var chequeraId: Long? = null,
    var cuit: String = "",
    var razonSocial: String = "",
    var domicilio: String = "",
    var email: String = "",
    var condicionIva: String = "",

) : Auditable()
