package com.mitchelnijdam.commonground.dataimport

import com.mitchelnijdam.commonground.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
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
class ImportAuthorizationIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `non-admin user receives 403 on the import endpoint`() {
        mockMvc.perform(
            post("/api/admin/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"topics": [{"question": "A question?", "labels": [], "patterns": []}]}"""),
        ).andExpect(status().isForbidden)
    }
}
