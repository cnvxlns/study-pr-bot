package com.pulse.prbot.github.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateCommentRequest(
    @JsonProperty("body")
    val body: String,
)
