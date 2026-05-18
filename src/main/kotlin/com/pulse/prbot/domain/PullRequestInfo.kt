package com.pulse.prbot.domain

data class PullRequestInfo(
    val number: Int,
    val title: String,
    val draft: Boolean,
    val htmlUrl: String,
)
