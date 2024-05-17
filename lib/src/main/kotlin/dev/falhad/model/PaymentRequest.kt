package dev.falhad.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param amount price in Toman
 */

@Serializable
data class PaymentRequest(
    @SerialName("MerchantID")
    var merchantID: String,
    @SerialName("Amount")
    var amount: Int,
    @SerialName("Description")
    var description: String,
    @SerialName("CallbackURL")
    var callbackURL: String,
    @SerialName("Email")
    var email: String? = null,
    @SerialName("Mobile")
    var mobile: String? = null,

    @SerialName("AdditionalData")
    val additionalData: String? = null,

    ) {
    fun isValid(): Boolean {
        if (amount < 1) throw Throwable("amount must be a positive number (current: $amount)")
        if (callbackURL.isBlank()) throw Throwable("callbackURL should not be empty")
        if (description.isBlank()) throw Throwable("description should not be empty")
        return true
    }
}