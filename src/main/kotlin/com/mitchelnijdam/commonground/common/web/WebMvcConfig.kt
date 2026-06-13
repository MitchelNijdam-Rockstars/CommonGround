package com.mitchelnijdam.commonground.common.web

import com.mitchelnijdam.commonground.common.auth.AdminAuthorizationInterceptor
import com.mitchelnijdam.commonground.common.auth.CurrentUserArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver

@Configuration
class WebMvcConfig(
    private val adminAuthorizationInterceptor: AdminAuthorizationInterceptor,
    private val currentUserArgumentResolver: CurrentUserArgumentResolver,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(adminAuthorizationInterceptor).addPathPatterns("/api/admin/**")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserArgumentResolver)
    }

    /** SPA fallback: serve index.html for unknown non-API paths so Angular routes survive a refresh. */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(object : PathResourceResolver() {
                override fun getResource(resourcePath: String, location: Resource): Resource? {
                    if (resourcePath.startsWith("api/")) return null
                    val requested = location.createRelative(resourcePath)
                    return if (requested.exists() && requested.isReadable) requested
                    else location.createRelative("index.html")
                }
            })
    }
}
