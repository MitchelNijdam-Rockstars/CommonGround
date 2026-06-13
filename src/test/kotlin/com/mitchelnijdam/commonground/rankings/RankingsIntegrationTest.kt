package com.mitchelnijdam.commonground.rankings

import com.mitchelnijdam.commonground.IntegrationTestBase
import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.pattern.PatternRepository
import com.mitchelnijdam.commonground.topic.Topic
import com.mitchelnijdam.commonground.topic.TopicRepository
import com.mitchelnijdam.commonground.user.User
import com.mitchelnijdam.commonground.user.UserRepository
import com.mitchelnijdam.commonground.user.UserRole
import com.mitchelnijdam.commonground.voting.Vote
import com.mitchelnijdam.commonground.voting.VoteRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import kotlin.test.assertEquals

@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "commonground.auth.dev-bypass.enabled=true",
        "commonground.auth.dev-bypass.email=dev@test.dev",
    ],
)
class RankingsIntegrationTest : IntegrationTestBase() {

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

    private fun parseArray(json: String): List<JsonNode> {
        val node = objectMapper.readTree(json)
        return (0 until node.size()).map { node.get(it) }
    }

    @Test
    fun `ELO ranking sorts patterns by elo rating descending`() {
        val topic = topicRepository.save(Topic(question = "How to handle nulls?"))
        patternRepository.save(Pattern(topic = topic, title = "Low", code = "a", language = "kotlin", eloRating = 1400.0))
        patternRepository.save(Pattern(topic = topic, title = "High", code = "b", language = "kotlin", eloRating = 1600.0))
        patternRepository.save(Pattern(topic = topic, title = "Mid", code = "c", language = "kotlin", eloRating = 1500.0))

        val json = mockMvc.perform(get("/api/rankings").param("algorithm", "ELO"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val sections = parseArray(json)
        assertEquals(1, sections.size)
        val patterns = (0 until sections[0].get("patterns").size()).map { sections[0].get("patterns").get(it) }
        assertEquals(listOf("High", "Mid", "Low"), patterns.map { it.get("title").asString() })
    }

    @Test
    fun `WIN_RATE ranking sorts by win rate descending and ranks no-vote patterns last`() {
        val topic = topicRepository.save(Topic(question = "Win rate topic?"))
        patternRepository.save(
            Pattern(topic = topic, title = "Best", code = "a", language = "kotlin", timesShown = 10, timesChosen = 9),
        )
        patternRepository.save(
            Pattern(topic = topic, title = "Worst", code = "b", language = "kotlin", timesShown = 10, timesChosen = 1),
        )
        patternRepository.save(
            Pattern(topic = topic, title = "Unseen", code = "c", language = "kotlin", timesShown = 0, timesChosen = 0),
        )

        val json = mockMvc.perform(get("/api/rankings").param("algorithm", "WIN_RATE"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val sections = parseArray(json)
        val patterns = (0 until sections[0].get("patterns").size()).map { sections[0].get("patterns").get(it) }
        assertEquals(listOf("Best", "Worst", "Unseen"), patterns.map { it.get("title").asString() })
    }

    @Test
    fun `unknown algorithm returns 400`() {
        mockMvc.perform(get("/api/rankings").param("algorithm", "BOGUS"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `default algorithm is ELO when no param given`() {
        val topic = topicRepository.save(Topic(question = "Default topic?"))
        patternRepository.save(Pattern(topic = topic, title = "Low", code = "a", language = "kotlin", eloRating = 1400.0))
        patternRepository.save(Pattern(topic = topic, title = "High", code = "b", language = "kotlin", eloRating = 1600.0))

        val json = mockMvc.perform(get("/api/rankings"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val patterns = parseArray(json)[0].get("patterns")
        assertEquals("High", patterns.get(0).get("title").asString())
    }

    @Test
    fun `each topic section reports total votes and only topics with active patterns appear`() {
        val voter = userRepository.save(User(email = "ranker@test.dev", role = UserRole.USER))
        val topic = topicRepository.save(Topic(question = "Vote count topic?"))
        val a = patternRepository.save(Pattern(topic = topic, title = "A", code = "a", language = "kotlin"))
        val b = patternRepository.save(Pattern(topic = topic, title = "B", code = "b", language = "kotlin"))
        voteRepository.save(Vote(topic = topic, winnerPattern = a, loserPattern = b, user = voter))
        voteRepository.save(Vote(topic = topic, winnerPattern = b, loserPattern = a, user = voter))

        // a topic with only an inactive pattern must be excluded
        val emptyTopic = topicRepository.save(Topic(question = "No active patterns?"))
        patternRepository.save(Pattern(topic = emptyTopic, title = "Gone", code = "x", language = "kotlin", active = false))

        val json = mockMvc.perform(get("/api/rankings"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val sections = parseArray(json)
        assertEquals(1, sections.size)
        assertEquals(topic.id, sections[0].get("topic").get("id").asLong())
        assertEquals(2, sections[0].get("totalVotes").asInt())
    }
}
