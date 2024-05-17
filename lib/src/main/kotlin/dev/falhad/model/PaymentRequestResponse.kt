package dev.falhad.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequestResponse(
    @SerialName("Status")
    val status: Int,
    @SerialName("Authority")
    val authority: String,

    var endpoint: String? = null,

    ) {
    fun webGatewayUrl() = "$endpoint$authority"
    fun zarinGatewayUrl() = "$endpoint$authority/ZarinGate"
    fun mobileGatewayUrl() = "$endpoint$authority/MobileGate"

    fun alreadyVerified() = status == 101
    fun success() = status == 100
    fun error() = status < 0
}