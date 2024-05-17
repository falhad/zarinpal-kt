package dev.falhad.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshAuthority(
    @SerialName("MerchantID")
    val merchantID: String,
    @SerialName("Authority")
    val authority: String,
    @SerialName("ExpireIn")
    val expireIn: Int
)