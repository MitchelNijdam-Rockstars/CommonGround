package com.mitchelnijdam.commonground.common.auth

import com.mitchelnijdam.commonground.user.User
import com.mitchelnijdam.commonground.user.UserRole
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/** Guards the admin endpoints: only Users with the ADMIN role may pass. */
@Component
class AdminAuthorizationInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val user = request.getAttribute(AuthenticationFilter.USER_ATTRIBUTE) as? User
        if (user?.role == UserRole.ADMIN) return true

        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write("""{"error": "Admin role required"}""")
        return false
    }
}
