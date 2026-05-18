package com.pulse.prbot.github.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubCommentDto(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("body")
    val body: String? = null,
)
