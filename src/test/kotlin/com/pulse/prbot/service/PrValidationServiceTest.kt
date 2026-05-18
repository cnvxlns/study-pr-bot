package com.pulse.prbot.service

import com.pulse.prbot.util.PathMatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PrValidationServiceTest {
    private val service = PrValidationService(PathMatcher())

    @Test
    fun `parse valid PR titles`() {
        val titleWithSpaces = service.parseTitle("ABC350 / tourist")
        assertNotNull(titleWithSpaces)
        assertEquals("ABC350", titleWithSpaces.contestName)
        assertEquals("tourist", titleWithSpaces.atCoderHandle)

        val titleWithoutSpaces = service.parseTitle("ABC350/tourist")
        assertNotNull(titleWithoutSpaces)
        assertEquals("ABC350", titleWithoutSpaces.contestName)
        assertEquals("tourist", titleWithoutSpaces.atCoderHandle)

        val titleWithUnderscore = service.parseTitle("ABC350 / tourist_123")
        assertNotNull(titleWithUnderscore)
        assertEquals("ABC350", titleWithUnderscore.contestName)
        assertEquals("tourist_123", titleWithUnderscore.atCoderHandle)
    }

    @Test
    fun `reject invalid PR titles`() {
        assertNull(service.parseTitle("ABC350 tourist"))
        assertNull(service.parseTitle("/ tourist"))
        assertNull(service.parseTitle("ABC350 /"))
    }

    @Test
    fun `validate allowed file paths`() {
        val result = service.validate(
            prTitle = "ABC350 / lee",
            changedFiles = listOf(
                "ABC350/lee/A.kt",
                "ABC350/lee/src/Main.kt",
            ),
        )

        assertTrue(result.valid)
        assertTrue(result.invalidFiles.isEmpty())
    }

    @Test
    fun `reject files outside contest and handle directory`() {
        val result = service.validate(
            prTitle = "ABC350 / lee",
            changedFiles = listOf(
                "ABC350/kim/A.kt",
                "ABC349/lee/A.kt",
                "README.md",
            ),
        )

        assertFalse(result.valid)
        assertEquals(
            listOf(
                "ABC350/kim/A.kt",
                "ABC349/lee/A.kt",
                "README.md",
            ),
            result.invalidFiles,
        )
    }
}
