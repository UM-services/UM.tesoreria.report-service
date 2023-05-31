package um.tesoreria.factura.rest.kotlin.model

import um.tesoreria.factura.rest.model.Auditable
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table
data class FacturacionElectronica(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var facturacionElectronicaId: Long? = null,

    var chequeraPagoId: Long? = null,
    var comprobanteId: Int? = null,
    var numeroComprobante: Long = 0,
    var personaId: BigDecimal? = null,
    var tipoDocumento: String? = null,
    var apellido: String? = null,
    var nombre: String? = null,
    var cuit: String? = null,
    var condicionIva: String = "",
    var importe: BigDecimal = BigDecimal.ZERO,
    var cae: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var fechaRecibo: OffsetDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var fechaVencimientoCae: OffsetDateTime? = null,
    var enviada: Byte = 0,
    var retries: Int = 0,

    @OneToOne(optional = true)
    @JoinColumn(name = "chequeraPagoId", insertable = false, updatable = false)
    var chequeraPago: ChequeraPago? = null,

    @OneToOne(optional = true)
    @JoinColumn(name = "comprobanteId", insertable = false, updatable = false)
    var comprobante: Comprobante? = null,

    ) : Auditable()
