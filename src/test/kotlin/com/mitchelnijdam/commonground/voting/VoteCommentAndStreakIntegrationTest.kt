package com.mitchelnijdam.commonground.voting

import com.mitchelnijdam.commonground.IntegrationTestBase
import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.pattern.PatternRepository
import com.mitchelnijdam.commonground.topic.Topic
import com.mitchelnijdam.commonground.topic.TopicRepository
import com.mitchelnijdam.commonground.user.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate

@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "commonground.auth.dev-bypass.enabled=true",
        "commonground.auth.dev-bypass.email=streak@test.dev",
    ],
)
class VoteCommentAndStreakIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var topicRepository: TopicRepository

    @Autowired
    lateinit var patternRepository: PatternRepository

    @Autowired
    lateinit var voteRepository: VoteRepository

    @Autowired
    lateinit var userRepository: UserRepository

    private lateinit var patternA: Pattern
    private lateinit var patternB: Pattern

    @BeforeEach
    fun seedTopic() {
        val topic = topicRepository.save(Topic(question = "Comments and streaks?"))
        patternA = patternRepository.save(Pattern(topic = topic, title = "A", code = "a"))
        patternB = patternRepository.save(Pattern(topic = topic, title = "B", code = "b"))
    }

    private fun vote(comment: String? = null) = mockMvc.perform(
        post("/api/voting/vote")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                buildString {
                    append("""{"winnerPatternId": ${patternA.id}, "beatenPatternIds": [${patternB.id}]""")
                    if (comment != null) append(""", "comment": "$comment"""")
                    append("}")
                },
            ),
    )

    @Test
    fun `a vote can carry an optional comment`() {
        vote("Explicit is better than implicit").andExpect(status().isOk)
        assertThat(voteRepository.findAll().single().comment).isEqualTo("Explicit is better than implicit")
    }

    @Test
    fun `a vote without comment stores null`() {
        vote().andExpect(status().isOk)
        assertThat(voteRepository.findAll().single().comment).isNull()
    }

    @Test
    fun `comments longer than 500 characters are rejected`() {
        vote("x".repeat(501)).andExpect(status().isBadRequest)
        assertThat(voteRepository.count()).isEqualTo(0L)
    }

    @Test
    fun `first vote of the day starts or continues the streak`() {
        vote().andExpect(status().isOk).andExpect(jsonPath("$.currentStreak").value(1))
        // voting again the same day keeps the streak at 1
        vote().andExpect(status().isOk).andExpect(jsonPath("$.currentStreak").value(1))
    }

    @Test
    fun `voting on consecutive days increments the streak`() {
        vote().andExpect(status().isOk)

        val user = userRepository.findByEmailIgnoreCase("streak@test.dev")!!
        user.lastVotedDate = LocalDate.now().minusDays(1)
        user.currentStreak = 4
        userRepository.save(user)

        vote().andExpect(status().isOk).andExpect(jsonPath("$.currentStreak").value(5))
    }

    @Test
    fun `a missed day resets the streak to 1`() {
        vote().andExpect(status().isOk)

        val user = userRepository.findByEmailIgnoreCase("streak@test.dev")!!
        user.lastVotedDate = LocalDate.now().minusDays(3)
        user.currentStreak = 9
        userRepository.save(user)

        vote().andExpect(status().isOk).andExpect(jsonPath("$.currentStreak").value(1))
    }

    @Test
    fun `me endpoint exposes the current streak and zeroes out stale streaks`() {
        vote().andExpect(status().isOk)
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.currentStreak").value(1))

        val user = userRepository.findByEmailIgnoreCase("streak@test.dev")!!
        user.lastVotedDate = LocalDate.now().minusDays(5)
        user.currentStreak = 7
        userRepository.save(user)

        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.currentStreak").value(0))
    }
}
