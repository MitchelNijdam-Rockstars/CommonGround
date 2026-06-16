package com.mitchelnijdam.commonground.user

import com.mitchelnijdam.commonground.IntegrationTestBase
import com.mitchelnijdam.commonground.label.Label
import com.mitchelnijdam.commonground.label.LabelRepository
import com.mitchelnijdam.commonground.label.LabelType
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.assertj.core.api.Assertions.assertThat
import tools.jackson.databind.ObjectMapper

@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "commonground.auth.dev-bypass.enabled=true",
        "commonground.auth.dev-bypass.email=expert@test.dev",
    ],
)
class UserExpertiseIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var labelRepository: LabelRepository

    @Autowired
    lateinit var topicRepository: TopicRepository

    @Autowired
    lateinit var patternRepository: PatternRepository

    private lateinit var kotlinLabel: Label
    private lateinit var typescriptLabel: Label
    private lateinit var frameworkLabel: Label

    @BeforeEach
    fun seedLabelsAndTopics() {
        kotlinLabel = labelRepository.save(Label(name = "Kotlin", labelType = LabelType.LANGUAGE))
        typescriptLabel = labelRepository.save(Label(name = "TypeScript", labelType = LabelType.LANGUAGE))
        frameworkLabel = labelRepository.save(Label(name = "Spring", labelType = LabelType.FRAMEWORK))

        seedVotableTopic("Kotlin topic?", kotlinLabel)
        seedVotableTopic("TypeScript topic?", typescriptLabel)
    }

    private fun seedVotableTopic(question: String, label: Label): Topic {
        val topic = topicRepository.save(Topic(question = question, labels = mutableSetOf(label)))
        patternRepository.save(Pattern(topic = topic, title = "$question A", code = "a"))
        patternRepository.save(Pattern(topic = topic, title = "$question B", code = "b"))
        return topic
    }

    private fun putExpertise(vararg labelIds: Long) =
        mockMvc.perform(
            put("/api/users/me/expertise")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"labelIds": [${labelIds.joinToString()}]}"""),
        )

    @Test
    fun `expertise can be set and read back`() {
        putExpertise(kotlinLabel.id)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Kotlin"))

        mockMvc.perform(get("/api/users/me/expertise"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Kotlin"))
    }

    @Test
    fun `non-LANGUAGE labels are rejected with 400`() {
        putExpertise(frameworkLabel.id).andExpect(status().isBadRequest)
    }

    @Test
    fun `matchups are filtered by the user's language expertise`() {
        putExpertise(kotlinLabel.id).andExpect(status().isOk)

        repeat(5) {
            val json = mockMvc.perform(get("/api/voting/matchups").param("count", "10"))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString
            val node = objectMapper.readTree(json)
            val questions = (0 until node.size()).map { node.get(it).get("topic").get("question").asText() }
            assertThat(questions).isNotEmpty()
            assertThat(questions).describedAs("only Kotlin topics expected").containsOnly("Kotlin topic?")
        }
    }

    @Test
    fun `without expertise no filter is applied`() {
        val json = mockMvc.perform(get("/api/voting/matchups").param("count", "10"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        val node = objectMapper.readTree(json)
        val questions = (0 until node.size()).map { node.get(it).get("topic").get("question").asText() }
        assertThat(questions).contains("Kotlin topic?", "TypeScript topic?")
    }

    @Test
    fun `open topic count reflects the expertise filter`() {
        mockMvc.perform(get("/api/voting/open-topic-count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(2))

        putExpertise(kotlinLabel.id).andExpect(status().isOk)

        mockMvc.perform(get("/api/voting/open-topic-count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(1))
    }
}
