package com.mitchelnijdam.commonground.label

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/labels")
class LabelController(private val labelRepository: LabelRepository) {

    @GetMapping
    fun list(@RequestParam(required = false) type: LabelType?): List<LabelDto> {
        val labels = if (type != null) labelRepository.findByLabelType(type) else labelRepository.findAll()
        return labels.sortedBy { it.name.lowercase() }.map { it.toDto() }
    }
}
