package dev.falhad.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PaymentMetadata(
    @SerialName("mobile")
    var mobile: String? = null,
    @SerialName("email")
    var email: String? = null,
    @SerialName("order_id")
    var orderId: String? = null,
)

@Serializable
data class PaymentRequest(
    @SerialName("merchant_id")
    var merchantId: String,
    @SerialName("currency")
    var currency: String? = null,
    @SerialName("amount")
    var amount: Int,
    @SerialName("description")
    var description: String,
    @SerialName("callback_url")
    var callbackURL: String,
    @SerialName("metadata")
    var metadata: PaymentMetadata,


    ) {
    fun isValid(): Boolean {
        if (amount < 1) throw Throwable("amount must be a positive number (current: $amount)")
        if (callbackURL.isBlank()) throw Throwable("callbackURL should not be empty")
        if (description.isBlank()) throw Throwable("description should not be empty")
        if (currency !in listOf("IRT", "IRR")) throw Throwable("currency must be one of IRT or IRR values.")
        return true
    }

}