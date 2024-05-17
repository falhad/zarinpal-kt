package dev.falhad.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentVerification(
    @SerialName("MerchantID")
    val merchantID: String,
    @SerialName("Authority")
    val authority: String,
    @SerialName("Amount")
    val amount: Int
){
    fun isValid(): Boolean {
        if (amount < 1) throw Throwable("amount must be a positive number (current: $amount)")
        if (authority.isBlank()) throw Throwable("authority should not be empty")
        return true
    }
}