package com.mitchelnijdam.commonground.suggestion

import com.mitchelnijdam.commonground.common.auth.CurrentUser
import com.mitchelnijdam.commonground.user.User
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class PatternSuggestionController(private val patternSuggestionService: PatternSuggestionService) {

    @PostMapping("/api/topics/{topicId}/suggestions/patterns")
    @ResponseStatus(HttpStatus.CREATED)
    fun submit(
        @CurrentUser user: User,
        @PathVariable topicId: Long,
        @Valid @RequestBody request: CreatePatternSuggestionRequest,
    ): PatternSuggestionDto =
        patternSuggestionService.submit(user, topicId, request.title, request.code)

    @GetMapping("/api/users/me/suggestions/patterns")
    fun mySuggestions(@CurrentUser user: User): List<PatternSuggestionDto> =
        patternSuggestionService.forUser(user)
}
