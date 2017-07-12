package com.wetjens.powerline.server.social.data

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserConnectionRepository : CrudRepository<UserConnection, UUID> {

    fun findByUserIdAndProviderId(userId: UUID, providerId: String): List<UserConnection>

    fun findByUserIdAndProviderIdAndProviderUserId(userId: UUID, providerId: String, providerUserId: String): UserConnection?

    fun findByProviderIdAndProviderUserId(providerId: String, providerUserId: String): UserConnection?

}