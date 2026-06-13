package com.mitchelnijdam.commonground.dataimport

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/import")
class ImportController(private val importService: ImportService) {

    @PostMapping
    fun import(@Valid @RequestBody request: ImportRequest): ImportResultDto =
        importService.import(request)
}
