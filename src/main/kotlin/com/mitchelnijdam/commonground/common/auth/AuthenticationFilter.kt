package com.mitchelnijdam.commonground.common.auth

import com.mitchelnijdam.commonground.user.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Resolves the authenticated User for every /api request (except /api/health) and stores it as a
 * request attribute. In local development the Cloudflare check is bypassed with a configured email.
 */
@Component
class AuthenticationFilter(
    private val userService: UserService,
    private val jwtVerifier: CloudflareJwtVerifier,
    private val properties: AuthProperties,
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return !path.startsWith("/api") || path == "/api/health"
    }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val email = resolveEmail(request)
        if (email == null) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write("""{"error": "Authentication required"}""")
            return
        }

        request.setAttribute(USER_ATTRIBUTE, userService.resolveUser(email))
        filterChain.doFilter(request, response)
    }

    private fun resolveEmail(request: HttpServletRequest): String? {
        if (properties.devBypass.enabled) return properties.devBypass.email
        return request.getHeader(CLOUDFLARE_JWT_HEADER)?.let(jwtVerifier::verify)
    }

    companion object {
        const val USER_ATTRIBUTE = "commonground.currentUser"
        const val CLOUDFLARE_JWT_HEADER = "Cf-Access-Jwt-Assertion"
    }
}
