package com.mitchelnijdam.commonground.pattern

import com.mitchelnijdam.commonground.topic.TopicRepository
import com.mitchelnijdam.commonground.voting.VoteRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class PatternService(
    private val patternRepository: PatternRepository,
    private val topicRepository: TopicRepository,
    private val voteRepository: VoteRepository,
) {

    @Transactional(readOnly = true)
    fun activePatternsForTopic(topicId: Long): List<PatternDto> {
        requireTopicExists(topicId)
        return patternRepository.findByTopicIdAndActiveTrue(topicId)
            .sortedBy { it.title.lowercase() }
            .map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun detail(patternId: Long): PatternDetailDto {
        val pattern = patternRepository.findById(patternId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Pattern $patternId not found") }
        val comments = voteRepository.findCommentsForWinnerPattern(patternId)
            .map { PatternCommentDto(comment = it.comment!!, createdAt = it.createdAt) }
        return PatternDetailDto(
            id = pattern.id,
            topicId = pattern.topic.id,
            title = pattern.title,
            code = pattern.code,
            language = pattern.topic.language,
            eloRating = pattern.eloRating,
            winRate = pattern.winRate,
            comments = comments,
        )
    }

    @Transactional
    fun create(topicId: Long, title: String, code: String): PatternDto {
        val topic = topicRepository.findById(topicId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Topic $topicId not found") }
        return patternRepository.save(Pattern(topic = topic, title = title, code = code)).toDto()
    }

    @Transactional
    fun deactivate(patternId: Long): PatternDto {
        val pattern = patternRepository.findById(patternId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Pattern $patternId not found") }
        pattern.active = false
        return patternRepository.save(pattern).toDto()
    }

    private fun requireTopicExists(topicId: Long) {
        if (!topicRepository.existsById(topicId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Topic $topicId not found")
        }
    }
}
