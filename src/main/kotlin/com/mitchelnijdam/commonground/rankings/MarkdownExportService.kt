package com.mitchelnijdam.commonground.rankings

import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.pattern.PatternRepository
import com.mitchelnijdam.commonground.topic.TopicRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MarkdownExportService(
    private val topicRepository: TopicRepository,
    private val patternRepository: PatternRepository,
) {

    /**
     * Builds a deterministic Markdown document with one section per Topic that has at least one
     * voted Pattern (a Pattern is "voted" once it has been shown). Each section is headed by the
     * Topic's question and contains the winning Pattern (highest ELO) with a language-fenced code
     * block. Must run inside the transaction so lazy associations resolve.
     */
    @Transactional(readOnly = true)
    fun export(): String {
        val activePatternsByTopic = patternRepository.findByActiveTrue().groupBy { it.topic.id }

        val sections = topicRepository.findAll()
            .sortedBy { it.id }
            .mapNotNull { topic ->
                val patterns = activePatternsByTopic[topic.id].orEmpty()
                if (patterns.none { it.timesShown > 0 }) return@mapNotNull null
                val winner = patterns.sortedWith(
                    compareByDescending<Pattern> { it.eloRating }.thenBy { it.id },
                ).first()
                section(topic.question, winner)
            }

        return sections.joinToString("\n\n", postfix = "\n")
    }

    private fun section(question: String, winner: Pattern): String = buildString {
        append("## ").append(question).append("\n\n")
        append("### ").append(winner.title).append("\n\n")
        append("```").append(winner.language).append("\n")
        append(winner.code.trimEnd('\n')).append("\n")
        append("```")
    }
}
