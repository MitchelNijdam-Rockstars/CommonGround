package com.mitchelnijdam.commonground.pattern

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminPatternController(private val patternService: PatternService) {

    @PostMapping("/topics/{topicId}/patterns")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable topicId: Long, @Valid @RequestBody request: CreatePatternRequest): PatternDto =
        patternService.create(topicId, request.title, request.code, request.language).toDto()

    @PatchMapping("/patterns/{patternId}/deactivate")
    fun deactivate(@PathVariable patternId: Long): PatternDto =
        patternService.deactivate(patternId).toDto()
}
