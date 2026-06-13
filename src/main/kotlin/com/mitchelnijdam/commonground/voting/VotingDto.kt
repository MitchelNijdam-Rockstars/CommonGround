package com.mitchelnijdam.commonground.voting

import com.mitchelnijdam.commonground.pattern.PatternDto
import com.mitchelnijdam.commonground.topic.TopicDto
import jakarta.validation.constraints.Size

data class MatchupDto(
    val topic: TopicDto,
    val patternA: PatternDto,
    val patternB: PatternDto,
    val topicVoteCount: Long,
)

data class VoteRequest(
    val winnerPatternId: Long,
    val loserPatternId: Long,
    @field:Size(max = 500)
    val comment: String? = null,
)

data class VoteResultDto(
    val voteId: Long,
    val winnerNewRating: Double,
    val loserNewRating: Double,
    val currentStreak: Int,
)

data class OpenTopicCountDto(val count: Long)

data class SkipRequest(
    val patternAId: Long,
    val patternBId: Long,
    val reason: SkipReason,
)
