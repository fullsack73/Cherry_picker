package teamcherrypicker.com

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import teamcherrypicker.com.ui.main.MainScreen

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mainScreen_mapIsVisible() {
        composeTestRule.setContent {
            MainScreen()
        }

        composeTestRule.onNodeWithTag("map").assertIsDisplayed()
    }

    @Test
    fun mainScreen_searchBarIsVisible() {
        composeTestRule.setContent {
            MainScreen()
        }

        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()
    }

    @Test
    fun mainScreen_settingsAndFilterIconsAreVisible() {
        composeTestRule.setContent {
            MainScreen()
        }

        composeTestRule.onNodeWithContentDescription("Settings").assertIsDisplayed()
        // composeTestRule.onNodeWithContentDescription("Filter").assertIsDisplayed()
    }
}