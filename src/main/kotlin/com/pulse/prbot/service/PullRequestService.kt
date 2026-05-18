package com.pulse.prbot.service

import com.pulse.prbot.domain.ProcessResult
import com.pulse.prbot.domain.ProcessStatus
import com.pulse.prbot.domain.PullRequestInfo
import com.pulse.prbot.domain.ValidationResult
import com.pulse.prbot.github.GitHubClient
import com.pulse.prbot.github.dto.GitHubPullRequestDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PullRequestService(
    private val gitHubClient: GitHubClient,
    private val prValidationService: PrValidationService,
    private val commentService: CommentService,
    private val mergeService: MergeService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun processOpenPullRequests(): List<ProcessResult> {
        val pullRequests = gitHubClient.listOpenPullRequests()
            .map { it.toPullRequestInfo() }

        log.info("Found {} open pull request(s)", pullRequests.size)

        val results = pullRequests.map { pr ->
            processSafely(pr)
        }

        logSummary(results)
        return results
    }

    private fun processSafely(pr: PullRequestInfo): ProcessResult =
        try {
            process(pr)
        } catch (ex: Exception) {
            log.error("Failed to process PR #{}", pr.number, ex)
            ProcessResult(
                pr = pr,
                status = ProcessStatus.FAILED,
                errorMessage = ex.message,
            )
        }

    private fun process(pr: PullRequestInfo): ProcessResult {
        log.info("Processing PR #{}: {}", pr.number, pr.title)

        val changedFiles = gitHubClient.listPullRequestFiles(pr.number)
            .map { it.filename }
        val validationResult = prValidationService.validate(pr.title, changedFiles)

        if (validationResult.invalid) {
            return handleInvalidPr(pr, validationResult)
        }

        return handleValidPr(pr)
    }

    private fun handleInvalidPr(
        pr: PullRequestInfo,
        validationResult: ValidationResult,
    ): ProcessResult {
        val outcome = commentService.handleInvalidPr(pr, validationResult)
        val status = when (outcome) {
            CommentOutcome.COMMENTED -> ProcessStatus.INVALID_COMMENTED
            CommentOutcome.ALREADY_EXISTS -> ProcessStatus.INVALID_COMMENT_ALREADY_EXISTS
            CommentOutcome.WOULD_COMMENT -> ProcessStatus.WOULD_COMMENT
        }

        return ProcessResult(
            pr = pr,
            status = status,
            reasons = validationResult.reasons,
            invalidFiles = validationResult.invalidFiles,
        )
    }

    private fun handleValidPr(pr: PullRequestInfo): ProcessResult {
        val outcome = mergeService.merge(pr)
        val status = when (outcome) {
            MergeOutcome.MERGED -> ProcessStatus.MERGED
            MergeOutcome.WOULD_MERGE -> ProcessStatus.WOULD_MERGE
            MergeOutcome.SKIPPED_DRAFT -> ProcessStatus.SKIPPED_DRAFT
        }

        return ProcessResult(
            pr = pr,
            status = status,
        )
    }

    private fun logSummary(results: List<ProcessResult>) {
        val counts = results.groupingBy { it.status }.eachCount()

        log.info(
            "Summary: total={}, merged={}, wouldMerge={}, skippedDraft={}, invalidCommented={}, invalidAlreadyCommented={}, wouldComment={}, failed={}",
            results.size,
            counts[ProcessStatus.MERGED] ?: 0,
            counts[ProcessStatus.WOULD_MERGE] ?: 0,
            counts[ProcessStatus.SKIPPED_DRAFT] ?: 0,
            counts[ProcessStatus.INVALID_COMMENTED] ?: 0,
            counts[ProcessStatus.INVALID_COMMENT_ALREADY_EXISTS] ?: 0,
            counts[ProcessStatus.WOULD_COMMENT] ?: 0,
            counts[ProcessStatus.FAILED] ?: 0,
        )

        results.filter { it.invalidPr }.forEach { result ->
            log.info(
                "Invalid PR #{}: reasons={}, invalidFiles={}",
                result.pr.number,
                result.reasons,
                result.invalidFiles,
            )
        }
    }

    private fun GitHubPullRequestDto.toPullRequestInfo(): PullRequestInfo =
        PullRequestInfo(
            number = number,
            title = title,
            draft = draft,
            htmlUrl = htmlUrl,
        )
}
