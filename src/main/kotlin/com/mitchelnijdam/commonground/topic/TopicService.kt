package com.mitchelnijdam.commonground.topic

import com.mitchelnijdam.commonground.label.LabelRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class TopicService(
    private val topicRepository: TopicRepository,
    private val labelRepository: LabelRepository,
) {

    @Transactional(readOnly = true)
    fun search(search: String?, labelId: Long?): List<Topic> =
        topicRepository.findAll()
            .filter { search.isNullOrBlank() || it.question.contains(search.trim(), ignoreCase = true) }
            .filter { labelId == null || it.labels.any { label -> label.id == labelId } }
            .sortedBy { it.question.lowercase() }

    @Transactional
    fun create(question: String, context: String?, language: String?, labelIds: List<Long>): Topic {
        val labels = labelRepository.findAllById(labelIds)
        val missing = labelIds.toSet() - labels.map { it.id }.toSet()
        if (missing.isNotEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown label ids: $missing")
        }
        return topicRepository.save(
            Topic(
                question = question,
                context = context?.ifBlank { null },
                language = language?.ifBlank { null },
                labels = labels.toMutableSet(),
            ),
        )
    }
}
