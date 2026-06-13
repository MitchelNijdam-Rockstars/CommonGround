package com.mitchelnijdam.commonground.pattern

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PatternRepository : JpaRepository<Pattern, Long> {

    fun findByTopicIdAndActiveTrue(topicId: Long): List<Pattern>

    fun findByActiveTrue(): List<Pattern>

    fun findByTopicIdAndTitleIgnoreCase(topicId: Long, title: String): Pattern?

    /** Topic ids that are eligible for matchups: at least two active Patterns. */
    @Query("select p.topic.id from Pattern p where p.active = true group by p.topic.id having count(p) >= 2")
    fun findTopicIdsWithAtLeastTwoActivePatterns(): List<Long>
}
