package com.novavpn.app.data

import kotlinx.serialization.Serializable

@Serializable
data class ProvisioningRequest(
    val publicKey: String
)
