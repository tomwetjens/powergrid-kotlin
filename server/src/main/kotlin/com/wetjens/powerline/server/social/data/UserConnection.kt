package com.wetjens.powerline.server.social.data

import org.springframework.social.connect.ConnectionData
import java.util.*

data class UserConnection(
        val userId: UUID,
        val providerId: String,
        val providerUserId: String,
        val connectionData: ConnectionData
) {


}