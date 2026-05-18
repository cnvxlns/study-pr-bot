package com.pulse.prbot.domain

data class ProcessResult(
    val pr: PullRequestInfo,
    val status: ProcessStatus,
    val reasons: List<String> = emptyList(),
    val invalidFiles: List<String> = emptyList(),
    val errorMessage: String? = null,
) {
    val invalidPr: Boolean
        get() = status in invalidStatuses

    companion object {
        val invalidStatuses: Set<ProcessStatus> = setOf(
            ProcessStatus.INVALID_COMMENTED,
            ProcessStatus.INVALID_COMMENT_ALREADY_EXISTS,
            ProcessStatus.WOULD_COMMENT,
        )
    }
}

enum class ProcessStatus {
    MERGED,
    WOULD_MERGE,
    SKIPPED_DRAFT,
    INVALID_COMMENTED,
    INVALID_COMMENT_ALREADY_EXISTS,
    WOULD_COMMENT,
    FAILED,
}
