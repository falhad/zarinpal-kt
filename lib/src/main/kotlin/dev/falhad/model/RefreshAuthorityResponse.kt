package dev.falhad.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class RefreshAuthorityResponse(
    @SerialName("Status")
    val status: Int
)
