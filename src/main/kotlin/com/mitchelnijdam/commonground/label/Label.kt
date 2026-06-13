package com.mitchelnijdam.commonground.label

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "label")
data class Label(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "label_type", nullable = false)
    val labelType: LabelType,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)

enum class LabelType {
    LANGUAGE,
    FRAMEWORK,
    ARCHITECTURE,
    PARADIGM,
    STYLE,
}
