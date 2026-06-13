package com.mitchelnijdam.commonground.suggestion

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class PatternSuggestionDto(
    val id: Long,
    val topicId: Long,
    val topicQuestion: String,
    val title: String?,
    val code: String,
    val language: String,
    val status: SuggestionStatus,
    val rejectionReason: String?,
    val createdAt: Instant,
)

data class CreatePatternSuggestionRequest(
    @field:Size(max = 200)
    val title: String? = null,
    @field:NotBlank
    val code: String,
    @field:NotBlank @field:Size(max = 50)
    val language: String,
)

data class RejectSuggestionRequest(
    @field:Size(max = 500)
    val reason: String? = null,
)

fun PatternSuggestion.toDto(): PatternSuggestionDto = PatternSuggestionDto(
    id = id,
    topicId = topic.id,
    topicQuestion = topic.question,
    title = title,
    code = code,
    language = language,
    status = status,
    rejectionReason = rejectionReason,
    createdAt = createdAt,
)
