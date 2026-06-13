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

    /** Unordered pattern pairs this user has already voted on, as "smallerId:largerId" keys. */
    @Query(
        """
        select concat(least(v.winnerPattern.id, v.loserPattern.id), ':', greatest(v.winnerPattern.id, v.loserPattern.id))
        from Vote v where v.user.id = :userId
        """,
    )
    fun findVotedPairKeysByUserId(userId: Long): Set<String>
}
