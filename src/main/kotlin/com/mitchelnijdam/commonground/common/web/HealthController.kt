package com.mitchelnijdam.commonground.common.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/health")
class HealthController {

    @GetMapping
    fun health(): HealthDto = HealthDto(status = "UP")
}

data class HealthDto(val status: String)
