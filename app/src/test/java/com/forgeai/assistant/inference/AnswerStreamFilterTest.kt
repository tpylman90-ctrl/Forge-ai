package com.forgeai.assistant.inference

import org.junit.Assert.assertEquals
import org.junit.Test

class AnswerStreamFilterTest {
    @Test fun hiddenEmptyThinkTags() {
        val f = AnswerStreamFilter()
        f.append("<think>\n\n</think>\n\nHello!")
        assertEquals("Hello!", f.finish())
    }

    @Test fun splitThinkTagsNeverLeak() {
        val f = AnswerStreamFilter()
        assertEquals("", f.append("<"))
        assertEquals("", f.append("thi"))
        assertEquals("", f.append("nk>secret"))
        assertEquals("", f.append(" internal reasoning"))
        assertEquals("", f.append("</thi"))
        assertEquals("Hi", f.append("nk>Hi"))
        assertEquals("Hi", f.finish())
    }

    @Test fun multipleHiddenBlocksInAnswer() {
        val f = AnswerStreamFilter()
        f.append("Begin <think>ignore</think>middle <think>more private</think>end.")
        assertEquals("Begin middle end.", f.finish())
    }

    @Test fun ordinaryTextStreamsWithoutDelay() {
        val f = AnswerStreamFilter()
        assertEquals("Hello", f.append("Hello"))
        assertEquals("Hello world", f.append(" world"))
        assertEquals("Hello world", f.finish())
    }

    @Test fun unfinishedThoughtStaysHidden() {
        val f = AnswerStreamFilter()
        f.append("Visible <think>private")
        assertEquals("Visible", f.finish())
    }
}
