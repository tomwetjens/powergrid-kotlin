package com.wetjens.powergrid.server.security

import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.client.OAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.Filter

@Configuration
@EnableOAuth2Client
@EnableAuthorizationServer
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
class OAuth2ServerConfiguration(private val oauth2ClientContext: OAuth2ClientContext) : WebSecurityConfigurerAdapter() {

    /**
     * Because we are also a resource server, we need to configure the client details for
     * Facebook separate from the standard "spring.oauth2.client" properties.
     */
    @Bean
    @ConfigurationProperties("facebook")
    fun facebookClientProperties(): OAuth2ClientProperties {
        return OAuth2ClientProperties()
    }

    @Bean
    fun facebookSsoFilter() = ssoFilter("/login/facebook", facebookClientProperties())

    override fun configure(http: HttpSecurity) {
        http
                .csrf().disable()
                .addFilterBefore(facebookSsoFilter(), BasicAuthenticationFilter::class.java)
                .authorizeRequests()
                .antMatchers("/", "/assets/**").permitAll()
                .antMatchers("/login/**").permitAll()
                .anyRequest().authenticated()
    }

    private fun ssoFilter(url: String, clientProperties: OAuth2ClientProperties): Filter {
        val restTemplate = OAuth2RestTemplate(clientProperties.client, oauth2ClientContext)

        val tokenServices = UserInfoTokenServices(
                clientProperties.resource.userInfoUri,
                clientProperties.client.clientId)

        tokenServices.setRestTemplate(restTemplate)

        val filter = OAuth2ClientAuthenticationProcessingFilter(url)

        filter.setTokenServices(tokenServices)
        filter.setRestTemplate(restTemplate)

        return filter
    }

}
