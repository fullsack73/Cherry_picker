package teamcherrypicker.com.ui.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import teamcherrypicker.com.api.ApiService
import teamcherrypicker.com.api.CardBenefitsResponse
import teamcherrypicker.com.api.CardsMetaDto
import teamcherrypicker.com.api.CardsResponse
import teamcherrypicker.com.api.RecommendationMetaDto
import teamcherrypicker.com.api.RecommendationRequestDto
import teamcherrypicker.com.api.RecommendationsResponse
import teamcherrypicker.com.api.ScoreSourcesDto
import teamcherrypicker.com.api.StoresResponse
import teamcherrypicker.com.data.CardsRepository
import teamcherrypicker.com.data.StoreRepository
import teamcherrypicker.com.data.UserLocation
import teamcherrypicker.com.location.LocationPermissionStatus
import teamcherrypicker.com.location.LocationUiState

class LocationPermissionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeApiService = object : ApiService {
        override suspend fun sendLocation(location: UserLocation): Response<Unit> {
            return Response.success(Unit)
        }

        override suspend fun getCards(
            limit: Int?,
            offset: Int?,
            category: String?
        ): CardsResponse {
            return CardsResponse(
                data = emptyList(),
                meta = CardsMetaDto(0, 0, 0, null, null)
            )
        }

        override suspend fun getCardBenefits(cardId: Int): CardBenefitsResponse {
            return CardBenefitsResponse(data = emptyList())
        }

        override suspend fun getNearbyStores(
            latitude: Double,
            longitude: Double,
            radius: Int?,
            categories: String?
        ): StoresResponse = StoresResponse(data = emptyList())

        override suspend fun searchStores(query: String, limit: Int?): StoresResponse = StoresResponse(data = emptyList())

        override suspend fun getRecommendations(request: RecommendationRequestDto): RecommendationsResponse {
            return RecommendationsResponse(
                data = emptyList(),
                meta = RecommendationMetaDto(
                    total = 0,
                    limit = request.limit,
                    storeId = request.storeId,
                    discover = request.discover,
                    latencyMs = 0,
                    cached = false,
                    scoreSources = ScoreSourcesDto()
                )
            )
        }
    }

    private val cardsRepository = CardsRepository(fakeApiService)
    private val cardsViewModel = CardsViewModel(cardsRepository, StoreRepository(fakeApiService))

    @Test
    fun permissionDenied_showsBannerAndMap() {
        val locationUiStateFlow = MutableStateFlow(
            LocationUiState(
                permissionStatus = LocationPermissionStatus.Denied,
                isLoading = false
            )
        )

        composeTestRule.setContent {
            MainScreen(
                navController = rememberNavController(),
                isDarkMode = false,
                onToggleDarkMode = {},
                locationUiStateFlow = locationUiStateFlow,
                cardsViewModel = cardsViewModel
            )
        }

        // Verify banner is shown
        composeTestRule.onNodeWithTag("locationPermissionBanner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Location permission needed").assertIsDisplayed()
        composeTestRule.onNodeWithTag("openSettingsButton").assertIsDisplayed()

        // Verify map is still shown (fallback viewport)
        composeTestRule.onNodeWithTag("map").assertIsDisplayed()
    }
}
