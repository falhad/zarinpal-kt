package dev.falhad.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SuccessPaymentRequestResponse(
    @SerialName("data")
    var `data`: PaymentRequestData? = null,
    @SerialName("errors")
    var errors: Errors? = null,
)

@Serializable
data class ErrorPaymentRequestResponse(
    @SerialName("data")
    var `data`: Array<String> = arrayOf(),
    @SerialName("errors")
    var errors: Errors? = null,
)



@Serializable
data class PaymentRequestData(
    @SerialName("authority")
    var authority: String? = null,
    @SerialName("code")
    var code: Int? = null,
    @SerialName("fee")
    var fee: Int? = null,
    @SerialName("fee_type")
    var feeType: String? = null,
    @SerialName("message")
    var message: String? = null,


    var endpoint: String? = null,
) {
    fun success() = code == 100
    fun error() = (code ?: -1000) < 0

    fun webGatewayUrl() = "$endpoint${authority}"
    fun zarinGatewayUrl() = "$endpoint${authority}/ZarinGate"
    fun mobileGatewayUrl() = "$endpoint${authority}/MobileGate"

}

@Serializable
data class Validation(
    @SerialName("merchant_id")
    var merchantId: String? = null
)


@Serializable
data class Errors(
    @SerialName("code")
    var code: Int? = null,
    @SerialName("message")
    var message: String? = null,
    @SerialName("validations")
    var validations: List<Validation>? = null
)


