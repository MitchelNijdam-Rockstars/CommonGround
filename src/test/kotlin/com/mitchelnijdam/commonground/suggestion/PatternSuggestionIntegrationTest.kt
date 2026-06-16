package com.mitchelnijdam.commonground.suggestion

import com.mitchelnijdam.commonground.IntegrationTestBase
import com.mitchelnijdam.commonground.pattern.PatternRepository
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
class PatternSuggestionIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var topicRepository: TopicRepository

    @Autowired
    lateinit var patternRepository: PatternRepository

    @Autowired
    lateinit var patternSuggestionRepository: PatternSuggestionRepository

    private var topicId: Long = 0

    @BeforeEach
    fun seedTopic() {
        topicId = topicRepository.save(Topic(question = "How to inject dependencies?")).id
    }

    private fun submitSuggestion(title: String? = "Constructor injection"): Long {
        val titleJson = if (title != null) """"title": "$title",""" else ""
        val response = mockMvc.perform(
            post("/api/topics/$topicId/suggestions/patterns")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{$titleJson "code": "class A(private val b: B)", "language": "kotlin"}"""),
        ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn().response.contentAsString
        return Regex(""""id":(\d+)""").find(response)!!.groupValues[1].toLong()
    }

    @Test
    fun `submitted suggestions appear in the user's own list with status`() {
        submitSuggestion()
        mockMvc.perform(get("/api/users/me/suggestions/patterns"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].status").value("PENDING"))
            .andExpect(jsonPath("$[0].topicQuestion").value("How to inject dependencies?"))
    }

    @Test
    fun `approving a pending suggestion creates an active pattern with initial values`() {
        val id = submitSuggestion()

        mockMvc.perform(post("/api/admin/suggestions/patterns/$id/approve"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("APPROVED"))

        val patterns = patternRepository.findByTopicIdAndActiveTrue(topicId)
        assertThat(patterns).hasSize(1)
        with(patterns.single()) {
            assertThat(title).isEqualTo("Constructor injection")
            assertThat(eloRating).isEqualTo(1500.0)
            assertThat(timesShown).isEqualTo(0)
            assertThat(timesChosen).isEqualTo(0)
            assertThat(active).isTrue()
        }
        // the suggestion remains as a historical record
        assertThat(patternSuggestionRepository.findById(id).orElseThrow().status).isEqualTo(SuggestionStatus.APPROVED)
    }

    @Test
    fun `rejecting a pending suggestion stores the optional reason`() {
        val id = submitSuggestion()

        mockMvc.perform(
            post("/api/admin/suggestions/patterns/$id/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"reason": "Duplicate of an existing pattern"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("REJECTED"))
            .andExpect(jsonPath("$.rejectionReason").value("Duplicate of an existing pattern"))

        assertThat(patternRepository.findByTopicIdAndActiveTrue(topicId)).isEmpty()
    }

    @Test
    fun `reviewing an already-reviewed suggestion returns 409`() {
        val id = submitSuggestion()
        mockMvc.perform(post("/api/admin/suggestions/patterns/$id/approve")).andExpect(status().isOk)

        mockMvc.perform(post("/api/admin/suggestions/patterns/$id/reject")).andExpect(status().isConflict)
        mockMvc.perform(post("/api/admin/suggestions/patterns/$id/approve")).andExpect(status().isConflict)
    }

    @Test
    fun `pending list only contains pending suggestions`() {
        val approved = submitSuggestion("First")
        submitSuggestion("Second")
        mockMvc.perform(post("/api/admin/suggestions/patterns/$approved/approve")).andExpect(status().isOk)

        mockMvc.perform(get("/api/admin/suggestions/patterns").param("status", "PENDING"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("Second"))
    }

    @Test
    fun `a suggestion without title gets a fallback title on approval`() {
        val id = submitSuggestion(title = null)
        mockMvc.perform(post("/api/admin/suggestions/patterns/$id/approve")).andExpect(status().isOk)

        val pattern = patternRepository.findByTopicIdAndActiveTrue(topicId).single()
        assertThat(pattern.title).isEqualTo("Community suggestion #$id")
    }
}
