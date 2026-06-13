package com.mitchelnijdam.commonground.topic

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface TopicRepository : JpaRepository<Topic, Long> {

    @EntityGraph(attributePaths = ["labels"])
    override fun findAll(): List<Topic>

    fun findByQuestionIgnoreCase(question: String): Topic?
}
