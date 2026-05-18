package com.pulse.prbot.util

import com.pulse.prbot.domain.ParsedPrTitle
import org.springframework.stereotype.Component

@Component
class PathMatcher {
    fun isAllowed(changedFile: String, parsedTitle: ParsedPrTitle): Boolean {
        val normalizedPath = changedFile.replace('\\', '/')
        val allowedPrefix = "${parsedTitle.contestName}/${parsedTitle.atCoderHandle}/"

        return normalizedPath.startsWith(allowedPrefix) &&
            normalizedPath.length > allowedPrefix.length
    }

    fun findInvalidFiles(
        changedFiles: List<String>,
        parsedTitle: ParsedPrTitle,
    ): List<String> = changedFiles.filterNot { isAllowed(it, parsedTitle) }
}
