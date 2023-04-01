package ar.edu.um.tesoreria.factura.rest.kotlin.model

import ar.edu.um.tesoreria.factura.rest.model.Auditable
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["chp_fac_id", "chp_tch_id", "chp_chs_id", "chp_pro_id", "chp_alt_id", "chp_cuo_id", "chp_orden"])])
data class ChequeraPago(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clave")
    var chequeraPagoId: Long? = null,

    @Column(name = "chp_fac_id")
    var facultadId: Int? = null,

    @Column(name = "chp_tch_id")
    var tipoChequeraId: Int? = null,

    @Column(name = "chp_chs_id")
    var chequeraSerieId: Long? = null,

    @Column(name = "chp_pro_id")
    var productoId: Int? = null,

    @Column(name = "chp_alt_id")
    var alternativaId: Int? = null,

    @Column(name = "chp_cuo_id")
    var cuotaId: Int? = null,

    @Column(name = "chp_orden")
    var orden: Int? = null,

    @Column(name = "chp_mes")
    var mes: Int = 0,

    @Column(name = "chp_anio")
    var anho: Int = 0,

    @Column(name = "chp_fecha")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var fecha: OffsetDateTime? = null,

    @Column(name = "chp_acred")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    var acreditacion: OffsetDateTime? = null,

    @Column(name = "chp_importe")
    var importe: BigDecimal = BigDecimal.ZERO,

    @Column(name = "chp_path")
    var path: String = "",

    @Column(name = "chp_archivo")
    var archivo: String = "",

    @Column(name = "chp_observaciones")
    var observaciones: String = "",

    @Column(name = "chp_arb_id")
    var archivoBancoId: Long? = null,

    @Column(name = "chp_arb_id_acred")
    var archivoBancoIdAcreditacion: Long? = null,

    var verificador: Int = 0,

    @Column(name = "chp_tpa_id")
    var tipoPagoId: Int? = null,

    ) : Auditable() {

    fun getCuotaKey(): String {
        return (this.facultadId.toString() + "." + this.tipoChequeraId + "." + this.chequeraSerieId + "." + this.productoId + "."
                + this.alternativaId + "." + this.cuotaId)
    }

}
