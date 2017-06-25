package com.wetjens.powergrid.server.social

import com.wetjens.powergrid.server.social.data.UserConnectionRepository
import org.springframework.context.annotation.Configuration
import org.springframework.social.config.annotation.EnableSocial
import org.springframework.social.config.annotation.SocialConfigurerAdapter
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.ConnectionSignUp
import org.springframework.social.connect.UsersConnectionRepository

@Configuration
@EnableSocial
class SocialConfiguration(
        private val userConnectionRepository: UserConnectionRepository,
        private val connectionSignUp: ConnectionSignUp) : SocialConfigurerAdapter() {

    override fun getUsersConnectionRepository(connectionFactoryLocator: ConnectionFactoryLocator): UsersConnectionRepository {
        return MyUsersConnectionRepository(userConnectionRepository, connectionFactoryLocator, connectionSignUp)
    }

}
