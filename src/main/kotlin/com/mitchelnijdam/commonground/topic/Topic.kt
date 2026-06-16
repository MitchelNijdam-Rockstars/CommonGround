package com.mitchelnijdam.commonground.topic

import com.mitchelnijdam.commonground.label.Label
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "topic")
data class Topic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val question: String,

    @Column(columnDefinition = "text")
    val context: String? = null,

    @Column(length = 50)
    val language: String? = null,

    @ManyToMany
    @JoinTable(
        name = "topic_label",
        joinColumns = [JoinColumn(name = "topic_id")],
        inverseJoinColumns = [JoinColumn(name = "label_id")],
    )
    val labels: MutableSet<Label> = mutableSetOf(),

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)
