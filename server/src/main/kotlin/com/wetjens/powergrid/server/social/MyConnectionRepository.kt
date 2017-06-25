package com.wetjens.powergrid.server.social

import com.wetjens.powergrid.server.social.data.UserConnection
import com.wetjens.powergrid.server.social.data.UserConnectionRepository
import org.springframework.social.connect.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

class MyConnectionRepository(private val userId: UUID,
                             private val userConnectionRepository: UserConnectionRepository,
                             private val connectionFactoryLocator: ConnectionFactoryLocator) : ConnectionRepository {

    override fun updateConnection(connection: Connection<*>) {
        val userConnection = userConnectionRepository.findByUserIdAndProviderIdAndProviderUserId(
                userId, connection.key.providerId, connection.key.providerUserId)

        if (userConnection != null) {
            userConnectionRepository.save(userConnection.copy(connectionData = connection.createData()))
        } else {
            throw NoSuchConnectionException(connection.key)
        }
    }

    override fun removeConnection(connectionKey: ConnectionKey) {
        val userConnection = userConnectionRepository.findByUserIdAndProviderIdAndProviderUserId(
                userId, connectionKey.providerId, connectionKey.providerUserId)

        if (userConnection != null) {
            userConnectionRepository.delete(userConnection)
        } else {
            throw NoSuchConnectionException(connectionKey)
        }
    }

    override fun findConnections(providerId: String): List<Connection<*>> {
        val connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId)

        val userConnections = userConnectionRepository.findByUserIdAndProviderId(userId, providerId)

        return userConnections.map { userConnection -> connectionFactory.createConnection(userConnection.connectionData) }
    }

    override fun <A : Any> findConnections(apiType: Class<A>): List<Connection<A>> {
        val connectionFactory = connectionFactoryLocator.getConnectionFactory(apiType)

        val userConnections = userConnectionRepository.findByUserIdAndProviderId(userId, connectionFactory.providerId)

        return userConnections.map { userConnection -> connectionFactory.createConnection(userConnection.connectionData) }
    }

    override fun getConnection(connectionKey: ConnectionKey): Connection<*> {
        val userConnection = userConnectionRepository.findByUserIdAndProviderIdAndProviderUserId(
                userId, connectionKey.providerId, connectionKey.providerUserId)

        if (userConnection != null) {
            val connectionFactory = connectionFactoryLocator.getConnectionFactory(userConnection.providerId)

            return connectionFactory.createConnection(userConnection.connectionData)
        } else {
            throw NoSuchConnectionException(connectionKey)
        }
    }

    override fun <A : Any> getConnection(apiType: Class<A>, providerUserId: String): Connection<A> {
        val connectionFactory = connectionFactoryLocator.getConnectionFactory(apiType)

        val userConnection = userConnectionRepository.findByUserIdAndProviderIdAndProviderUserId(
                userId, connectionFactory.providerId, providerUserId)

        if (userConnection != null) {
            return connectionFactory.createConnection(userConnection.connectionData)
        } else {
            throw NoSuchConnectionException(ConnectionKey(connectionFactory.providerId, providerUserId))
        }
    }

    override fun findConnectionsToUsers(providerUserIds: MultiValueMap<String, String>): MultiValueMap<String, Connection<*>?> {
        return LinkedMultiValueMap(providerUserIds.mapValues { (providerId, providerUserIds) ->
            val connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId)

            providerUserIds.map { providerUserId ->
                val userConnection = userConnectionRepository.findByUserIdAndProviderIdAndProviderUserId(
                        userId, providerId, providerUserId)

                if (userConnection != null)
                    connectionFactory.createConnection(userConnection.connectionData)
                else null
            }
        })
    }

    override fun addConnection(connection: Connection<*>) {
        val existingConnection = userConnectionRepository.findByUserIdAndProviderIdAndProviderUserId(
                userId, connection.key.providerId, connection.key.providerUserId)

        if (existingConnection != null) {
            throw DuplicateConnectionException(connection.key)
        }

        userConnectionRepository.save(UserConnection(
                userId = userId,
                providerId = connection.key.providerId,
                providerUserId = connection.key.providerUserId,
                connectionData = connection.createData()
        ))
    }

    override fun <A : Any> getPrimaryConnection(apiType: Class<A>): Connection<A> {
        val primaryConnection = findPrimaryConnection(apiType)

        return if (primaryConnection != null)
            primaryConnection
        else {
            val connectionFactory = connectionFactoryLocator.getConnectionFactory(apiType)
            throw NotConnectedException(connectionFactory.providerId)
        }
    }

    override fun findAllConnections(): MultiValueMap<String, Connection<*>> {
        return LinkedMultiValueMap(connectionFactoryLocator.registeredProviderIds().associate { providerId ->
            val connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId)

            val userConnections = userConnectionRepository.findByUserIdAndProviderId(userId, providerId)

            Pair(providerId, userConnections.map { userConnection ->
                connectionFactory.createConnection(userConnection.connectionData)
            })
        })
    }

    override fun <A : Any> findPrimaryConnection(apiType: Class<A>): Connection<A>? {
        val connectionFactory = connectionFactoryLocator.getConnectionFactory(apiType)

        val userConnections = userConnectionRepository.findByUserIdAndProviderId(userId, connectionFactory.providerId)

        return when (userConnections.size) {
            0 -> null
            1 -> connectionFactory.createConnection(userConnections[0].connectionData)
            else -> throw IllegalStateException("Multiple connections found for provider: ${connectionFactory.providerId}")
        }
    }

    override fun removeConnections(providerId: String) {
        val userConnections = userConnectionRepository.findByUserIdAndProviderId(userId, providerId)

        userConnectionRepository.deleteAll(userConnections)
    }

}