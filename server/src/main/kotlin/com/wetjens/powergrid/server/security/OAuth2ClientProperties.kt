package com.wetjens.powergrid.server.security

import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails

class OAuth2ClientProperties {
    @NestedConfigurationProperty val client: AuthorizationCodeResourceDetails = AuthorizationCodeResourceDetails()
    @NestedConfigurationProperty val resource: ResourceServerProperties = ResourceServerProperties()
}