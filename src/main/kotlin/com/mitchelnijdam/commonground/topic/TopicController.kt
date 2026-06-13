package com.mitchelnijdam.commonground.topic

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/topics")
class TopicController(private val topicService: TopicService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) labelId: Long?,
    ): List<TopicDto> = topicService.search(search, labelId).map { it.toDto() }
}
