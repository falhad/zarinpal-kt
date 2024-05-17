package dev.falhad.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentVerificationResponse(
    @SerialName("Status")
    val status: Int,
    @SerialName("RefID")
    val refID: Long,
    @SerialName("ExtraDetail")
    val extraDetail: String? = null

){
    fun success() = status == 100
    fun error() = status < 0
}