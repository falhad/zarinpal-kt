package dev.falhad.model

import kotlinx.serialization.Serializable

@Serializable
data class GetUnverifiedTransactionsResponse(
    val status: Int,
    val authorities: String
)