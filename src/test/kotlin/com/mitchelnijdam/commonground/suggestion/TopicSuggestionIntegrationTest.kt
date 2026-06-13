package com.mitchelnijdam.commonground.suggestion

import com.mitchelnijdam.commonground.IntegrationTestBase
import com.mitchelnijdam.commonground.label.Label
import com.mitchelnijdam.commonground.label.LabelRepository
import com.mitchelnijdam.commonground.label.LabelType
import com.mitchelnijdam.commonground.topic.TopicRepository
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
import kotlin.test.assertEquals

@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "commonground.auth.dev-bypass.enabled=true",
        "commonground.auth.dev-bypass.email=admin@test.dev",
        "commonground.auth.admin-emails=admin@test.dev",
    ],
)
class TopicSuggestionIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var topicRepository: TopicRepository

    @Autowired
    lateinit var labelRepository: LabelRepository

    private fun submit(question: String, labelIds: List<Long> = emptyList()): Long {
        val response = mockMvc.perform(
            post("/api/suggestions/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"question": "$question", "labelIds": [${labelIds.joinToString()}]}"""),
        ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn().response.contentAsString
        return Regex(""""id":(\d+)""").find(response)!!.groupValues[1].toLong()
    }

    @Test
    fun `submitted topic suggestions appear in the user's own list`() {
        submit("Should we use sealed classes for state?")
        mockMvc.perform(get("/api/users/me/suggestions/topics"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].question").value("Should we use sealed classes for state?"))
    }

    @Test
    fun `approving creates a topic with the suggested labels but no patterns`() {
        val label = labelRepository.save(Label(name = "Kotlin", labelType = LabelType.LANGUAGE))
        val id = submit("How to model domain errors?", listOf(label.id))

        mockMvc.perform(post("/api/admin/suggestions/topics/$id/approve"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("APPROVED"))
            .andExpect(jsonPath("$.createdTopicId").isNumber)

        // findAll fetches labels eagerly via the EntityGraph
        val topic = topicRepository.findAll().single { it.question == "How to model domain errors?" }
        assertEquals(setOf("Kotlin"), topic.labels.map { it.name }.toSet())

        // visible in the catalog
        mockMvc.perform(get("/api/topics").param("search", "domain errors"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))

        // but not matchup-eligible: no patterns yet
        mockMvc.perform(get("/api/voting/matchups").param("count", "10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `rejecting stores the optional reason and creates no topic`() {
        val id = submit("Tabs or spaces?")
        mockMvc.perform(
            post("/api/admin/suggestions/topics/$id/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"reason": "Off-topic for this instance"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("REJECTED"))
            .andExpect(jsonPath("$.rejectionReason").value("Off-topic for this instance"))

        assertEquals(0, topicRepository.count())
    }

    @Test
    fun `reviewing an already-reviewed topic suggestion returns 409`() {
        val id = submit("Another question?")
        mockMvc.perform(post("/api/admin/suggestions/topics/$id/reject")).andExpect(status().isOk)
        mockMvc.perform(post("/api/admin/suggestions/topics/$id/approve")).andExpect(status().isConflict)
    }

    @Test
    fun `pending topic suggestions are listed for admins`() {
        submit("Pending question?")
        mockMvc.perform(get("/api/admin/suggestions/topics").param("status", "PENDING"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
    }
}
