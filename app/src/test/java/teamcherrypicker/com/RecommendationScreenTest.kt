package teamcherrypicker.com

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcherrypicker.com.data.CardBenefit
import teamcherrypicker.com.data.CardSummary
import teamcherrypicker.com.ui.main.RecommendationList
import teamcherrypicker.com.ui.main.RecommendedCard

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest=Config.NONE)
class RecommendationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleCards = listOf(
        RecommendedCard(
            summary = CardSummary(
                id = 1,
                name = "Chase Sapphire Preferred",
                issuer = "Chase",
                normalizedCategories = listOf("TRAVEL", "DINING")
            ),
            matchRate = 0.95,
            benefits = listOf(
                CardBenefit(id = 101, description = "5x points on travel", normalizedCategory = "TRAVEL", keyword = null),
                CardBenefit(id = 102, description = "3x points on dining", normalizedCategory = "DINING", keyword = null)
            )
        ),
        RecommendedCard(
            summary = CardSummary(
                id = 2,
                name = "Amex Gold",
                issuer = "American Express",
                normalizedCategories = listOf("DINING", "GROCERIES")
            ),
            matchRate = 0.92,
            benefits = listOf(
                CardBenefit(id = 201, description = "4x points on dining", normalizedCategory = "DINING", keyword = null),
                CardBenefit(id = 202, description = "4x points at U.S. Supermarkets", normalizedCategory = "GROCERIES", keyword = null)
            )
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

        composeTestRule.onNodeWithText(card.summary.name).assertIsDisplayed()
        composeTestRule.onNodeWithText("95% Match").assertIsDisplayed()
        composeTestRule.onNodeWithText("5x points on travel").assertIsDisplayed()
        composeTestRule.onNodeWithText("3x points on dining").assertIsDisplayed()
        composeTestRule.onNodeWithText("Apply Now").assertIsDisplayed()
        composeTestRule.onNodeWithText("View Details").assertIsDisplayed()
    }
}
