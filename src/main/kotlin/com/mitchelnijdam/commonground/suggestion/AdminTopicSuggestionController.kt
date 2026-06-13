package com.mitchelnijdam.commonground.suggestion

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/suggestions/topics")
class AdminTopicSuggestionController(private val topicSuggestionService: TopicSuggestionService) {

    @GetMapping
    fun list(@RequestParam(defaultValue = "PENDING") status: SuggestionStatus): List<TopicSuggestionDto> =
        topicSuggestionService.byStatus(status)

    @PostMapping("/{suggestionId}/approve")
    fun approve(@PathVariable suggestionId: Long): TopicSuggestionDto =
        topicSuggestionService.approve(suggestionId)

    @PostMapping("/{suggestionId}/reject")
    fun reject(
        @PathVariable suggestionId: Long,
        @Valid @RequestBody(required = false) request: RejectSuggestionRequest?,
    ): TopicSuggestionDto =
        topicSuggestionService.reject(suggestionId, request?.reason)
}
