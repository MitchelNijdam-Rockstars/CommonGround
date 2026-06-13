package com.mitchelnijdam.commonground.voting

import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.pattern.PatternRepository
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
) {

    /**
     * Records a Vote and atomically updates both Patterns' ELO ratings and win-rate counters.
     * Everything happens in one transaction: either the Vote and both rating updates land, or none.
     */
    @Transactional
    fun castVote(user: User, winnerPatternId: Long, loserPatternId: Long, comment: String? = null): VoteResultDto {
        val (winner, loser) = loadMatchupPatterns(winnerPatternId, loserPatternId)

        val (newWinnerRating, newLoserRating) = EloCalculator.ratingsAfterWin(winner.eloRating, loser.eloRating)
        winner.eloRating = newWinnerRating
        winner.timesShown += 1
        winner.timesChosen += 1
        loser.eloRating = newLoserRating
        loser.timesShown += 1
        patternRepository.saveAll(listOf(winner, loser))

        val voter = userRepository.findById(user.id).orElseThrow()
        val vote = voteRepository.save(
            Vote(
                user = voter,
                topic = winner.topic,
                winnerPattern = winner,
                loserPattern = loser,
                comment = comment?.trim()?.ifBlank { null },
            ),
        )
        val streak = updateStreak(voter)
        return VoteResultDto(
            voteId = vote.id,
            winnerNewRating = newWinnerRating,
            loserNewRating = newLoserRating,
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

    /** Records a Skip with its reason. ELO ratings and counters are deliberately untouched. */
    @Transactional
    fun recordSkip(user: User, patternAId: Long, patternBId: Long, reason: SkipReason) {
        val (patternA, patternB) = loadMatchupPatterns(patternAId, patternBId)
        skipRepository.save(
            Skip(user = user, topic = patternA.topic, patternA = patternA, patternB = patternB, reason = reason),
        )
    }

    private fun loadMatchupPatterns(firstId: Long, secondId: Long): Pair<Pattern, Pattern> {
        if (firstId == secondId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A matchup needs two distinct patterns")
        }
        val first = loadActivePattern(firstId)
        val second = loadActivePattern(secondId)
        if (first.topic.id != second.topic.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Patterns must belong to the same topic")
        }
        return first to second
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
