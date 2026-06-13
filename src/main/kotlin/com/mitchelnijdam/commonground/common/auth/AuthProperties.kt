package com.mitchelnijdam.commonground.common.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("commonground.auth")
data class AuthProperties(
    val adminEmails: List<String> = emptyList(),
    val cloudflare: Cloudflare = Cloudflare(),
    val devBypass: DevBypass = DevBypass(),
) {
    data class Cloudflare(
        val teamDomain: String = "",
        val audience: String = "",
    ) {
        val enabled: Boolean get() = teamDomain.isNotBlank()
        val logoutUrl: String? get() = if (enabled) "https://$teamDomain.cloudflareaccess.com/cdn-cgi/access/logout" else null
    }

    data class DevBypass(
        val enabled: Boolean = false,
        val email: String = "dev@local.test",
    )
}
