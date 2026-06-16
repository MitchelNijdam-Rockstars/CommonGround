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

    private fun patternIds(matchup: JsonNode): Set<Long> {
        val patterns = matchup.get("patterns")
        return (0 until patterns.size()).map { patterns.get(it).get("id").asLong() }.toSet()
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
    fun `casting a vote updates ratings and counters of every shown pattern atomically`() {
        mockMvc.perform(
            post("/api/voting/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"winnerPatternId": ${patternA.id}, "beatenPatternIds": [${patternB.id}]}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.winnerNewRating").value(1516.0))

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
    fun `picking one favorite beats every other shown pattern`() {
        val patternC = patternRepository.save(Pattern(topic = topic, title = "Optional", code = "c", language = "kotlin"))

        mockMvc.perform(
            post("/api/voting/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"winnerPatternId": ${patternA.id}, "beatenPatternIds": [${patternB.id}, ${patternC.id}]}"""),
        ).andExpect(status().isOk)

        val winner = patternRepository.findById(patternA.id).orElseThrow()
        val loserB = patternRepository.findById(patternB.id).orElseThrow()
        val loserC = patternRepository.findById(patternC.id).orElseThrow()

        // winner gained against both, both losers were shown once and chosen zero times
        assertThat(winner.eloRating).isGreaterThan(1500.0)
        assertThat(winner.timesShown).isEqualTo(1)
        assertThat(winner.timesChosen).isEqualTo(1)
        assertThat(loserB.timesShown).isEqualTo(1)
        assertThat(loserB.timesChosen).isEqualTo(0)
        assertThat(loserC.timesShown).isEqualTo(1)
        assertThat(loserC.timesChosen).isEqualTo(0)
        assertThat(voteRepository.count()).isEqualTo(1L)
    }

    @Test
    fun `skipping records the topic and reason but never touches ratings or counters`() {
        mockMvc.perform(
            post("/api/voting/skip")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"topicId": ${topic.id}, "reason": "NOT_FAMILIAR"}"""),
        ).andExpect(status().isNoContent)

        val a = patternRepository.findById(patternA.id).orElseThrow()
        assertThat(a.eloRating).isCloseTo(1500.0, within(0.0001))
        assertThat(a.timesShown).isEqualTo(0)
        assertThat(a.timesChosen).isEqualTo(0)

        val skips = skipRepository.findAll()
        assertThat(skips).hasSize(1)
        assertThat(skips.first().reason).isEqualTo(SkipReason.NOT_FAMILIAR)
        assertThat(skips.first().topic.id).isEqualTo(topic.id)
    }

    @Test
    fun `a matchup serves all active patterns of one topic`() {
        mockMvc.perform(get("/api/voting/matchup"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.topic.id").value(topic.id))
            .andExpect(jsonPath("$.patterns.length()").value(2))
    }

    @Test
    fun `a batch never repeats a topic and shows all its patterns`() {
        // a second eligible topic
        val topic2 = topicRepository.save(Topic(question = "Constructor or builder?"))
        patternRepository.save(Pattern(topic = topic2, title = "Constructor", code = "c", language = "kotlin"))
        patternRepository.save(Pattern(topic = topic2, title = "Builder", code = "d", language = "kotlin"))

        val json = mockMvc.perform(get("/api/voting/matchups").param("count", "10"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val matchups = parseArray(json)
        assertThat(matchups).hasSize(2)

        val topicIds = matchups.map { it.get("topic").get("id").asLong() }
        assertThat(topicIds).describedAs("topics must not repeat in a batch").doesNotHaveDuplicates()
        assertThat(matchups).allSatisfy { assertThat(patternIds(it)).hasSize(2) }
    }

    @Test
    fun `topics with fewer than two active patterns are not served`() {
        val sparseTopic = topicRepository.save(Topic(question = "Only one option?"))
        patternRepository.save(Pattern(topic = sparseTopic, title = "Lonely", code = "x", language = "kotlin"))

        val json = mockMvc.perform(get("/api/voting/matchups").param("count", "10"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val topicIds = parseArray(json).map { it.get("topic").get("id").asLong() }
        assertThat(topicIds).doesNotContain(sparseTopic.id)
    }

    @Test
    fun `voting on patterns of different topics is rejected`() {
        val otherTopic = topicRepository.save(Topic(question = "Another topic?"))
        val foreign = patternRepository.save(Pattern(topic = otherTopic, title = "F", code = "f", language = "kotlin"))

        mockMvc.perform(
            post("/api/voting/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"winnerPatternId": ${patternA.id}, "beatenPatternIds": [${foreign.id}]}"""),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `a topic already voted on is not served again`() {
        // vote on topic 1
        mockMvc.perform(
            post("/api/voting/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"winnerPatternId": ${patternA.id}, "beatenPatternIds": [${patternB.id}]}"""),
        ).andExpect(status().isOk)

        // add an unseen topic
        val topic2 = topicRepository.save(Topic(question = "Fresh topic?"))
        val p3 = patternRepository.save(Pattern(topic = topic2, title = "P3", code = "c", language = "kotlin"))
        val p4 = patternRepository.save(Pattern(topic = topic2, title = "P4", code = "d", language = "kotlin"))

        val json = mockMvc.perform(get("/api/voting/matchups").param("count", "10"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val matchups = parseArray(json)
        assertThat(matchups).describedAs("the voted topic is hidden, only the fresh one remains").hasSize(1)
        assertThat(matchups[0].get("topic").get("id").asLong()).isEqualTo(topic2.id)
        assertThat(patternIds(matchups[0])).isEqualTo(setOf(p3.id, p4.id))
    }
}
