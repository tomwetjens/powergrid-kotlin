package com.wetjens.powergrid.server.social

import com.wetjens.powergrid.server.social.data.UserConnectionRepository
import org.springframework.social.connect.*
import java.util.*

class MyUsersConnectionRepository(
        private val userConnectionRepository: UserConnectionRepository,
        private val connectionFactoryLocator: ConnectionFactoryLocator,
        private val connectionSignUp: ConnectionSignUp?) : UsersConnectionRepository {

    override fun findUserIdsConnectedTo(providerId: String, providerUserIds: Set<String>): Set<String> {
        return providerUserIds.mapNotNull { providerUserId ->
            val userConnection = userConnectionRepository.findByProviderIdAndProviderUserId(providerId, providerUserId)

            if (userConnection != null) userConnection.userId.toString() else null
        }.toSet()
    }

    override fun findUserIdsWithConnection(connection: Connection<*>): List<String> {
        val userConnection = userConnectionRepository.findByProviderIdAndProviderUserId(
                connection.key.providerId, connection.key.providerUserId)

        if (userConnection == null) {
            if (connectionSignUp != null) {
                val newUserId = connectionSignUp.execute(connection)
                if (newUserId != null) {
                    createConnectionRepository(newUserId).addConnection(connection)
                    return listOf(newUserId)
                }
            }

            return emptyList()
        } else {
            return listOf(userConnection.userId.toString())
        }
    }

    override fun createConnectionRepository(userId: String): ConnectionRepository {
        return MyConnectionRepository(UUID.fromString(userId), userConnectionRepository, connectionFactoryLocator)
    }

    override fun setConnectionSignUp(connectionSignUp: ConnectionSignUp) {
        // Nothing
    }

}