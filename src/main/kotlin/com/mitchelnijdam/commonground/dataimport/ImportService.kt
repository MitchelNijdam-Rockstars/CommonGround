package com.mitchelnijdam.commonground.dataimport

import com.mitchelnijdam.commonground.label.Label
import com.mitchelnijdam.commonground.label.LabelRepository
import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.pattern.PatternRepository
import com.mitchelnijdam.commonground.topic.Topic
import com.mitchelnijdam.commonground.topic.TopicRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ImportService(
    private val topicRepository: TopicRepository,
    private val patternRepository: PatternRepository,
    private val labelRepository: LabelRepository,
    private val labelTypeMapping: LabelTypeMapping,
) {

    @Transactional
    fun import(request: ImportRequest): ImportResultDto {
        var topicsCreated = 0
        var topicsReused = 0
        var patternsCreated = 0
        var patternsSkipped = 0
        var labelsCreated = 0

        for (importTopic in request.topics) {
            val existing = topicRepository.findByQuestionIgnoreCase(importTopic.question.trim())
            val topic: Topic
            if (existing != null) {
                topic = existing
                topicsReused++
            } else {
                val labels = importTopic.labels.map { name ->
                    resolveLabel(name) { labelsCreated++ }
                }.toMutableSet()
                topic = topicRepository.save(
                    Topic(
                        question = importTopic.question.trim(),
                        context = importTopic.context?.ifBlank { null },
                        labels = labels,
                    ),
                )
                topicsCreated++
            }

            for (importPattern in importTopic.patterns) {
                val duplicate = patternRepository.findByTopicIdAndTitleIgnoreCase(topic.id, importPattern.title.trim())
                if (duplicate != null) {
                    patternsSkipped++
                    continue
                }
                patternRepository.save(
                    Pattern(
                        topic = topic,
                        title = importPattern.title.trim(),
                        code = importPattern.code,
                        language = importPattern.language.trim(),
                    ),
                )
                patternsCreated++
            }
        }

        return ImportResultDto(
            topicsCreated = topicsCreated,
            topicsReused = topicsReused,
            patternsCreated = patternsCreated,
            patternsSkipped = patternsSkipped,
            labelsCreated = labelsCreated,
        )
    }

    private fun resolveLabel(name: String, onCreate: () -> Unit): Label {
        val trimmed = name.trim()
        return labelRepository.findByNameIgnoreCase(trimmed) ?: run {
            onCreate()
            labelRepository.save(Label(name = trimmed, labelType = labelTypeMapping.inferType(trimmed)))
        }
    }
}
