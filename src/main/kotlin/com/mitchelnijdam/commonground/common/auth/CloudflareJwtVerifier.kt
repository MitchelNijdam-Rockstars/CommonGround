package com.mitchelnijdam.commonground.common.auth

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Verifies the `Cf-Access-Jwt-Assertion` JWT that Cloudflare Access injects into every request,
 * using Cloudflare's public JWKS endpoint. Only active when a team domain is configured.
 */
@Component
class CloudflareJwtVerifier(private val properties: AuthProperties) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val processor = if (properties.cloudflare.enabled) {
        val jwksUrl = URI("https://${properties.cloudflare.teamDomain}.cloudflareaccess.com/cdn-cgi/access/certs").toURL()
        DefaultJWTProcessor<SecurityContext>().apply {
            jwsKeySelector = JWSVerificationKeySelector(JWSAlgorithm.RS256, JWKSourceBuilder.create<SecurityContext>(jwksUrl).build())
            jwtClaimsSetVerifier = DefaultJWTClaimsVerifier(
                properties.cloudflare.audience.ifBlank { null }?.let { setOf(it) },
                JWTClaimsSet.Builder().build(),
                setOf("email", "exp"),
                null,
            )
        }
    } else {
        null
    }

    /** Returns the authenticated email, or null when the token is missing, invalid or expired. */
    fun verify(token: String): String? {
        val activeProcessor = processor ?: return null
        return try {
            activeProcessor.process(token, null).getStringClaim("email")
        } catch (e: Exception) {
            log.debug("Cloudflare JWT verification failed: {}", e.message)
            null
        }
    }
}
