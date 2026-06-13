package com.mitchelnijdam.commonground.user

import com.mitchelnijdam.commonground.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "commonground.auth.dev-bypass.enabled=true",
        "commonground.auth.dev-bypass.email=admin@test.dev",
        "commonground.auth.admin-emails=admin@test.dev,other-admin@test.dev",
    ],
)
class UserAuthenticationIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun `me returns the authenticated user with ADMIN role for allowlisted email`() {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("admin@test.dev"))
            .andExpect(jsonPath("$.role").value("ADMIN"))
    }

    @Test
    fun `user record is created on first login and reused on subsequent logins`() {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isOk)
        val firstId = userRepository.findByEmailIgnoreCase("admin@test.dev")?.id
        assertNotNull(firstId)

        mockMvc.perform(get("/api/users/me")).andExpect(status().isOk)
        assertEquals(firstId, userRepository.findByEmailIgnoreCase("admin@test.dev")?.id)
        assertEquals(1, userRepository.findAll().count { it.email == "admin@test.dev" })
    }

    @Test
    fun `health endpoint is reachable without authentication context`() {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
    }
}
