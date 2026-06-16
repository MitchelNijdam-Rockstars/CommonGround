package com.mitchelnijdam.commonground.voting

import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.topic.Topic
import com.mitchelnijdam.commonground.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "vote")
data class Vote(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id")
    val topic: Topic,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "winner_pattern_id")
    val winnerPattern: Pattern,

    // The other patterns shown that the winner beat. One entry for a pairwise vote, N-1 when all
    // patterns of a topic are shown at once.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "vote_beaten_pattern",
        joinColumns = [JoinColumn(name = "vote_id")],
        inverseJoinColumns = [JoinColumn(name = "pattern_id")],
    )
    val beatenPatterns: Set<Pattern>,

    @Column(length = 500)
    val comment: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)
