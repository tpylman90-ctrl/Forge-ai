package com.forgeai.assistant.inference

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionPromptBuilderTest {
    @Test fun firstTurnNoHistory() {
        assertEquals("Hello\n/no_think", SessionPromptBuilder.build(emptyList(), "Hello"))
    }

    @Test fun followUpIncludesRecentUserFact() {
        val result = SessionPromptBuilder.build(
            listOf("my favorite color is purple"),
            "what color did I just say?"
        )
        assertTrue(result.contains("my favorite color is purple"))
        assertTrue(result.contains("what color did I just say?"))
        assertTrue(result.endsWith("/no_think"))
    }

    @Test fun skipsOldMessagesAndLimitsContext() {
        val previous = (1..8).map { "turn $it " + "x".repeat(400) }
        val result = SessionPromptBuilder.build(previous, "Latest")
        assertFalse(result.contains("turn 1"))
        assertFalse(result.contains("turn 4"))
        assertTrue(result.contains("turn 8"))
        assertTrue(result.length < 1100)
    }
}
