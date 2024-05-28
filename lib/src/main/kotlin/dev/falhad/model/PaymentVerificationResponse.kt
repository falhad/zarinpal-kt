package dev.falhad.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
//
//@Serializable
//data class PaymentVerificationResponse(
//    @SerialName("data")
//    var `data`: PaymentVerificationData? = null,
//    @SerialName("errors")
//    var errors: Errors? = null,
//){
//    fun success() = data?.code == 100
//    fun error() = (data?.code ?: -1000) < 0
//}
//
//@Serializable
//data class PaymentVerificationData(
//    @SerialName("card_hash")
//    var cardHash: String? = null,
//    @SerialName("card_pan")
//    var cardPan: String? = null,
//    @SerialName("code")
//    var code: Int? = null,
//    @SerialName("fee")
//    var fee: Int? = null,
//    @SerialName("fee_type")
//    var feeType: String? = null,
//    @SerialName("message")
//    var message: String? = null,
//    @SerialName("ref_id")
//    var refId: Int? = null
//)
//
