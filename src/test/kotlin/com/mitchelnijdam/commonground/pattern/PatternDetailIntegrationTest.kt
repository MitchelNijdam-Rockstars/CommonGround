package com.mitchelnijdam.commonground.pattern

import com.mitchelnijdam.commonground.IntegrationTestBase
import com.mitchelnijdam.commonground.topic.Topic
import com.mitchelnijdam.commonground.topic.TopicRepository
import com.mitchelnijdam.commonground.user.User
import com.mitchelnijdam.commonground.user.UserRepository
import com.mitchelnijdam.commonground.user.UserRole
import com.mitchelnijdam.commonground.voting.Vote
import com.mitchelnijdam.commonground.voting.VoteRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.assertj.core.api.Assertions.assertThat
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.time.Instant

@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "commonground.auth.dev-bypass.enabled=true",
        "commonground.auth.dev-bypass.email=dev@test.dev",
    ],
)
class PatternDetailIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var topicRepository: TopicRepository

    @Autowired
    lateinit var patternRepository: PatternRepository

    @Autowired
    lateinit var voteRepository: VoteRepository

    @Autowired
    lateinit var userRepository: UserRepository

    private fun parseArray(node: JsonNode): List<JsonNode> = (0 until node.size()).map { node.get(it) }

    private lateinit var topic: Topic
    private lateinit var winner: Pattern
    private lateinit var loser: Pattern
    private lateinit var voter: User

    @BeforeEach
    fun seed() {
        topic = topicRepository.save(Topic(question = "How to handle nulls?", language = "kotlin"))
        winner = patternRepository.save(Pattern(topic = topic, title = "Nullable", code = "fun a() = 1"))
        loser = patternRepository.save(Pattern(topic = topic, title = "Exception", code = "fun b() = 2"))
        voter = userRepository.save(User(email = "voter@test.dev", role = UserRole.USER))
    }

    private fun vote(comment: String?, createdAt: Instant, winnerPattern: Pattern = winner) {
        voteRepository.save(
            Vote(
                user = voter,
                topic = topic,
                winnerPattern = winnerPattern,
                loserPattern = if (winnerPattern == winner) loser else winner,
                comment = comment,
                createdAt = createdAt,
            ),
        )
    }

    @Test
    fun `returns pattern detail with comments ordered most recent first`() {
        vote("oldest", Instant.parse("2026-01-01T00:00:00Z"))
        vote("newest", Instant.parse("2026-03-01T00:00:00Z"))
        vote("middle", Instant.parse("2026-02-01T00:00:00Z"))

        val json = mockMvc.perform(get("/api/patterns/${winner.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(winner.id))
            .andExpect(jsonPath("$.title").value("Nullable"))
            .andExpect(jsonPath("$.code").value("fun a() = 1"))
            .andExpect(jsonPath("$.language").value("kotlin"))
            .andExpect(jsonPath("$.eloRating").isNumber)
            .andReturn().response.contentAsString

        val comments = parseArray(objectMapper.readTree(json).get("comments"))
        assertThat(comments.map { it.get("comment").asString() }).containsExactly("newest", "middle", "oldest")
    }

    @Test
    fun `excludes votes with null or blank comment`() {
        vote("real comment", Instant.parse("2026-01-01T00:00:00Z"))
        vote(null, Instant.parse("2026-01-02T00:00:00Z"))
        vote("   ", Instant.parse("2026-01-03T00:00:00Z"))

        val json = mockMvc.perform(get("/api/patterns/${winner.id}"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val comments = parseArray(objectMapper.readTree(json).get("comments"))
        assertThat(comments.map { it.get("comment").asString() }).containsExactly("real comment")
    }

    @Test
    fun `comments only include votes where this pattern was the winner`() {
        // comment tied to the OTHER pattern (loser won its own matchup); must not appear here
        vote("for the other one", Instant.parse("2026-01-01T00:00:00Z"), winnerPattern = loser)

        val json = mockMvc.perform(get("/api/patterns/${winner.id}"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val comments = parseArray(objectMapper.readTree(json).get("comments"))
        assertThat(comments).isEmpty()
    }

    @Test
    fun `returns empty comment list when pattern has no comments`() {
        mockMvc.perform(get("/api/patterns/${winner.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.comments").isArray)
            .andExpect(jsonPath("$.comments").isEmpty)
    }

    @Test
    fun `returns 404 for an unknown pattern`() {
        mockMvc.perform(get("/api/patterns/99999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `never leaks voter identity in the response`() {
        vote("anonymous please", Instant.parse("2026-01-01T00:00:00Z"))

        val json = mockMvc.perform(get("/api/patterns/${winner.id}"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        assertThat(json).doesNotContain("voter@test.dev", "\"user\"", "\"email\"")
    }
}
