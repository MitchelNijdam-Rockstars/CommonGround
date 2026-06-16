package com.mitchelnijdam.commonground.suggestion

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * A candidate Pattern submitted together with a [TopicSuggestion]. It has no language of its own —
 * that lives on the suggestion/Topic. On approval it is turned into a real Pattern.
 */
@Entity
@Table(name = "topic_suggestion_pattern")
data class TopicSuggestionPattern(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(length = 200)
    val title: String? = null,

    @Column(nullable = false, columnDefinition = "text")
    val code: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)
