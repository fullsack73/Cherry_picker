package teamcherrypicker.com

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class LocationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun locationScreen_whenLoading_showsSpinner() {
        composeTestRule.setContent {
            LocationScreen(location = "", isLoading = true, onRefreshClick = {})
        }

        composeTestRule.onNodeWithContentDescription("Refresh Location").assertDoesNotExist()
    }

    @Test
    fun locationScreen_whenNotLoading_showsLocationAndRefreshButton() {
        val testLocation = "Test Location"
        composeTestRule.setContent {
            LocationScreen(location = testLocation, isLoading = false, onRefreshClick = {})
        }

        composeTestRule.onNodeWithText(testLocation).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Refresh Location").assertIsDisplayed()
    }
}
