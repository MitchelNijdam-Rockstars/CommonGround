package com.mitchelnijdam.commonground.suggestion

import com.mitchelnijdam.commonground.label.Label
import com.mitchelnijdam.commonground.topic.Topic
import com.mitchelnijdam.commonground.user.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "topic_suggestion")
data class TopicSuggestion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(nullable = false)
    val question: String,

    @Column(columnDefinition = "text")
    val context: String? = null,

    @Column(length = 50)
    val language: String? = null,

    @ManyToMany
    @JoinTable(
        name = "topic_suggestion_label",
        joinColumns = [JoinColumn(name = "topic_suggestion_id")],
        inverseJoinColumns = [JoinColumn(name = "label_id")],
    )
    val labels: MutableSet<Label> = mutableSetOf(),

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_suggestion_id")
    val patterns: MutableList<TopicSuggestionPattern> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: SuggestionStatus = SuggestionStatus.PENDING,

    @Column(name = "rejection_reason")
    var rejectionReason: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_topic_id")
    var createdTopic: Topic? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "reviewed_at")
    var reviewedAt: Instant? = null,
)
