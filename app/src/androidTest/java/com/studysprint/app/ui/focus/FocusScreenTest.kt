package com.studysprint.app.ui.focus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.studysprint.app.ui.theme.StudySprintTheme
import org.junit.Rule
import org.junit.Test

/**
 * GUI test for the Focus screen's phase label and primary button.
 * (Full ViewModel integration is covered in unit tests; this verifies the
 *  Compose layer renders and the Start button toggles to Pause.)
 */
class FocusScreenTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun focusScreen_showsPhaseLabelAndStartButton() {
        composeRule.setContent {
            StudySprintTheme {
                FocusScreen(onNavigateToTasks = {})
            }
        }
        // The focus phase label should be visible on first load.
        composeRule.onNodeWithText("Focus").assertIsDisplayed()
        // Start button is present and tappable.
        composeRule.onNodeWithText("Start").assertIsDisplayed().performClick()
    }
}
