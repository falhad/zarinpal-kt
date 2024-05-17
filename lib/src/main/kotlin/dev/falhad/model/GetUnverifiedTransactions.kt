package dev.falhad.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUnverifiedTransactions(
    @SerialName("MerchantID")
    val merchantID: String
)