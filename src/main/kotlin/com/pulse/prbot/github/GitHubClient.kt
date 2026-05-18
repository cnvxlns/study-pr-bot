package com.pulse.prbot.github

import com.pulse.prbot.config.BotProperties
import com.pulse.prbot.github.dto.CreateCommentRequest
import com.pulse.prbot.github.dto.GitHubCommentDto
import com.pulse.prbot.github.dto.GitHubPullRequestDto
import com.pulse.prbot.github.dto.GitHubPullRequestFileDto
import com.pulse.prbot.github.dto.MergePullRequestRequest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class GitHubClient(
    private val properties: BotProperties,
    restClientBuilder: RestClient.Builder,
) {
    private val restClient: RestClient = restClientBuilder.clone()
        .baseUrl("https://api.github.com")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${properties.github.token}")
        .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
        .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
        .build()

    fun listOpenPullRequests(): List<GitHubPullRequestDto> =
        restClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/repos/{owner}/{repo}/pulls")
                    .queryParam("state", "open")
                    .queryParam("per_page", 100)
                    .build(properties.github.owner, properties.github.repo)
            }
            .retrieve()
            .body(object : ParameterizedTypeReference<List<GitHubPullRequestDto>>() {})
            ?: emptyList()

    fun listPullRequestFiles(pullNumber: Int): List<GitHubPullRequestFileDto> =
        restClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/repos/{owner}/{repo}/pulls/{pullNumber}/files")
                    .queryParam("per_page", 100)
                    .build(properties.github.owner, properties.github.repo, pullNumber)
            }
            .retrieve()
            .body(object : ParameterizedTypeReference<List<GitHubPullRequestFileDto>>() {})
            ?: emptyList()

    fun listIssueComments(issueNumber: Int): List<GitHubCommentDto> =
        restClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/repos/{owner}/{repo}/issues/{issueNumber}/comments")
                    .queryParam("per_page", 100)
                    .build(properties.github.owner, properties.github.repo, issueNumber)
            }
            .retrieve()
            .body(object : ParameterizedTypeReference<List<GitHubCommentDto>>() {})
            ?: emptyList()

    fun createIssueComment(issueNumber: Int, body: String) {
        restClient.post()
            .uri(
                "/repos/{owner}/{repo}/issues/{issueNumber}/comments",
                properties.github.owner,
                properties.github.repo,
                issueNumber,
            )
            .body(CreateCommentRequest(body))
            .retrieve()
            .toBodilessEntity()
    }

    fun mergePullRequest(pullNumber: Int) {
        restClient.put()
            .uri(
                "/repos/{owner}/{repo}/pulls/{pullNumber}/merge",
                properties.github.owner,
                properties.github.repo,
                pullNumber,
            )
            .body(MergePullRequestRequest())
            .retrieve()
            .toBodilessEntity()
    }
}
