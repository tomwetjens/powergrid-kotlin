package com.wetjens.powergrid.server.user

import org.springframework.data.annotation.Id
import java.util.*

data class User(
        @Id val id: UUID = UUID.randomUUID(),
        val displayName: String)