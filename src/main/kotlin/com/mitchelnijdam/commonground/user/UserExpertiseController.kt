package com.mitchelnijdam.commonground.user

import com.mitchelnijdam.commonground.common.auth.CurrentUser
import com.mitchelnijdam.commonground.label.LabelDto
import com.mitchelnijdam.commonground.label.toDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users/me/expertise")
class UserExpertiseController(private val userExpertiseService: UserExpertiseService) {

    @GetMapping
    fun get(@CurrentUser user: User): List<LabelDto> =
        user.expertise.sortedBy { it.name.lowercase() }.map { it.toDto() }

    @PutMapping
    fun replace(@CurrentUser user: User, @RequestBody request: UpdateExpertiseRequest): List<LabelDto> =
        userExpertiseService.replaceExpertise(user, request.labelIds)
            .sortedBy { it.name.lowercase() }
            .map { it.toDto() }
}

data class UpdateExpertiseRequest(val labelIds: List<Long> = emptyList())
