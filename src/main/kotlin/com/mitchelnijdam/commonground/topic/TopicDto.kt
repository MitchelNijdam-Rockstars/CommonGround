package com.mitchelnijdam.commonground.topic

import com.mitchelnijdam.commonground.label.LabelDto
import com.mitchelnijdam.commonground.label.toDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class TopicDto(
    val id: Long,
    val question: String,
    val context: String?,
    val language: String?,
    val labels: List<LabelDto>,
)

data class CreateTopicRequest(
    @field:NotBlank @field:Size(max = 500)
    val question: String,
    val context: String? = null,
    @field:Size(max = 50)
    val language: String? = null,
    val labelIds: List<Long> = emptyList(),
)

fun Topic.toDto(): TopicDto = TopicDto(
    id = id,
    question = question,
    context = context,
    language = language,
    labels = labels.sortedBy { it.name.lowercase() }.map { it.toDto() },
)
