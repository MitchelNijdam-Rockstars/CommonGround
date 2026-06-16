package com.mitchelnijdam.commonground.voting

import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.pattern.PatternRepository
import com.mitchelnijdam.commonground.topic.TopicRepository
import com.mitchelnijdam.commonground.user.User
import com.mitchelnijdam.commonground.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Service
class VotingService(
    private val patternRepository: PatternRepository,
    private val voteRepository: VoteRepository,
    private val skipRepository: SkipRepository,
    private val userRepository: UserRepository,
    private val topicRepository: TopicRepository,
) {

    /**
     * Records a Vote: the winner was picked over every pattern in [beatenPatternIds] (one for a
     * pairwise vote, N-1 when all of a topic's patterns are shown). Atomically updates ELO and
     * win-rate counters for every pattern involved: either the Vote and all updates land, or none.
     */
    @Transactional
    fun castVote(user: User, winnerPatternId: Long, beatenPatternIds: List<Long>, comment: String? = null): VoteResultDto {
        val (winner, beaten) = loadVotePatterns(winnerPatternId, beatenPatternIds)

        val (newWinnerRating, newLoserRatings) =
            EloCalculator.ratingsAfterWinAgainstAll(winner.eloRating, beaten.map { it.eloRating })
        winner.eloRating = newWinnerRating
        winner.timesShown += 1
        winner.timesChosen += 1
        beaten.forEachIndexed { i, loser ->
            loser.eloRating = newLoserRatings[i]
            loser.timesShown += 1
        }
        patternRepository.saveAll(listOf(winner) + beaten)

        val voter = userRepository.findById(user.id).orElseThrow()
        val vote = voteRepository.save(
            Vote(
                user = voter,
                topic = winner.topic,
                winnerPattern = winner,
                beatenPatterns = beaten.toSet(),
                comment = comment?.trim()?.ifBlank { null },
            ),
        )
        val streak = updateStreak(voter)
        return VoteResultDto(
            voteId = vote.id,
            winnerNewRating = newWinnerRating,
            currentStreak = streak,
        )
    }

    /**
     * A streak counts consecutive calendar days with at least one Vote: unchanged when the user
     * already voted today, +1 when the last vote was yesterday, back to 1 after a missed day.
     */
    private fun updateStreak(voter: User, today: LocalDate = LocalDate.now()): Int {
        when (voter.lastVotedDate) {
            today -> Unit
            today.minusDays(1) -> voter.currentStreak += 1
            else -> voter.currentStreak = 1
        }
        voter.lastVotedDate = today
        userRepository.save(voter)
        return voter.currentStreak
    }

    /** Records a topic-level Skip with its reason. ELO ratings and counters are deliberately untouched. */
    @Transactional
    fun recordSkip(user: User, topicId: Long, reason: SkipReason) {
        val topic = topicRepository.findById(topicId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Topic $topicId not found") }
        skipRepository.save(Skip(user = user, topic = topic, reason = reason))
    }

    private fun loadVotePatterns(winnerId: Long, beatenIds: List<Long>): Pair<Pattern, List<Pattern>> {
        if (winnerId in beatenIds) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The winner cannot also be a beaten pattern")
        }
        if (beatenIds.toSet().size != beatenIds.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Beaten patterns must be distinct")
        }
        val winner = loadActivePattern(winnerId)
        val beaten = beatenIds.map { loadActivePattern(it) }
        if (beaten.any { it.topic.id != winner.topic.id }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Patterns must belong to the same topic")
        }
        return winner to beaten
    }

    private fun loadActivePattern(patternId: Long): Pattern {
        val pattern = patternRepository.findById(patternId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Pattern $patternId not found") }
        if (!pattern.active) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Pattern $patternId is no longer active")
        }
        return pattern
    }
}
