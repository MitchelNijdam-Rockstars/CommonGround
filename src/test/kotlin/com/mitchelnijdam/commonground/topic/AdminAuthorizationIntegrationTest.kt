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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "commonground.auth.dev-bypass.enabled=true",
        "commonground.auth.dev-bypass.email=regular@test.dev",
        "commonground.auth.admin-emails=admin@test.dev",
    ],
)
class AdminAuthorizationIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `non-admin user receives 403 on admin endpoints`() {
        mockMvc.perform(
            post("/api/admin/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Kotlin", "labelType": "LANGUAGE"}"""),
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            post("/api/admin/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"question": "A question?"}"""),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `non-admin user can still read labels and topics`() {
        mockMvc.perform(get("/api/labels")).andExpect(status().isOk)
        mockMvc.perform(get("/api/topics")).andExpect(status().isOk)
    }
}
