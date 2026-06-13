package com.mitchelnijdam.commonground.suggestion

import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.topic.Topic
import com.mitchelnijdam.commonground.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "pattern_suggestion")
data class PatternSuggestion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id")
    val topic: Topic,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    val user: User,

    val title: String? = null,

    @Column(nullable = false, columnDefinition = "text")
    val code: String,

    @Column(nullable = false)
    val language: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: SuggestionStatus = SuggestionStatus.PENDING,

    @Column(name = "rejection_reason")
    var rejectionReason: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_pattern_id")
    var createdPattern: Pattern? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "reviewed_at")
    var reviewedAt: Instant? = null,
)
