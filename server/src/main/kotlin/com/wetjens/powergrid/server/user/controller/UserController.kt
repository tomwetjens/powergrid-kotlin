package com.wetjens.powergrid.server.user.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/user")
class UserController {

    @RequestMapping
    fun getUser(principal: Principal): Principal {
        return principal
    }

}