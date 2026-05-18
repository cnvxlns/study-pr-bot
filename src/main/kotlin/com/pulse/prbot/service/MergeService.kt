package com.pulse.prbot.service

import com.pulse.prbot.config.BotProperties
import com.pulse.prbot.domain.PullRequestInfo
import com.pulse.prbot.github.GitHubClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MergeService(
    private val gitHubClient: GitHubClient,
    private val properties: BotProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun merge(pr: PullRequestInfo): MergeOutcome {
        if (pr.draft) {
            log.info("Skip merge for draft PR #{}", pr.number)
            return MergeOutcome.SKIPPED_DRAFT
        }

        if (properties.dryRun) {
            log.info("Would merge PR #{} with squash method", pr.number)
            return MergeOutcome.WOULD_MERGE
        }

        return try {
            gitHubClient.mergePullRequest(pr.number)
            log.info("Merged PR #{} with squash method", pr.number)
            MergeOutcome.MERGED
        } catch (ex: Exception) {
            log.error("Failed to merge PR #{} with squash method", pr.number, ex)
            throw ex
        }
    }
}

enum class MergeOutcome {
    MERGED,
    WOULD_MERGE,
    SKIPPED_DRAFT,
}
