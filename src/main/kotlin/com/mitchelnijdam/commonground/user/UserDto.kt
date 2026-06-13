package com.mitchelnijdam.commonground.user

data class UserDto(
    val email: String,
    val displayName: String,
    val role: UserRole,
    val currentStreak: Int,
    val logoutUrl: String?,
)

fun User.toDto(logoutUrl: String?): UserDto = UserDto(
    email = email,
    displayName = email.substringBefore("@").replace('.', ' ').replaceFirstChar { it.uppercase() },
    role = role,
    currentStreak = effectiveStreak(),
    logoutUrl = logoutUrl,
)
