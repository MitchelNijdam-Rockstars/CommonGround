package com.mitchelnijdam.commonground.pattern

import com.mitchelnijdam.commonground.topic.Topic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

const val INITIAL_ELO_RATING = 1500.0

@Entity
@Table(name = "pattern")
data class Pattern(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id")
    val topic: Topic,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false, columnDefinition = "text")
    val code: String,

    @Column(nullable = false)
    val language: String,

    @Column(name = "elo_rating", nullable = false)
    var eloRating: Double = INITIAL_ELO_RATING,

    @Column(name = "times_shown", nullable = false)
    var timesShown: Int = 0,

    @Column(name = "times_chosen", nullable = false)
    var timesChosen: Int = 0,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
) {
    val winRate: Double?
        get() = if (timesShown > 0) timesChosen.toDouble() / timesShown else null
}
