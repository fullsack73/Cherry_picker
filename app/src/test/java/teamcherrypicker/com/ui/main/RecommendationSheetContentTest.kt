package teamcherrypicker.com.ui.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcherrypicker.com.data.RecommendationCard
import teamcherrypicker.com.data.RecommendationMeta
import teamcherrypicker.com.data.RecommendationScoreBreakdown
import teamcherrypicker.com.data.RecommendationScoreSource
import teamcherrypicker.com.data.Store

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class RecommendationSheetContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleStore = Store(
        id = 1,
        name = "Cherry Coffee",
        branch = "Downtown",
        address = "123 Main St",
        latitude = 0.0,
        longitude = 0.0,
        sourceCategory = "DINING",
        normalizedCategory = "DINING",
        distance = 0.0
    )

    private val sampleCard = RecommendationCard(
        cardId = 10,
        cardName = "Cherry Rewards",
        issuer = "Cherry Bank",
        normalizedCategories = listOf("DINING"),
        score = 95,
        scoreSource = RecommendationScoreSource.LOCATION,
        rationale = "Matched keyword \"Cherry\""
    )

    @Test
    fun discoverButtonDisabledDuringInFlight() {
        val uiState = baseUiState().copy(
            discoverInFlight = true,
            discoverMode = false
        )

        composeTestRule.setContent {
            RecommendationSheetContent(
                storeName = sampleStore.name,
                uiState = uiState,
                onRetry = {},
                onDiscover = {},
                onShowOwned = {}
            )
        }

        composeTestRule.onNodeWithTag("discoverButton").assertIsNotEnabled()
    }

    @Test
    fun fallbackBannerShowsWhenMessagePresent() {
        val uiState = baseUiState().copy(fallbackBannerMessage = "Showing heuristic matches")

        composeTestRule.setContent {
            RecommendationSheetContent(
                storeName = sampleStore.name,
                uiState = uiState,
                onRetry = {},
                onDiscover = {},
                onShowOwned = {}
            )
        }

        composeTestRule.onNodeWithTag("fallbackBanner").assertIsDisplayed()
    }

    @Test
    fun ownedCardsNoticeVisibleWhenEmpty() {
        val uiState = baseUiState(cards = emptyList()).copy(ownedCardIds = emptySet())

        composeTestRule.setContent {
            RecommendationSheetContent(
                storeName = sampleStore.name,
                uiState = uiState,
                onRetry = {},
                onDiscover = {},
                onShowOwned = {}
            )
        }

        composeTestRule.onNodeWithTag("ownedCardsInfo").assertIsDisplayed()
    }

    @Test
    fun ownedCardsNoticeHiddenWhenCardsExist() {
        val uiState = baseUiState(cards = emptyList()).copy(ownedCardIds = setOf(1, 2))

        composeTestRule.setContent {
            RecommendationSheetContent(
                storeName = sampleStore.name,
                uiState = uiState,
                onRetry = {},
                onDiscover = {},
                onShowOwned = {}
            )
        }

        val matches = composeTestRule.onAllNodesWithTag("ownedCardsInfo").fetchSemanticsNodes()
        assertTrue(matches.isEmpty())
    }

    private fun baseUiState(cards: List<RecommendationCard> = listOf(sampleCard)): RecommendationUiState {
        val meta = RecommendationMeta(
            total = cards.size,
            limit = 10,
            discover = false,
            storeId = sampleStore.id,
            latencyMs = 12,
            cached = false,
            scoreSources = RecommendationScoreBreakdown(location = 1)
        )
        return RecommendationUiState(
            selectedStore = sampleStore,
            cards = cards,
            meta = meta,
            ownedCardIds = setOf(1),
            isLoading = false,
            discoverMode = false,
            discoverInFlight = false,
            fallbackBannerMessage = null
        )
    }
}
