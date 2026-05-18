package com.pulse.prbot.service

import com.pulse.prbot.config.BotProperties
import com.pulse.prbot.domain.PullRequestInfo
import com.pulse.prbot.domain.ValidationResult
import com.pulse.prbot.github.GitHubClient
import com.pulse.prbot.github.dto.GitHubCommentDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CommentService(
    private val gitHubClient: GitHubClient,
    private val properties: BotProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handleInvalidPr(
        pr: PullRequestInfo,
        validationResult: ValidationResult,
    ): CommentOutcome {
        val existingComments = gitHubClient.listIssueComments(pr.number)

        if (!shouldCreateGuardianComment(existingComments)) {
            log.info("Skip comment for PR #{} because guardian comment already exists", pr.number)
            return CommentOutcome.ALREADY_EXISTS
        }

        val body = buildInvalidPrComment(pr, validationResult)
        if (properties.dryRun) {
            log.info("Would comment on PR #{}:\n{}", pr.number, body)
            return CommentOutcome.WOULD_COMMENT
        }

        gitHubClient.createIssueComment(pr.number, body)
        log.info("Created validation failure comment on PR #{}", pr.number)
        return CommentOutcome.COMMENTED
    }

    companion object {
        const val COMMENT_MARKER: String = "<!-- pr-guardian-bot -->"

        fun shouldCreateGuardianComment(comments: List<GitHubCommentDto>): Boolean =
            comments.none { it.body?.contains(COMMENT_MARKER) == true }

        fun buildInvalidPrComment(
            pr: PullRequestInfo,
            validationResult: ValidationResult,
        ): String = buildString {
            appendLine(COMMENT_MARKER)
            appendLine("### PR validation failed")
            appendLine()
            appendLine("This PR cannot be merged automatically for the following reason(s):")
            validationResult.reasons.forEach { reason ->
                appendLine("- $reason")
            }

            if (validationResult.invalidFiles.isNotEmpty()) {
                appendLine()
                appendLine("Invalid file path(s):")
                validationResult.invalidFiles.forEach { file ->
                    appendLine("- `$file`")
                }
            }

            appendLine()
            appendLine("Expected title format: `ContestName / AtCoderHandle`")
            appendLine("Expected file path format: `ContestName/AtCoderHandle/**`")
            appendLine()
            appendLine("PR: #${pr.number} `${pr.title}`")
        }
    }
}

enum class CommentOutcome {
    COMMENTED,
    ALREADY_EXISTS,
    WOULD_COMMENT,
}
