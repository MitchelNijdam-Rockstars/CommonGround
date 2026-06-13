package com.mitchelnijdam.commonground.user

import com.mitchelnijdam.commonground.common.auth.AuthProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val authProperties: AuthProperties,
) {

    /**
     * Looks up the User by email, creating one on first login. The role is re-resolved from the
     * admin allowlist on every login so config changes take effect without manual intervention.
     */
    @Transactional
    fun resolveUser(email: String): User {
        val role = resolveRole(email)
        val existing = userRepository.findByEmailIgnoreCase(email)

        return when {
            existing == null -> userRepository.save(User(email = email.lowercase(), role = role))
            existing.role != role -> userRepository.save(existing.apply { this.role = role })
            else -> existing
        }
    }

    private fun resolveRole(email: String): UserRole =
        if (authProperties.adminEmails.any { it.equals(email, ignoreCase = true) }) UserRole.ADMIN else UserRole.USER
}
