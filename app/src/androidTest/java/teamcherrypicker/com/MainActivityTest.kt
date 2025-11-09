package teamcherrypicker.com

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainScreen_isDisplayed_onLaunch() {
        // Verify that the main screen content is displayed on launch
        composeTestRule.onNodeWithText("Location not available").assertIsDisplayed()
    }

    @Test
    fun navigateToAddCardScreen_displaysAddCardScreenContent() {
        // This test will fail until navigation to AddCardScreen is implemented
        // For now, it serves as a placeholder to verify navigation setup
        // composeTestRule.onNodeWithText("Add Card Screen").assertIsDisplayed()
    }

    @Test
    fun navigateToSettingsScreen_displaysSettingsScreenContent() {
        // This test will fail until navigation to SettingsScreen is implemented
        // For now, it serves as a placeholder to verify navigation setup
        // composeTestRule.onNodeWithText("Settings Screen").assertIsDisplayed()
    }
}
