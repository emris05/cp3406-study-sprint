package com.studysprint.app.ui.components

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FormatClockTest {

    @Test
    fun `zero seconds formats as 0_00`() {
        assertThat(formatClock(0)).isEqualTo("0:00")
    }

    @Test
    fun `twenty-five minutes formats as 25_00`() {
        assertThat(formatClock(25 * 60)).isEqualTo("25:00")
    }

    @Test
    fun `seconds under a minute show leading zero on minutes`() {
        assertThat(formatClock(45)).isEqualTo("0:45")
    }

    @Test
    fun `minutes and seconds both show`() {
        assertThat(formatClock(5 * 60 + 3)).isEqualTo("5:03")
    }

    @Test
    fun `over an hour switches to h_mm_ss`() {
        assertThat(formatClock(90 * 60)).isEqualTo("1:30:00")
    }

    @Test
    fun `negative input clamps to zero`() {
        assertThat(formatClock(-100)).isEqualTo("0:00")
    }
}
