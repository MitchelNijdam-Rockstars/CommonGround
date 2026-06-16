package com.mitchelnijdam.commonground.voting

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
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "skip")
data class Skip(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id")
    val topic: Topic,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val reason: SkipReason,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)

enum class SkipReason {
    NO_PREFERENCE,
    NOT_FAMILIAR,
}
