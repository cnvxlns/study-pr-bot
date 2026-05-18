package com.pulse.prbot.github.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubPullRequestFileDto(
    @JsonProperty("filename")
    val filename: String,
)
