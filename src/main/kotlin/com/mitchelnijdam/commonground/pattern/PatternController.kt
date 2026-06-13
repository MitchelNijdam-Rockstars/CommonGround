package com.mitchelnijdam.commonground.pattern

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/topics/{topicId}/patterns")
class PatternController(private val patternService: PatternService) {

    @GetMapping
    fun listActive(@PathVariable topicId: Long): List<PatternDto> =
        patternService.activePatternsForTopic(topicId).map { it.toDto() }
}

@RestController
@RequestMapping("/api/patterns")
class PatternDetailController(private val patternService: PatternService) {

    @GetMapping("/{patternId}")
    fun detail(@PathVariable patternId: Long): PatternDetailDto =
        patternService.detail(patternId)
}
