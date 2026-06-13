package com.mitchelnijdam.commonground.suggestion

import org.springframework.data.jpa.repository.JpaRepository

interface TopicSuggestionRepository : JpaRepository<TopicSuggestion, Long> {
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<TopicSuggestion>
    fun findByStatusOrderByCreatedAtAsc(status: SuggestionStatus): List<TopicSuggestion>
}
