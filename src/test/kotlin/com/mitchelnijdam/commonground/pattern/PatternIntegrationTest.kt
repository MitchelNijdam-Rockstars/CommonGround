package com.mitchelnijdam.commonground.pattern

import com.mitchelnijdam.commonground.IntegrationTestBase
import com.mitchelnijdam.commonground.topic.Topic
import com.mitchelnijdam.commonground.topic.TopicRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "commonground.auth.dev-bypass.enabled=true",
        "commonground.auth.dev-bypass.email=admin@test.dev",
        "commonground.auth.admin-emails=admin@test.dev",
    ],
)
class PatternIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var topicRepository: TopicRepository

    @Autowired
    lateinit var patternRepository: PatternRepository

    private var topicId: Long = 0

    @BeforeEach
    fun setUp() {
        topicId = topicRepository.save(Topic(question = "How to handle nulls?")).id
    }

    private fun createPattern(title: String): Long {
        val response = mockMvc.perform(
            post("/api/admin/topics/$topicId/patterns")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title": "$title", "code": "fun a() = 1"}"""),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        return Regex(""""id":(\d+)""").find(response)!!.groupValues[1].toLong()
    }

    @Test
    fun `created pattern starts with initial ELO and zero counters`() {
        val id = createPattern("Nullable return")
        val pattern = patternRepository.findById(id).orElseThrow()
        assertThat(pattern.eloRating).isEqualTo(1500.0)
        assertThat(pattern.timesShown).isEqualTo(0)
        assertThat(pattern.timesChosen).isEqualTo(0)
        assertThat(pattern.active).isTrue()
    }

    @Test
    fun `only active patterns are returned for a topic`() {
        createPattern("Nullable return")
        val deactivatedId = createPattern("Throw exception")

        mockMvc.perform(patch("/api/admin/patterns/$deactivatedId/deactivate"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.active").value(false))

        mockMvc.perform(get("/api/topics/$topicId/patterns"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("Nullable return"))
    }

    @Test
    fun `deactivating preserves the pattern row and its counters`() {
        val id = createPattern("Nullable return")
        mockMvc.perform(patch("/api/admin/patterns/$id/deactivate")).andExpect(status().isOk)
        val pattern = patternRepository.findById(id).orElseThrow()
        assertThat(pattern.active).isFalse()
    }

    @Test
    fun `topics need at least two active patterns to be matchup-eligible`() {
        createPattern("Only one")
        assertThat(patternRepository.findTopicIdsWithAtLeastTwoActivePatterns()).isEmpty()

        createPattern("A second")
        assertThat(patternRepository.findTopicIdsWithAtLeastTwoActivePatterns()).containsExactly(topicId)
    }

    @Test
    fun `creating a pattern on a missing topic returns 404`() {
        mockMvc.perform(
            post("/api/admin/topics/99999/patterns")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title": "T", "code": "c"}"""),
        ).andExpect(status().isNotFound)
    }
}
