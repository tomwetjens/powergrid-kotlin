package com.wetjens.powergrid.server.social

import com.wetjens.powergrid.server.user.User
import com.wetjens.powergrid.server.user.UserRepository
import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionSignUp
import org.springframework.stereotype.Component

@Component
class MyConnectionSignUp(private val userRepository: UserRepository) : ConnectionSignUp {

    override fun execute(connection: Connection<*>): String {
        val user = userRepository.save(User(displayName = connection.displayName))

        return user.id.toString()
    }

}