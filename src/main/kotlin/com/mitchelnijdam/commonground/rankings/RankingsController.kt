package com.mitchelnijdam.commonground.rankings

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/rankings")
class RankingsController(
    private val rankingsService: RankingsService,
    private val markdownExportService: MarkdownExportService,
) {

    @GetMapping
    fun leaderboard(@RequestParam(defaultValue = "ELO") algorithm: String): List<TopicRankingDto> {
        val parsed = runCatching { RankingAlgorithm.valueOf(algorithm.uppercase()) }
            .getOrElse { throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown algorithm: $algorithm") }
        return rankingsService.leaderboard(parsed)
    }

    @GetMapping("/export")
    fun export(@RequestParam(defaultValue = "GENERIC") format: String): ResponseEntity<String> {
        val exportFormat = runCatching { ExportFormat.valueOf(format.uppercase()) }
            .getOrElse { throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown export format: $format") }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${exportFormat.filename}\"")
            .contentType(MediaType.parseMediaType("text/markdown;charset=UTF-8"))
            .body(markdownExportService.export(exportFormat))
    }
}
