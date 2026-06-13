package com.mitchelnijdam.commonground.suggestion

import org.springframework.data.jpa.repository.JpaRepository

interface PatternSuggestionRepository : JpaRepository<PatternSuggestion, Long> {
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<PatternSuggestion>
    fun findByStatusOrderByCreatedAtAsc(status: SuggestionStatus): List<PatternSuggestion>
}
