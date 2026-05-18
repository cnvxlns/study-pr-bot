package com.pulse.prbot.github.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubPullRequestDto(
    @JsonProperty("number")
    val number: Int,

    @JsonProperty("title")
    val title: String,

    @JsonProperty("draft")
    val draft: Boolean = false,

    @JsonProperty("html_url")
    val htmlUrl: String = "",
)
