package com.mitchelnijdam.commonground.user

import com.mitchelnijdam.commonground.common.auth.AuthProperties
import com.mitchelnijdam.commonground.common.auth.CurrentUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users/me")
class UserController(private val authProperties: AuthProperties) {

    @GetMapping
    fun me(@CurrentUser user: User): UserDto = user.toDto(authProperties.cloudflare.logoutUrl)
}
