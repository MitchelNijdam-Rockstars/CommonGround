package com.mitchelnijdam.commonground.label

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/admin/labels")
class AdminLabelController(private val labelRepository: LabelRepository) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateLabelRequest): LabelDto {
        labelRepository.findByNameIgnoreCase(request.name)?.let {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Label '${request.name}' already exists")
        }
        return labelRepository.save(Label(name = request.name, labelType = request.labelType)).toDto()
    }

    @PatchMapping("/{labelId}")
    fun rename(@PathVariable labelId: Long, @Valid @RequestBody request: RenameLabelRequest): LabelDto {
        val label = labelRepository.findById(labelId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Label $labelId not found") }
        label.name = request.name
        return labelRepository.save(label).toDto()
    }
}
