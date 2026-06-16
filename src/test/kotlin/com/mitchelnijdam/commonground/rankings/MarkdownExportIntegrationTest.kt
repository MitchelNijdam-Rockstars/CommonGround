package com.mitchelnijdam.commonground.rankings

import com.mitchelnijdam.commonground.IntegrationTestBase
import com.mitchelnijdam.commonground.pattern.Pattern
import com.mitchelnijdam.commonground.pattern.PatternRepository
import com.mitchelnijdam.commonground.topic.Topic
import com.mitchelnijdam.commonground.topic.TopicRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate

@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "commonground.auth.dev-bypass.enabled=true",
        "commonground.auth.dev-bypass.email=dev@test.dev",
    ],
)
class MarkdownExportIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var topicRepository: TopicRepository

    @Autowired
    lateinit var patternRepository: PatternRepository

    @Test
    fun `export streams a markdown attachment with a dated filename`() {
        val topic = topicRepository.save(Topic(question = "How to handle nulls?"))
        patternRepository.save(
            Pattern(topic = topic, title = "Winner", code = "val x = 1", language = "kotlin", eloRating = 1600.0, timesShown = 5),
        )

        val response = mockMvc.perform(get("/api/rankings/export"))
            .andExpect(status().isOk)
            .andReturn().response

        val disposition = response.getHeader(HttpHeaders.CONTENT_DISPOSITION)
        assertThat(disposition)
            .isNotNull()
            .contains("attachment")
            .contains("common-ground-${LocalDate.now()}.md")
        assertThat(response.contentType).startsWith("text/markdown")
    }

    @Test
    fun `export contains the question, winning pattern title and fenced code block`() {
        val topic = topicRepository.save(Topic(question = "How to handle nulls?"))
        patternRepository.save(
            Pattern(topic = topic, title = "Low elo", code = "loser code", language = "kotlin", eloRating = 1400.0, timesShown = 3),
        )
        patternRepository.save(
            Pattern(topic = topic, title = "High elo", code = "val winner = true", language = "kotlin", eloRating = 1700.0, timesShown = 4),
        )

        val body = mockMvc.perform(get("/api/rankings/export"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        assertThat(body).contains("## How to handle nulls?", "High elo", "```kotlin", "val winner = true")
        // The losing pattern must not appear.
        assertThat(body).doesNotContain("Low elo", "loser code")
    }

    @Test
    fun `topics without any voted pattern are excluded`() {
        val voted = topicRepository.save(Topic(question = "Voted topic?"))
        patternRepository.save(
            Pattern(topic = voted, title = "Seen", code = "a", language = "kotlin", timesShown = 2),
        )

        val unvoted = topicRepository.save(Topic(question = "Unvoted topic?"))
        patternRepository.save(
            Pattern(topic = unvoted, title = "Never shown", code = "b", language = "kotlin", timesShown = 0),
        )

        val body = mockMvc.perform(get("/api/rankings/export"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        assertThat(body).contains("## Voted topic?")
        assertThat(body).doesNotContain("Unvoted topic?")
    }

    @Test
    fun `topics are ordered by id and output is deterministic`() {
        val first = topicRepository.save(Topic(question = "First question?"))
        patternRepository.save(Pattern(topic = first, title = "P1", code = "1", language = "kotlin", timesShown = 1))
        val second = topicRepository.save(Topic(question = "Second question?"))
        patternRepository.save(Pattern(topic = second, title = "P2", code = "2", language = "kotlin", timesShown = 1))

        val body1 = mockMvc.perform(get("/api/rankings/export")).andReturn().response.contentAsString
        val body2 = mockMvc.perform(get("/api/rankings/export")).andReturn().response.contentAsString

        assertThat(body1.indexOf("First question?")).isLessThan(body1.indexOf("Second question?"))
        assertThat(body2).isEqualTo(body1)
    }
}
