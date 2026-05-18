package com.pulse.prbot.service

import com.pulse.prbot.domain.ParsedPrTitle
import com.pulse.prbot.domain.ValidationResult
import com.pulse.prbot.util.PathMatcher
import org.springframework.stereotype.Service

@Service
class PrValidationService(
    private val pathMatcher: PathMatcher,
) {
    private val titlePattern = Regex("""^\s*([^/]+?)\s*/\s*([A-Za-z0-9_-]+)\s*$""")

    fun parseTitle(title: String): ParsedPrTitle? {
        val match = titlePattern.matchEntire(title) ?: return null
        val contestName = match.groupValues[1].trim()
        val atCoderHandle = match.groupValues[2].trim()

        if (contestName.isBlank()) {
            return null
        }

        return ParsedPrTitle(
            contestName = contestName,
            atCoderHandle = atCoderHandle,
        )
    }

    fun validate(
        prTitle: String,
        changedFiles: List<String>,
    ): ValidationResult {
        val parsedTitle = parseTitle(prTitle)
        val reasons = mutableListOf<String>()

        if (parsedTitle == null) {
            reasons += "PR title must follow `ContestName / AtCoderHandle`. Example: `ABC350 / tourist`."
            return ValidationResult.invalid(
                parsedTitle = null,
                reasons = reasons,
            )
        }

        val invalidFiles = pathMatcher.findInvalidFiles(changedFiles, parsedTitle)
        if (invalidFiles.isNotEmpty()) {
            reasons += "Changed files must be under `${parsedTitle.contestName}/${parsedTitle.atCoderHandle}/**`."
        }

        return if (reasons.isEmpty()) {
            ValidationResult.valid(parsedTitle)
        } else {
            ValidationResult.invalid(
                parsedTitle = parsedTitle,
                reasons = reasons,
                invalidFiles = invalidFiles,
            )
        }
    }
}
