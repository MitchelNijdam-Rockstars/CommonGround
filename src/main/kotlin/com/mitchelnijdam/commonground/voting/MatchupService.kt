package com.mitchelnijdam.commonground.voting

import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.pattern.PatternRepository
import com.mitchelnijdam.commonground.pattern.toDto
import com.mitchelnijdam.commonground.topic.Topic
import com.mitchelnijdam.commonground.topic.toDto
import com.mitchelnijdam.commonground.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MatchupService(
    private val patternRepository: PatternRepository,
    private val voteRepository: VoteRepository,
    private val skipRepository: SkipRepository,
) {

    /**
     * Generates up to [count] matchups for the user. Each matchup is a distinct Topic (with at
     * least two active Patterns) showing all of its active Patterns at once, in random order.
     * Topics the user already voted or skipped are excluded, so every Topic is seen once.
     */
    @Transactional(readOnly = true)
    fun generateBatch(user: User, count: Int): List<MatchupDto> {
        val topics = openTopics(user)
        if (topics.isEmpty()) return emptyList()

        return topics
            .shuffled()
            .take(count)
            .map { (topic, patterns) ->
                MatchupDto(
                    topic = topic.toDto(),
                    patterns = patterns.shuffled().map { it.toDto() },
                    topicVoteCount = voteRepository.countByTopicId(topic.id),
                )
            }
    }

    /** Topics open for this user: matchup-eligible, expertise-matching, and not yet voted/skipped. */
    @Transactional(readOnly = true)
    fun countOpenTopics(user: User): Long = openTopics(user).size.toLong()

    private fun openTopics(user: User): List<Pair<Topic, List<Pattern>>> {
        val eligibleTopicIds = patternRepository.findTopicIdsWithAtLeastTwoActivePatterns().toSet()
        if (eligibleTopicIds.isEmpty()) return emptyList()

        val seenTopicIds = voteRepository.findVotedTopicIdsByUserId(user.id) +
            skipRepository.findSkippedTopicIdsByUserId(user.id)

        return patternRepository.findByActiveTrue()
            .filter { it.topic.id in eligibleTopicIds && it.topic.id !in seenTopicIds }
            .groupBy { it.topic }
            .filterKeys { topic -> matchesExpertise(user, topic.labels.map { it.id }) }
            .map { (topic, patterns) -> topic to patterns }
    }

    /** No expertise selected means no filter; otherwise at least one expertise Label must match. */
    private fun matchesExpertise(user: User, topicLabelIds: List<Long>): Boolean {
        val expertiseIds = user.expertise.map { it.id }.toSet()
        return expertiseIds.isEmpty() || topicLabelIds.any { it in expertiseIds }
    }
}
