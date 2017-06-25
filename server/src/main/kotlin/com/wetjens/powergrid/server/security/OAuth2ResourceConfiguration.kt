package com.wetjens.powergrid.server.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter

@Configuration
@EnableResourceServer
@EnableWebSecurity
class OAuth2ResourceConfiguration : ResourceServerConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        // Because we are both resource and authorization server,
        // we must always define a specific matcher on which authorizeRequests() is called
        // else the resource server filter will also protect any authorization server endpoints
        http.antMatcher("/user")
                .authorizeRequests()
                .anyRequest().authenticated()
    }

}