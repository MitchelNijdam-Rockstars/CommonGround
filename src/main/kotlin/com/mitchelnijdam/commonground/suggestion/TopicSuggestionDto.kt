package com.mitchelnijdam.commonground.suggestion

import com.mitchelnijdam.commonground.label.LabelDto
import com.mitchelnijdam.commonground.label.toDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class TopicSuggestionDto(
    val id: Long,
    val question: String,
    val context: String?,
    val labels: List<LabelDto>,
    val status: SuggestionStatus,
    val rejectionReason: String?,
    val createdTopicId: Long?,
    val createdAt: Instant,
)

data class CreateTopicSuggestionRequest(
    @field:NotBlank @field:Size(max = 500)
    val question: String,
    val context: String? = null,
    val labelIds: List<Long> = emptyList(),
)

fun TopicSuggestion.toDto(): TopicSuggestionDto = TopicSuggestionDto(
    id = id,
    question = question,
    context = context,
    labels = labels.sortedBy { it.name.lowercase() }.map { it.toDto() },
    status = status,
    rejectionReason = rejectionReason,
    createdTopicId = createdTopic?.id,
    createdAt = createdAt,
)
