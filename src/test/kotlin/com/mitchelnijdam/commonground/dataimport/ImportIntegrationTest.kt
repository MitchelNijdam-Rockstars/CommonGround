package com.mitchelnijdam.commonground.dataimport

import com.mitchelnijdam.commonground.IntegrationTestBase
import com.mitchelnijdam.commonground.label.LabelRepository
import com.mitchelnijdam.commonground.label.LabelType
import com.mitchelnijdam.commonground.pattern.PatternRepository
import com.mitchelnijdam.commonground.topic.Topic
import com.mitchelnijdam.commonground.topic.TopicRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
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
class ImportIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var topicRepository: TopicRepository

    @Autowired
    lateinit var patternRepository: PatternRepository

    @Autowired
    lateinit var labelRepository: LabelRepository

    private val payload = """
        {
          "topics": [
            {
              "question": "How should null values be handled in Kotlin?",
              "context": "Baseline context",
              "language": "kotlin",
              "labels": ["Kotlin", "SomeNewStyle"],
              "patterns": [
                { "title": "Return nullable type", "code": "fun f(): User?" },
                { "title": "Throw exception", "code": "fun f(): User" }
              ]
            }
          ]
        }
    """.trimIndent()

    private fun import(body: String): ResultActions =
        mockMvc.perform(post("/api/admin/import").contentType(MediaType.APPLICATION_JSON).content(body))

    @Test
    fun `import creates topics and patterns with initial values and reports counts`() {
        import(payload)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.topicsCreated").value(1))
            .andExpect(jsonPath("$.topicsReused").value(0))
            .andExpect(jsonPath("$.patternsCreated").value(2))
            .andExpect(jsonPath("$.patternsSkipped").value(0))
            .andExpect(jsonPath("$.labelsCreated").value(2))

        val topic = topicRepository.findByQuestionIgnoreCase("How should null values be handled in Kotlin?")!!
        val patterns = patternRepository.findByTopicIdAndActiveTrue(topic.id)
        assertThat(patterns).hasSize(2)
        with(patterns.first()) {
            assertThat(eloRating).isEqualTo(1500.0)
            assertThat(timesShown).isEqualTo(0)
            assertThat(timesChosen).isEqualTo(0)
            assertThat(active).isTrue()
        }
    }

    @Test
    fun `importing the same payload twice is idempotent`() {
        import(payload).andExpect(status().isOk)

        import(payload)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.topicsCreated").value(0))
            .andExpect(jsonPath("$.topicsReused").value(1))
            .andExpect(jsonPath("$.patternsCreated").value(0))
            .andExpect(jsonPath("$.patternsSkipped").value(2))
            .andExpect(jsonPath("$.labelsCreated").value(0))

        assertThat(topicRepository.findAll()).hasSize(1)
        assertThat(patternRepository.findByActiveTrue()).hasSize(2)
        assertThat(labelRepository.findAll()).hasSize(2)
    }

    @Test
    fun `labels are matched case-insensitively and unknown labels are auto-created with inferred type`() {
        // pre-existing label with different casing should be reused, not duplicated
        labelRepository.save(com.mitchelnijdam.commonground.label.Label(name = "Kotlin", labelType = LabelType.LANGUAGE))

        import(
            """
            {
              "topics": [
                {
                  "question": "A topic with labels?",
                  "labels": ["kotlin", "python", "MyCustomTag"],
                  "patterns": []
                }
              ]
            }
            """.trimIndent(),
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.labelsCreated").value(2))

        val labels = labelRepository.findAll().associateBy { it.name }
        assertThat(labels).hasSize(3)
        assertThat(labels["Kotlin"]!!.labelType).isEqualTo(LabelType.LANGUAGE)
        assertThat(labels["python"]!!.labelType).isEqualTo(LabelType.LANGUAGE)
        assertThat(labels["MyCustomTag"]!!.labelType).isEqualTo(LabelType.STYLE)
    }

    @Test
    fun `a topic matched by question case-insensitively is reused`() {
        topicRepository.save(Topic(question = "Existing Question?"))

        import(
            """
            {"topics": [{"question": "EXISTING QUESTION?", "labels": [], "patterns": [
              {"title": "A pattern", "code": "x"}
            ]}]}
            """.trimIndent(),
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.topicsCreated").value(0))
            .andExpect(jsonPath("$.topicsReused").value(1))
            .andExpect(jsonPath("$.patternsCreated").value(1))

        assertThat(topicRepository.findAll()).hasSize(1)
    }

    @Test
    fun `malformed body returns 400`() {
        import("""{"topics": [{"context": "missing question"}]}""")
            .andExpect(status().isBadRequest)

        import("""not even json""")
            .andExpect(status().isBadRequest)

        import("""{"topics": []}""")
            .andExpect(status().isBadRequest)
    }
}
