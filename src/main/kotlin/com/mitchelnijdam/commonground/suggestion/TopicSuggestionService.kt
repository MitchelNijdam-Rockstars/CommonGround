package com.mitchelnijdam.commonground.suggestion

import com.mitchelnijdam.commonground.label.LabelRepository
import com.mitchelnijdam.commonground.topic.Topic
import com.mitchelnijdam.commonground.topic.TopicRepository
import com.mitchelnijdam.commonground.user.User
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class TopicSuggestionService(
    private val topicSuggestionRepository: TopicSuggestionRepository,
    private val topicRepository: TopicRepository,
    private val labelRepository: LabelRepository,
) {

    @Transactional
    fun submit(user: User, question: String, context: String?, labelIds: List<Long>): TopicSuggestionDto {
        val labels = labelRepository.findAllById(labelIds.distinct())
        val missing = labelIds.toSet() - labels.map { it.id }.toSet()
        if (missing.isNotEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown label ids: $missing")
        }
        return topicSuggestionRepository.save(
            TopicSuggestion(
                user = user,
                question = question,
                context = context?.ifBlank { null },
                labels = labels.toMutableSet(),
            ),
        ).toDto()
    }

    @Transactional(readOnly = true)
    fun forUser(user: User): List<TopicSuggestionDto> =
        topicSuggestionRepository.findByUserIdOrderByCreatedAtDesc(user.id).map { it.toDto() }

    @Transactional(readOnly = true)
    fun byStatus(status: SuggestionStatus): List<TopicSuggestionDto> =
        topicSuggestionRepository.findByStatusOrderByCreatedAtAsc(status).map { it.toDto() }

    /**
     * Approval creates a new Topic carrying the suggested Labels. The Topic starts without
     * Patterns, so it shows in the Catalog but stays out of matchups until Patterns are added.
     */
    @Transactional
    fun approve(suggestionId: Long): TopicSuggestionDto {
        val suggestion = loadPending(suggestionId)
        val topic = topicRepository.save(
            Topic(
                question = suggestion.question,
                context = suggestion.context,
                labels = suggestion.labels.toMutableSet(),
            ),
        )
        suggestion.status = SuggestionStatus.APPROVED
        suggestion.createdTopic = topic
        suggestion.reviewedAt = Instant.now()
        return topicSuggestionRepository.save(suggestion).toDto()
    }

    @Transactional
    fun reject(suggestionId: Long, reason: String?): TopicSuggestionDto {
        val suggestion = loadPending(suggestionId)
        suggestion.status = SuggestionStatus.REJECTED
        suggestion.rejectionReason = reason?.ifBlank { null }
        suggestion.reviewedAt = Instant.now()
        return topicSuggestionRepository.save(suggestion).toDto()
    }

    private fun loadPending(suggestionId: Long): TopicSuggestion {
        val suggestion = topicSuggestionRepository.findById(suggestionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "TopicSuggestion $suggestionId not found") }
        if (suggestion.status != SuggestionStatus.PENDING) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "TopicSuggestion $suggestionId is ${suggestion.status}, only PENDING suggestions can be reviewed",
            )
        }
        return suggestion
    }
}
