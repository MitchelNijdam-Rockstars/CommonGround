package com.mitchelnijdam.commonground.suggestion

import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.pattern.PatternRepository
import com.mitchelnijdam.commonground.topic.TopicRepository
import com.mitchelnijdam.commonground.user.User
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class PatternSuggestionService(
    private val patternSuggestionRepository: PatternSuggestionRepository,
    private val patternRepository: PatternRepository,
    private val topicRepository: TopicRepository,
) {

    @Transactional
    fun submit(user: User, topicId: Long, title: String?, code: String, language: String): PatternSuggestionDto {
        val topic = topicRepository.findById(topicId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Topic $topicId not found") }
        return patternSuggestionRepository.save(
            PatternSuggestion(
                topic = topic,
                user = user,
                title = title?.ifBlank { null },
                code = code,
                language = language,
            ),
        ).toDto()
    }

    @Transactional(readOnly = true)
    fun forUser(user: User): List<PatternSuggestionDto> =
        patternSuggestionRepository.findByUserIdOrderByCreatedAtDesc(user.id).map { it.toDto() }

    @Transactional(readOnly = true)
    fun byStatus(status: SuggestionStatus): List<PatternSuggestionDto> =
        patternSuggestionRepository.findByStatusOrderByCreatedAtAsc(status).map { it.toDto() }

    /**
     * Approval creates a fresh Pattern in the voting pool (initial ELO, zero counters) and leaves
     * the suggestion behind as an APPROVED historical record — it is never promoted itself.
     */
    @Transactional
    fun approve(suggestionId: Long): PatternSuggestionDto {
        val suggestion = loadPending(suggestionId)
        val pattern = patternRepository.save(
            Pattern(
                topic = suggestion.topic,
                title = suggestion.title ?: "Community suggestion #${suggestion.id}",
                code = suggestion.code,
                language = suggestion.language,
            ),
        )
        suggestion.status = SuggestionStatus.APPROVED
        suggestion.createdPattern = pattern
        suggestion.reviewedAt = Instant.now()
        return patternSuggestionRepository.save(suggestion).toDto()
    }

    @Transactional
    fun reject(suggestionId: Long, reason: String?): PatternSuggestionDto {
        val suggestion = loadPending(suggestionId)
        suggestion.status = SuggestionStatus.REJECTED
        suggestion.rejectionReason = reason?.ifBlank { null }
        suggestion.reviewedAt = Instant.now()
        return patternSuggestionRepository.save(suggestion).toDto()
    }

    private fun loadPending(suggestionId: Long): PatternSuggestion {
        val suggestion = patternSuggestionRepository.findById(suggestionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "PatternSuggestion $suggestionId not found") }
        if (suggestion.status != SuggestionStatus.PENDING) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "PatternSuggestion $suggestionId is ${suggestion.status}, only PENDING suggestions can be reviewed",
            )
        }
        return suggestion
    }
}
