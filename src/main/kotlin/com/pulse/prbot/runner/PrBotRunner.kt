package com.pulse.prbot.runner

import com.pulse.prbot.service.DiscordNotifier
import com.pulse.prbot.service.PullRequestService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class PrBotRunner(
    private val pullRequestService: PullRequestService,
    private val discordNotifier: DiscordNotifier,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        log.info("Starting PR Guardian Bot")
        val results = pullRequestService.processOpenPullRequests()
        discordNotifier.notifyInvalidPullRequests(results)
        log.info("Finished PR Guardian Bot")
    }
}
