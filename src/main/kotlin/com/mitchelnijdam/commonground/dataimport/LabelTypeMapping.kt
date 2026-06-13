package com.mitchelnijdam.commonground.dataimport

import com.mitchelnijdam.commonground.label.LabelType
import org.springframework.stereotype.Component

/**
 * Infers a [LabelType] for Labels that the import file references by name but that do not yet exist.
 * The mapping is keyed by lower-cased Label name; unknown names default to [LabelType.STYLE].
 */
@Component
class LabelTypeMapping {

    private val byName: Map<String, LabelType> = buildMap {
        listOf(
            "kotlin", "java", "typescript", "javascript", "python", "go", "rust",
            "c#", "c++", "php", "ruby", "swift", "sql",
        ).forEach { put(it, LabelType.LANGUAGE) }

        listOf(
            "spring", "spring boot", "angular", "react", "vue", "django",
            "express", "quarkus", "rails",
        ).forEach { put(it, LabelType.FRAMEWORK) }

        listOf(
            "microservices", "monolith", "hexagonal", "event-driven", "rest", "cqrs",
        ).forEach { put(it, LabelType.ARCHITECTURE) }

        listOf(
            "functional", "object-oriented", "reactive", "imperative", "declarative",
        ).forEach { put(it, LabelType.PARADIGM) }
    }

    fun inferType(labelName: String): LabelType =
        byName[labelName.trim().lowercase()] ?: LabelType.STYLE
}
