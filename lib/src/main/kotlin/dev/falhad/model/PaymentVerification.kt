package dev.falhad.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentVerification(
    @SerialName("merchant_id")
    val merchantId: String,
    @SerialName("authority")
    val authority: String,
    @SerialName("amount")
    val amount: Int
){
    fun isValid(): Boolean {
        if (amount < 1) throw Throwable("amount must be a positive number (current: $amount)")
        if (authority.isBlank()) throw Throwable("authority should not be empty")
        return true
    }
}