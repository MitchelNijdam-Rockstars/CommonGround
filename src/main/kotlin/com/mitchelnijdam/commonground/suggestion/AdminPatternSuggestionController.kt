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
@RequestMapping("/api/admin/suggestions/patterns")
class AdminPatternSuggestionController(private val patternSuggestionService: PatternSuggestionService) {

    @GetMapping
    fun list(@RequestParam(defaultValue = "PENDING") status: SuggestionStatus): List<PatternSuggestionDto> =
        patternSuggestionService.byStatus(status)

    @PostMapping("/{suggestionId}/approve")
    fun approve(@PathVariable suggestionId: Long): PatternSuggestionDto =
        patternSuggestionService.approve(suggestionId)

    @PostMapping("/{suggestionId}/reject")
    fun reject(
        @PathVariable suggestionId: Long,
        @Valid @RequestBody(required = false) request: RejectSuggestionRequest?,
    ): PatternSuggestionDto =
        patternSuggestionService.reject(suggestionId, request?.reason)
}
