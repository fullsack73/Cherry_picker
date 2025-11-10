package teamcherrypicker.com

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcherrypicker.com.data.RecommendedCard
import teamcherrypicker.com.ui.main.RecommendationList

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest=Config.NONE)
class RecommendationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleCards = listOf(
        RecommendedCard(
            cardName = "Chase Sapphire Preferred",
            matchRate = 0.95,
            benefits = listOf("5x points on travel", "3x points on dining")
        ),
        RecommendedCard(
            cardName = "Amex Gold",
            matchRate = 0.92,
            benefits = listOf("4x points on dining", "4x points at U.S. Supermarkets")
        )
    )

    @Test
    fun recommendationList_showsSummaryWhenCollapsed() {
        composeTestRule.setContent {
            RecommendationList(
                cards = sampleCards,
                isExpanded = false,
                onToggle = {}
            )
        }
        composeTestRule.onNodeWithText("Show 2 Recommendations").assertIsDisplayed()
    }

    @Test
    fun recommendationList_showsCardsWhenExpanded() {
        composeTestRule.setContent {
            RecommendationList(
                cards = sampleCards,
                isExpanded = true,
                onToggle = {}
            )
        }
        composeTestRule.onNodeWithText("Chase Sapphire Preferred").assertIsDisplayed()
        composeTestRule.onNodeWithText("Amex Gold").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hide Recommendations").assertIsDisplayed()
    }

    @Test
    fun recommendationCardItem_displaysAllElements() {
        val card = sampleCards.first()
        composeTestRule.setContent {
            teamcherrypicker.com.ui.main.RecommendationCardItem(card = card)
        }

        composeTestRule.onNodeWithText(card.cardName).assertIsDisplayed()
        composeTestRule.onNodeWithText("95% Match").assertIsDisplayed()
        composeTestRule.onNodeWithText("5x points on travel").assertIsDisplayed()
        composeTestRule.onNodeWithText("3x points on dining").assertIsDisplayed()
        composeTestRule.onNodeWithText("Apply Now").assertIsDisplayed()
        composeTestRule.onNodeWithText("View Details").assertIsDisplayed()
    }
}
