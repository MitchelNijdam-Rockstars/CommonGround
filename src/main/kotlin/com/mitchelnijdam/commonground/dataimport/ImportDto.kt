package com.mitchelnijdam.commonground.dataimport

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class ImportRequest(
    @field:NotEmpty
    @field:Valid
    val topics: List<ImportTopic>,
)

data class ImportTopic(
    @field:NotBlank
    val question: String,
    val context: String? = null,
    val labels: List<String> = emptyList(),
    @field:Valid
    val patterns: List<ImportPattern> = emptyList(),
)

data class ImportPattern(
    @field:NotBlank @field:Size(max = 200)
    val title: String,
    @field:NotBlank @field:Size(max = 50)
    val language: String,
    @field:NotBlank
    val code: String,
)

data class ImportResultDto(
    val topicsCreated: Int,
    val topicsReused: Int,
    val patternsCreated: Int,
    val patternsSkipped: Int,
    val labelsCreated: Int,
)
