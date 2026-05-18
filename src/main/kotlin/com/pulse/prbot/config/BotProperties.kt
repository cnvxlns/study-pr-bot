package com.pulse.prbot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bot")
data class BotProperties(
    val github: GitHub = GitHub(),
    val dryRun: Boolean = true,
    val discord: Discord = Discord(),
) {
    data class GitHub(
        val owner: String = "",
        val repo: String = "",
        val token: String = "",
    )

    data class Discord(
        val enabled: Boolean = false,
        val webhookUrl: String = "",
    )
}
