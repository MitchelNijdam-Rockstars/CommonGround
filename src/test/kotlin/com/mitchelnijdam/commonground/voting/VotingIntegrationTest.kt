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
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

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
        patternA = patternRepository.save(Pattern(topic = topic, title = "Nullable", code = "a"))
        patternB = patternRepository.save(Pattern(topic = topic, title = "Exception", code = "b"))
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

        assertThat(winner.eloRating).isCloseTo(1516.0, within(0.0001))
        assertThat(winner.timesShown).isEqualTo(1)
        assertThat(winner.timesChosen).isEqualTo(1)
        assertThat(loser.eloRating).isCloseTo(1484.0, within(0.0001))
        assertThat(loser.timesShown).isEqualTo(1)
        assertThat(loser.timesChosen).isEqualTo(0)
        assertThat(voteRepository.count()).isEqualTo(1L)
    }

    @Test
    fun `skipping records the reason but never touches ratings or counters`() {
        mockMvc.perform(
            post("/api/voting/skip")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"patternAId": ${patternA.id}, "patternBId": ${patternB.id}, "reason": "NOT_FAMILIAR"}"""),
        ).andExpect(status().isNoContent)

        val a = patternRepository.findById(patternA.id).orElseThrow()
        assertThat(a.eloRating).isCloseTo(1500.0, within(0.0001))
        assertThat(a.timesShown).isEqualTo(0)
        assertThat(a.timesChosen).isEqualTo(0)

        val skips = skipRepository.findAll()
        assertThat(skips).hasSize(1)
        assertThat(skips.first().reason).isEqualTo(SkipReason.NOT_FAMILIAR)
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
        patternRepository.save(Pattern(topic = topic2, title = "Constructor", code = "c"))
        patternRepository.save(Pattern(topic = topic2, title = "Builder", code = "d"))

        val json = mockMvc.perform(get("/api/voting/matchups").param("count", "10"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val matchups = parseArray(json)
        assertThat(matchups).hasSize(2)

        val topicIds = matchups.map { it.get("topic").get("id").asLong() }
        assertThat(topicIds).describedAs("topics must not repeat in a batch").doesNotHaveDuplicates()

        val pairKeys = matchups.map {
            val a = it.get("patternA").get("id").asLong()
            val b = it.get("patternB").get("id").asLong()
            MatchupService.pairKey(a, b)
        }
        assertThat(pairKeys).describedAs("pairs must not repeat in a batch").doesNotHaveDuplicates()
    }

    @Test
    fun `topics with fewer than two active patterns are not served`() {
        val sparseTopic = topicRepository.save(Topic(question = "Only one option?"))
        patternRepository.save(Pattern(topic = sparseTopic, title = "Lonely", code = "x"))

        val json = mockMvc.perform(get("/api/voting/matchups").param("count", "10"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val topicIds = parseArray(json).map { it.get("topic").get("id").asLong() }
        assertThat(topicIds).doesNotContain(sparseTopic.id)
    }

    @Test
    fun `voting on patterns of different topics is rejected`() {
        val otherTopic = topicRepository.save(Topic(question = "Another topic?"))
        val foreign = patternRepository.save(Pattern(topic = otherTopic, title = "F", code = "f"))

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
        val p3 = patternRepository.save(Pattern(topic = topic2, title = "P3", code = "c"))
        val p4 = patternRepository.save(Pattern(topic = topic2, title = "P4", code = "d"))

        val json = mockMvc.perform(get("/api/voting/matchups").param("count", "1"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val matchups = parseArray(json)
        assertThat(matchups).hasSize(1)
        val servedIds = setOf(
            matchups[0].get("patternA").get("id").asLong(),
            matchups[0].get("patternB").get("id").asLong(),
        )
        assertThat(servedIds).describedAs("the unseen pair should be preferred").isEqualTo(setOf(p3.id, p4.id))
    }
}
