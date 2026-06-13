package com.mitchelnijdam.commonground.rankings

import com.mitchelnijdam.commonground.pattern.PatternDto
import com.mitchelnijdam.commonground.topic.TopicDto

enum class RankingAlgorithm {
    ELO,
    WIN_RATE,
}

data class TopicRankingDto(
    val topic: TopicDto,
    val totalVotes: Long,
    val patterns: List<PatternDto>,
)
