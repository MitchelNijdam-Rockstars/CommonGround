package com.mitchelnijdam.commonground.voting

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface VoteRepository : JpaRepository<Vote, Long> {

    fun countByTopicId(topicId: Long): Long

    /** Votes where this pattern was chosen and a non-blank comment was left, most recent first. */
    @Query(
        """
        select v from Vote v
        where v.winnerPattern.id = :patternId and v.comment is not null and trim(v.comment) <> ''
        order by v.createdAt desc
        """,
    )
    fun findCommentsForWinnerPattern(patternId: Long): List<Vote>

    /** Topic ids this user has already voted on (one preference per topic hides it afterwards). */
    @Query("select distinct v.topic.id from Vote v where v.user.id = :userId")
    fun findVotedTopicIdsByUserId(userId: Long): Set<Long>
}
