package com.pulse.prbot.github.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class MergePullRequestRequest(
    @JsonProperty("merge_method")
    val mergeMethod: String = "squash",
)
