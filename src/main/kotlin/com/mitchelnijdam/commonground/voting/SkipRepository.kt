package com.mitchelnijdam.commonground.voting

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SkipRepository : JpaRepository<Skip, Long> {

    /** Unordered pattern pairs this user has already skipped, as "smallerId:largerId" keys. */
    @Query(
        """
        select concat(least(s.patternA.id, s.patternB.id), ':', greatest(s.patternA.id, s.patternB.id))
        from Skip s where s.user.id = :userId
        """,
    )
    fun findSkippedPairKeysByUserId(userId: Long): Set<String>
}
