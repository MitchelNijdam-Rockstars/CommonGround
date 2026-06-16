package com.mitchelnijdam.commonground.voting

import com.mitchelnijdam.commonground.pattern.PatternDto
import com.mitchelnijdam.commonground.topic.TopicDto
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class MatchupDto(
    val topic: TopicDto,
    val patterns: List<PatternDto>,
    val topicVoteCount: Long,
)

data class VoteRequest(
    val winnerPatternId: Long,
    @field:NotEmpty
    val beatenPatternIds: List<Long>,
    @field:Size(max = 500)
    val comment: String? = null,
)

data class VoteResultDto(
    val voteId: Long,
    val winnerNewRating: Double,
    val currentStreak: Int,
)

data class OpenTopicCountDto(val count: Long)

data class SkipRequest(
    val topicId: Long,
    val reason: SkipReason,
)
