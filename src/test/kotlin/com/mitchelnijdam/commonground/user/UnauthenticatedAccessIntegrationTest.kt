package com.mitchelnijdam.commonground.user

import com.mitchelnijdam.commonground.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@TestPropertySource(properties = ["commonground.auth.dev-bypass.enabled=false"])
class UnauthenticatedAccessIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `me returns 401 without a Cloudflare JWT`() {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `me returns 401 with an unverifiable Cloudflare JWT`() {
        mockMvc.perform(get("/api/users/me").header("Cf-Access-Jwt-Assertion", "not-a-real-jwt"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `health endpoint stays reachable without authentication`() {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk)
    }
}
