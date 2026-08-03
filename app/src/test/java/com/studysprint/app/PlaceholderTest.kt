package com.studysprint.app

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Smoke test confirming the JVM test source set is wired up correctly.
 * Real tests are added in Phase 6 (timer state machine, stats calc, etc.).
 */
class PlaceholderTest {

    @Test
    fun `jvm unit tests are configured`() {
        assertThat(1 + 1).isEqualTo(2)
    }
}
