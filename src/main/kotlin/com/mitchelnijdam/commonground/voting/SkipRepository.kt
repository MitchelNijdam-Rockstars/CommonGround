package com.mitchelnijdam.commonground.voting

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SkipRepository : JpaRepository<Skip, Long> {

    /** Topic ids this user has already skipped (hidden from their feed afterwards). */
    @Query("select distinct s.topic.id from Skip s where s.user.id = :userId")
    fun findSkippedTopicIdsByUserId(userId: Long): Set<Long>
}
