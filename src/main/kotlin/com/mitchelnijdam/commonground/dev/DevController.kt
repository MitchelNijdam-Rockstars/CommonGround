package com.mitchelnijdam.commonground.dev

import com.mitchelnijdam.commonground.common.auth.CurrentUser
import com.mitchelnijdam.commonground.user.User
import com.mitchelnijdam.commonground.voting.SkipRepository
import com.mitchelnijdam.commonground.voting.VoteRepository
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Local-only helper endpoints for manual testing. Annotated [Profile] "local" so the bean never
 * exists in prod — these are deliberately unauthenticated beyond the normal [CurrentUser] resolution.
 */
@Profile("local")
@RestController
@RequestMapping("/api/dev")
class DevController(
    private val voteRepository: VoteRepository,
    private val skipRepository: SkipRepository,
) {

    /**
     * Deletes the current user's votes and skips so every topic becomes votable again — letting you
     * vote repeatedly as if you were multiple users. ELO ratings are left untouched on purpose.
     * Returns how many rows were removed.
     */
    @PostMapping("/voting/reset")
    @Transactional
    fun resetMyVotes(@CurrentUser user: User): ResetResult {
        val votes = voteRepository.deleteByUserId(user.id)
        val skips = skipRepository.deleteByUserId(user.id)
        return ResetResult(deletedVotes = votes, deletedSkips = skips)
    }
}

data class ResetResult(val deletedVotes: Long, val deletedSkips: Long)
