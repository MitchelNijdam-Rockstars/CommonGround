package com.mitchelnijdam.commonground.user

import com.mitchelnijdam.commonground.label.Label
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
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "app_user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "last_voted_date")
    var lastVotedDate: LocalDate? = null,

    @Column(name = "current_streak", nullable = false)
    var currentStreak: Int = 0,

    // Eagerly fetched: the set is tiny and needed on most requests (matchup filtering)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_expertise",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "label_id")],
    )
    val expertise: MutableSet<Label> = mutableSetOf(),
) {
    /** The streak as it should be displayed: stale streaks (no vote yesterday or today) count as 0. */
    fun effectiveStreak(today: LocalDate = LocalDate.now()): Int =
        if (lastVotedDate != null && !lastVotedDate!!.isBefore(today.minusDays(1))) currentStreak else 0
}

enum class UserRole {
    USER,
    ADMIN,
}
