package com.wetjens.powergrid.server.social

import org.springframework.social.connect.web.HttpSessionSessionStrategy
import org.springframework.stereotype.Component
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.handler.DispatcherServletWebRequest
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Filter that remembers a redirect URI that is passed to {@link ProviderSignInController#signIn} as request parameter.
 */
@Component
class RememberSignInRedirectUriFilter : OncePerRequestFilter() {

    companion object {

        private object SessionAttributes {
            val redirectUri = RememberSignInRedirectUriFilter::class.qualifiedName + ".redirectUri"
        }

        private val sessionStrategy = HttpSessionSessionStrategy()

        fun getRedirectUri(request: NativeWebRequest): String? {
            return sessionStrategy.getAttribute(request, SessionAttributes.redirectUri) as String
        }

        fun setRedirectUri(request: NativeWebRequest, redirectUri: String) {
            sessionStrategy.setAttribute(request, SessionAttributes.redirectUri, redirectUri)
        }

    }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        if (request.requestURI.startsWith("/signin")) {
            val redirectUri = request.getParameter("redirect_uri")
            if (redirectUri != null) {
                setRedirectUri(DispatcherServletWebRequest(request), redirectUri)
            }
        }

        filterChain.doFilter(request, response)
    }

}