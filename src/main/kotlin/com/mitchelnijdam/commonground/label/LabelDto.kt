package com.mitchelnijdam.commonground.label

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LabelDto(
    val id: Long,
    val name: String,
    val labelType: LabelType,
)

data class CreateLabelRequest(
    @field:NotBlank @field:Size(max = 100)
    val name: String,
    val labelType: LabelType,
)

data class RenameLabelRequest(
    @field:NotBlank @field:Size(max = 100)
    val name: String,
)

fun Label.toDto(): LabelDto = LabelDto(id = id, name = name, labelType = labelType)
