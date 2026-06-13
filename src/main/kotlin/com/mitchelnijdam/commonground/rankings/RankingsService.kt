package com.mitchelnijdam.commonground.rankings

import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.pattern.PatternRepository
import com.mitchelnijdam.commonground.pattern.toDto
import com.mitchelnijdam.commonground.topic.TopicRepository
import com.mitchelnijdam.commonground.topic.toDto
import com.mitchelnijdam.commonground.voting.VoteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RankingsService(
    private val topicRepository: TopicRepository,
    private val patternRepository: PatternRepository,
    private val voteRepository: VoteRepository,
) {

    @Transactional(readOnly = true)
    fun leaderboard(algorithm: RankingAlgorithm): List<TopicRankingDto> {
        val activePatternsByTopic = patternRepository.findByActiveTrue().groupBy { it.topic.id }

        return topicRepository.findAll()
            .mapNotNull { topic ->
                val patterns = activePatternsByTopic[topic.id] ?: return@mapNotNull null
                TopicRankingDto(
                    topic = topic.toDto(),
                    totalVotes = voteRepository.countByTopicId(topic.id),
                    patterns = patterns.sortedWith(comparator(algorithm)).map { it.toDto() },
                )
            }
    }

    private fun comparator(algorithm: RankingAlgorithm): Comparator<Pattern> = when (algorithm) {
        RankingAlgorithm.ELO -> compareByDescending { it.eloRating }
        // Patterns with no votes (null winRate) rank last.
        RankingAlgorithm.WIN_RATE -> compareByDescending { it.winRate ?: -1.0 }
    }
}
