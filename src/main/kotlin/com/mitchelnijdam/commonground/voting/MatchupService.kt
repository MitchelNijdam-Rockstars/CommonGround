package com.mitchelnijdam.commonground.voting

import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.pattern.PatternRepository
import com.mitchelnijdam.commonground.pattern.toDto
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
     * Generates up to [count] matchups for the user. Each matchup comes from a distinct random
     * Topic (with at least two active Patterns), so no Topic or pair repeats within a batch.
     * Pairs the user has voted or skipped on before are avoided as long as unseen pairs exist.
     */
    @Transactional(readOnly = true)
    fun generateBatch(user: User, count: Int): List<MatchupDto> {
        val eligibleTopicIds = patternRepository.findTopicIdsWithAtLeastTwoActivePatterns().toSet()
        if (eligibleTopicIds.isEmpty()) return emptyList()

        val seenPairs = voteRepository.findVotedPairKeysByUserId(user.id) +
            skipRepository.findSkippedPairKeysByUserId(user.id)

        val patternsByTopic = patternRepository.findByActiveTrue()
            .filter { it.topic.id in eligibleTopicIds }
            .groupBy { it.topic }
            .filterKeys { topic -> matchesExpertise(user, topic.labels.map { it.id }) }

        return patternsByTopic.entries
            .shuffled()
            .mapNotNull { (topic, patterns) -> pickPair(patterns, seenPairs)?.let { topic to it } }
            // stable sort: topics that still have an unseen pair come first, shuffle order otherwise kept
            .sortedBy { (_, pick) -> pick.alreadySeen }
            .take(count)
            .map { (topic, pick) ->
                MatchupDto(
                    topic = topic.toDto(),
                    patternA = pick.pair.first.toDto(),
                    patternB = pick.pair.second.toDto(),
                    topicVoteCount = voteRepository.countByTopicId(topic.id),
                )
            }
    }

    /** Topics open for this user: matchup-eligible and matching their LANGUAGE expertise filter. */
    @Transactional(readOnly = true)
    fun countOpenTopics(user: User): Long {
        val eligibleTopicIds = patternRepository.findTopicIdsWithAtLeastTwoActivePatterns().toSet()
        if (eligibleTopicIds.isEmpty()) return 0

        return patternRepository.findByActiveTrue()
            .filter { it.topic.id in eligibleTopicIds }
            .map { it.topic }
            .distinctBy { it.id }
            .count { topic -> matchesExpertise(user, topic.labels.map { it.id }) }
            .toLong()
    }

    /** No expertise selected means no filter; otherwise at least one expertise Label must match. */
    private fun matchesExpertise(user: User, topicLabelIds: List<Long>): Boolean {
        val expertiseIds = user.expertise.map { it.id }.toSet()
        return expertiseIds.isEmpty() || topicLabelIds.any { it in expertiseIds }
    }

    private data class PairPick(val pair: Pair<Pattern, Pattern>, val alreadySeen: Boolean)

    /**
     * Picks a random pair, preferring pairs the user has not seen. Falls back to a random seen
     * pair so a fully-explored Topic still produces matchups instead of starving the feed.
     */
    private fun pickPair(patterns: List<Pattern>, seenPairs: Set<String>): PairPick? {
        if (patterns.size < 2) return null

        val allPairs = patterns.flatMapIndexed { i, a ->
            patterns.drop(i + 1).map { b -> a to b }
        }
        val unseen = allPairs.filter { (a, b) -> pairKey(a.id, b.id) !in seenPairs }

        return if (unseen.isNotEmpty()) {
            PairPick(unseen.random().shuffledPair(), alreadySeen = false)
        } else {
            PairPick(allPairs.random().shuffledPair(), alreadySeen = true)
        }
    }

    private fun Pair<Pattern, Pattern>.shuffledPair(): Pair<Pattern, Pattern> =
        if (listOf(true, false).random()) this else second to first

    companion object {
        fun pairKey(patternId1: Long, patternId2: Long): String =
            "${minOf(patternId1, patternId2)}:${maxOf(patternId1, patternId2)}"
    }
}
