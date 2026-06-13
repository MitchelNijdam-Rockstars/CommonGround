package com.mitchelnijdam.commonground.voting

import com.mitchelnijdam.commonground.IntegrationTestBase
import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.pattern.PatternRepository
import com.mitchelnijdam.commonground.topic.Topic
import com.mitchelnijdam.commonground.topic.TopicRepository
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
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "commonground.auth.dev-bypass.enabled=true",
        "commonground.auth.dev-bypass.email=voter@test.dev",
    ],
)
class VotingIntegrationTest : IntegrationTestBase() {

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
    lateinit var skipRepository: SkipRepository

    /** Jackson 3 JsonNode is not Iterable; expand an array node into a list explicitly. */
    private fun parseArray(json: String): List<JsonNode> {
        val node = objectMapper.readTree(json)
        return (0 until node.size()).map { node.get(it) }
    }

    private lateinit var topic: Topic
    private lateinit var patternA: Pattern
    private lateinit var patternB: Pattern

    @BeforeEach
    fun seedTopicWithTwoPatterns() {
        topic = topicRepository.save(Topic(question = "How to handle nulls?"))
        patternA = patternRepository.save(Pattern(topic = topic, title = "Nullable", code = "a", language = "kotlin"))
        patternB = patternRepository.save(Pattern(topic = topic, title = "Exception", code = "b", language = "kotlin"))
    }

    @Test
    fun `casting a vote updates ratings and counters of both patterns atomically`() {
        mockMvc.perform(
            post("/api/voting/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"winnerPatternId": ${patternA.id}, "loserPatternId": ${patternB.id}}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.winnerNewRating").value(1516.0))
            .andExpect(jsonPath("$.loserNewRating").value(1484.0))

        val winner = patternRepository.findById(patternA.id).orElseThrow()
        val loser = patternRepository.findById(patternB.id).orElseThrow()

        assertEquals(1516.0, winner.eloRating, 0.0001)
        assertEquals(1, winner.timesShown)
        assertEquals(1, winner.timesChosen)
        assertEquals(1484.0, loser.eloRating, 0.0001)
        assertEquals(1, loser.timesShown)
        assertEquals(0, loser.timesChosen)
        assertEquals(1, voteRepository.count())
    }

    @Test
    fun `skipping records the reason but never touches ratings or counters`() {
        mockMvc.perform(
            post("/api/voting/skip")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"patternAId": ${patternA.id}, "patternBId": ${patternB.id}, "reason": "NOT_FAMILIAR"}"""),
        ).andExpect(status().isNoContent)

        val a = patternRepository.findById(patternA.id).orElseThrow()
        assertEquals(1500.0, a.eloRating, 0.0001)
        assertEquals(0, a.timesShown)
        assertEquals(0, a.timesChosen)

        val skips = skipRepository.findAll()
        assertEquals(1, skips.size)
        assertEquals(SkipReason.NOT_FAMILIAR, skips.first().reason)
    }

    @Test
    fun `a matchup serves two distinct active patterns from one topic`() {
        mockMvc.perform(get("/api/voting/matchup"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.topic.id").value(topic.id))
            .andExpect(jsonPath("$.patternA.id").isNumber)
            .andExpect(jsonPath("$.patternB.id").isNumber)
    }

    @Test
    fun `a batch never repeats a topic or a pattern pair`() {
        // a second eligible topic
        val topic2 = topicRepository.save(Topic(question = "Constructor or builder?"))
        patternRepository.save(Pattern(topic = topic2, title = "Constructor", code = "c", language = "kotlin"))
        patternRepository.save(Pattern(topic = topic2, title = "Builder", code = "d", language = "kotlin"))

        val json = mockMvc.perform(get("/api/voting/matchups").param("count", "10"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val matchups = parseArray(json)
        assertEquals(2, matchups.size)

        val topicIds = matchups.map { it.get("topic").get("id").asLong() }
        assertEquals(topicIds.toSet().size, topicIds.size, "topics must not repeat in a batch")

        val pairKeys = matchups.map {
            val a = it.get("patternA").get("id").asLong()
            val b = it.get("patternB").get("id").asLong()
            MatchupService.pairKey(a, b)
        }
        assertEquals(pairKeys.toSet().size, pairKeys.size, "pairs must not repeat in a batch")
    }

    @Test
    fun `topics with fewer than two active patterns are not served`() {
        val sparseTopic = topicRepository.save(Topic(question = "Only one option?"))
        patternRepository.save(Pattern(topic = sparseTopic, title = "Lonely", code = "x", language = "kotlin"))

        val json = mockMvc.perform(get("/api/voting/matchups").param("count", "10"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val topicIds = parseArray(json).map { it.get("topic").get("id").asLong() }
        assertTrue(sparseTopic.id !in topicIds)
    }

    @Test
    fun `voting on patterns of different topics is rejected`() {
        val otherTopic = topicRepository.save(Topic(question = "Another topic?"))
        val foreign = patternRepository.save(Pattern(topic = otherTopic, title = "F", code = "f", language = "kotlin"))

        mockMvc.perform(
            post("/api/voting/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"winnerPatternId": ${patternA.id}, "loserPatternId": ${foreign.id}}"""),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `already voted pairs are not served again while unseen pairs exist`() {
        // vote on the only pair of topic 1
        mockMvc.perform(
            post("/api/voting/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"winnerPatternId": ${patternA.id}, "loserPatternId": ${patternB.id}}"""),
        ).andExpect(status().isOk)

        // add an unseen topic
        val topic2 = topicRepository.save(Topic(question = "Fresh topic?"))
        val p3 = patternRepository.save(Pattern(topic = topic2, title = "P3", code = "c", language = "kotlin"))
        val p4 = patternRepository.save(Pattern(topic = topic2, title = "P4", code = "d", language = "kotlin"))

        val json = mockMvc.perform(get("/api/voting/matchups").param("count", "1"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val matchups = parseArray(json)
        assertEquals(1, matchups.size)
        val servedIds = setOf(
            matchups[0].get("patternA").get("id").asLong(),
            matchups[0].get("patternB").get("id").asLong(),
        )
        assertEquals(setOf(p3.id, p4.id), servedIds, "the unseen pair should be preferred")
    }
}
