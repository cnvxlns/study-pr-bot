package com.pulse.prbot.service

import com.pulse.prbot.github.dto.GitHubCommentDto
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommentServiceTest {
    @Test
    fun `do not create duplicate comment when marker exists`() {
        val comments = listOf(
            GitHubCommentDto(
                id = 1,
                body = "Existing comment\n${CommentService.COMMENT_MARKER}",
            ),
        )

        assertFalse(CommentService.shouldCreateGuardianComment(comments))
    }

    @Test
    fun `create comment when marker does not exist`() {
        val comments = listOf(
            GitHubCommentDto(
                id = 1,
                body = "Regular user comment",
            ),
        )

        assertTrue(CommentService.shouldCreateGuardianComment(comments))
    }
}
