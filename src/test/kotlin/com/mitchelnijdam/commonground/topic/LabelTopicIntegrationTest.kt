package com.mitchelnijdam.commonground.topic

import com.mitchelnijdam.commonground.IntegrationTestBase
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
import tools.jackson.databind.ObjectMapper

@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "commonground.auth.dev-bypass.enabled=true",
        "commonground.auth.dev-bypass.email=admin@test.dev",
        "commonground.auth.admin-emails=admin@test.dev",
    ],
)
class LabelTopicIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private fun createLabel(name: String, type: String = "LANGUAGE"): Long {
        val response = mockMvc.perform(
            post("/api/admin/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "$name", "labelType": "$type"}"""),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        return objectMapper.readTree(response).get("id").asLong()
    }

    private fun createTopic(question: String, labelIds: List<Long> = emptyList()): Long {
        val body = objectMapper.writeValueAsString(mapOf("question" to question, "labelIds" to labelIds))
        val response = mockMvc.perform(
            post("/api/admin/topics").contentType(MediaType.APPLICATION_JSON).content(body),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        return objectMapper.readTree(response).get("id").asLong()
    }

    @Test
    fun `admin can create labels and filter them by type`() {
        createLabel("Kotlin", "LANGUAGE")
        createLabel("Spring Boot", "FRAMEWORK")

        mockMvc.perform(get("/api/labels"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))

        mockMvc.perform(get("/api/labels").param("type", "LANGUAGE"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Kotlin"))
    }

    @Test
    fun `topics can be searched by question text and filtered by label`() {
        val kotlinLabel = createLabel("Kotlin")
        val tsLabel = createLabel("TypeScript")
        createTopic("How should null values be handled?", listOf(kotlinLabel))
        createTopic("How should components be structured?", listOf(tsLabel))

        mockMvc.perform(get("/api/topics").param("search", "null values"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].question").value("How should null values be handled?"))
            .andExpect(jsonPath("$[0].labels[0].name").value("Kotlin"))

        mockMvc.perform(get("/api/topics").param("labelId", tsLabel.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].question").value("How should components be structured?"))
    }

    @Test
    fun `creating a topic with unknown label ids returns 400`() {
        mockMvc.perform(
            post("/api/admin/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"question": "Some question?", "labelIds": [999]}"""),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `duplicate label name returns 409`() {
        createLabel("Kotlin")
        mockMvc.perform(
            post("/api/admin/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "kotlin", "labelType": "LANGUAGE"}"""),
        ).andExpect(status().isConflict)
    }
}
