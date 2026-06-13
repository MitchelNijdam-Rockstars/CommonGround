package com.mitchelnijdam.commonground.suggestion

import com.mitchelnijdam.commonground.common.auth.CurrentUser
import com.mitchelnijdam.commonground.user.User
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class TopicSuggestionController(private val topicSuggestionService: TopicSuggestionService) {

    @PostMapping("/api/suggestions/topics")
    @ResponseStatus(HttpStatus.CREATED)
    fun submit(
        @CurrentUser user: User,
        @Valid @RequestBody request: CreateTopicSuggestionRequest,
    ): TopicSuggestionDto =
        topicSuggestionService.submit(user, request.question, request.context, request.labelIds)

    @GetMapping("/api/users/me/suggestions/topics")
    fun mySuggestions(@CurrentUser user: User): List<TopicSuggestionDto> =
        topicSuggestionService.forUser(user)
}
