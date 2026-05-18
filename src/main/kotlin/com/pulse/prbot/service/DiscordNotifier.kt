package com.pulse.prbot.service

import com.pulse.prbot.config.BotProperties
import com.pulse.prbot.domain.ProcessResult
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class DiscordNotifier(
    private val properties: BotProperties,
    restClientBuilder: RestClient.Builder,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient: RestClient = restClientBuilder.clone().build()

    fun notifyInvalidPullRequests(results: List<ProcessResult>) {
        val invalidResults = results.filter { it.invalidPr }

        if (!properties.discord.enabled) {
            log.debug("Discord notification is disabled")
            return
        }

        if (properties.discord.webhookUrl.isBlank()) {
            log.debug("Discord webhook URL is blank")
            return
        }

        if (invalidResults.isEmpty()) {
            log.info("No invalid PRs to send to Discord")
            return
        }

        val content = buildInvalidPrSummary(invalidResults)
        if (properties.dryRun) {
            log.info("Would send Discord notification:\n{}", content)
            return
        }

        restClient.post()
            .uri(properties.discord.webhookUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("content" to content))
            .retrieve()
            .toBodilessEntity()

        log.info("Sent Discord notification for {} invalid PR(s)", invalidResults.size)
    }

    private fun buildInvalidPrSummary(invalidResults: List<ProcessResult>): String {
        val content = buildString {
            appendLine("PR Guardian Bot found invalid PR(s):")
            invalidResults.forEach { result ->
                appendLine("- #${result.pr.number} ${result.pr.title} (${result.pr.htmlUrl})")
                result.reasons.forEach { reason ->
                    appendLine("  - $reason")
                }
                if (result.invalidFiles.isNotEmpty()) {
                    appendLine("  - Invalid files: ${result.invalidFiles.joinToString(", ")}")
                }
            }
        }

        return content.truncateDiscordMessage()
    }

    private fun String.truncateDiscordMessage(): String {
        val maxLength = 1900
        return if (length <= maxLength) {
            this
        } else {
            take(maxLength - 15) + "\n...(truncated)"
        }
    }
}
