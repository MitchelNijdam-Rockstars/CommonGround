package com.mitchelnijdam.commonground.suggestion

import com.mitchelnijdam.commonground.label.LabelDto
import com.mitchelnijdam.commonground.label.toDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class TopicSuggestionDto(
    val id: Long,
    val question: String,
    val context: String?,
    val language: String?,
    val labels: List<LabelDto>,
    val patterns: List<TopicSuggestionPatternDto>,
    val submittedBy: String,
    val status: SuggestionStatus,
    val rejectionReason: String?,
    val createdTopicId: Long?,
    val createdAt: Instant,
)

data class TopicSuggestionPatternDto(
    val id: Long,
    val title: String?,
    val code: String,
)

data class CreateTopicSuggestionRequest(
    @field:NotBlank @field:Size(max = 500)
    val question: String,
    val context: String? = null,
    @field:Size(max = 50)
    val language: String? = null,
    val labelIds: List<Long> = emptyList(),
    @field:Valid
    val patterns: List<CreateTopicSuggestionPatternRequest> = emptyList(),
)

data class CreateTopicSuggestionPatternRequest(
    @field:Size(max = 200)
    val title: String? = null,
    @field:NotBlank
    val code: String,
)

fun TopicSuggestion.toDto(): TopicSuggestionDto = TopicSuggestionDto(
    id = id,
    question = question,
    context = context,
    language = language,
    labels = labels.sortedBy { it.name.lowercase() }.map { it.toDto() },
    patterns = patterns.map { TopicSuggestionPatternDto(id = it.id, title = it.title, code = it.code) },
    submittedBy = user.email.substringBefore("@"),
    status = status,
    rejectionReason = rejectionReason,
    createdTopicId = createdTopic?.id,
    createdAt = createdAt,
)
