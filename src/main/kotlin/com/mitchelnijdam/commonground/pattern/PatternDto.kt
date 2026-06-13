package com.mitchelnijdam.commonground.pattern

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class PatternDto(
    val id: Long,
    val topicId: Long,
    val title: String,
    val code: String,
    val language: String,
    val eloRating: Double,
    val timesShown: Int,
    val timesChosen: Int,
    val winRate: Double?,
    val active: Boolean,
)

/** An anonymised voter comment: only the text and when it was left, never the voter's identity. */
data class PatternCommentDto(
    val comment: String,
    val createdAt: Instant,
)

data class PatternDetailDto(
    val id: Long,
    val topicId: Long,
    val title: String,
    val code: String,
    val language: String,
    val eloRating: Double,
    val winRate: Double?,
    val comments: List<PatternCommentDto>,
)

data class CreatePatternRequest(
    @field:NotBlank @field:Size(max = 200)
    val title: String,
    @field:NotBlank
    val code: String,
    @field:NotBlank @field:Size(max = 50)
    val language: String,
)

fun Pattern.toDto(): PatternDto = PatternDto(
    id = id,
    topicId = topic.id,
    title = title,
    code = code,
    language = language,
    eloRating = eloRating,
    timesShown = timesShown,
    timesChosen = timesChosen,
    winRate = winRate,
    active = active,
)
