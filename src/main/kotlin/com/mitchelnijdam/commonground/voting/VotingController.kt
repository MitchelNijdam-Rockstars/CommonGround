package com.mitchelnijdam.commonground.voting

import com.mitchelnijdam.commonground.common.auth.CurrentUser
import com.mitchelnijdam.commonground.user.User
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/voting")
class VotingController(
    private val matchupService: MatchupService,
    private val votingService: VotingService,
) {

    @GetMapping("/matchup")
    fun matchup(@CurrentUser user: User): MatchupDto =
        matchupService.generateBatch(user, 1).firstOrNull()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No matchups available")

    @GetMapping("/matchups")
    fun matchups(@CurrentUser user: User, @RequestParam(defaultValue = "10") count: Int): List<MatchupDto> =
        matchupService.generateBatch(user, count.coerceIn(1, 25))

    @GetMapping("/open-topic-count")
    fun openTopicCount(@CurrentUser user: User): OpenTopicCountDto =
        OpenTopicCountDto(matchupService.countOpenTopics(user))

    @PostMapping("/vote")
    fun vote(@CurrentUser user: User, @Valid @RequestBody request: VoteRequest): VoteResultDto =
        votingService.castVote(user, request.winnerPatternId, request.loserPatternId, request.comment)

    @PostMapping("/skip")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun skip(@CurrentUser user: User, @Valid @RequestBody request: SkipRequest) {
        votingService.recordSkip(user, request.patternAId, request.patternBId, request.reason)
    }
}
