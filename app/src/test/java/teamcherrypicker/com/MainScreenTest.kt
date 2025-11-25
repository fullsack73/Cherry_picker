package teamcherrypicker.com

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response
import teamcherrypicker.com.api.ApiService
import teamcherrypicker.com.api.CardBenefitDto
import teamcherrypicker.com.api.CardBenefitsResponse
import teamcherrypicker.com.api.CardDto
import teamcherrypicker.com.api.CardsMetaDto
import teamcherrypicker.com.api.CardsResponse
import teamcherrypicker.com.data.CardsRepository
import teamcherrypicker.com.data.UserLocation
import teamcherrypicker.com.location.LocationPermissionStatus
import teamcherrypicker.com.location.LocationUiState
import teamcherrypicker.com.ui.main.CardsViewModel
import teamcherrypicker.com.ui.main.LocalMapSurfaceRenderer
import teamcherrypicker.com.ui.main.MapSurfaceRenderer
import teamcherrypicker.com.ui.main.MainScreen
import kotlinx.coroutines.flow.MutableStateFlow
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mapSurface_rendersAlongsideFloatingSearchBar() {
        setMainScreenContent()

        composeTestRule.onNodeWithTag("mapSurface").assertIsDisplayed()
        composeTestRule.onNodeWithTag("floatingSearchBar").assertIsDisplayed()
    }

    @Test
    fun floatingSearchBar_remainsVisibleAfterTextInput() {
        setMainScreenContent()

        composeTestRule.onNodeWithText("Search for a place or store...").performTextInput("Coffee")

        composeTestRule.onNodeWithTag("mapSurface").assertIsDisplayed()
        composeTestRule.onNodeWithTag("floatingSearchBar").assertIsDisplayed()
    }

    @Test
    fun mapSurface_setsAccessibleDescription() {
        setMainScreenContent()

        composeTestRule
            .onNodeWithContentDescription("Interactive map showing store locations")
            .assertIsDisplayed()
    }

    private fun setMainScreenContent() {
        composeTestRule.setContent {
            val context = LocalContext.current
            val navController = remember {
                TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            }
            val cardsViewModel = remember { createTestCardsViewModel() }
            val locationFlow = remember {
                MutableStateFlow(
                    LocationUiState(
                        permissionStatus = LocationPermissionStatus.Granted,
                        lastKnownLocation = LatLng(1.35, 103.87)
                    )
                )
            }

            CompositionLocalProvider(LocalMapSurfaceRenderer provides fakeMapRenderer) {
                MainScreen(
                    navController = navController,
                    isDarkMode = false,
                    onToggleDarkMode = {},
                    locationUiStateFlow = locationFlow,
                    cardsViewModel = cardsViewModel
                )
            }
        }
    }

    private fun createTestCardsViewModel(): CardsViewModel {
        val fakeApiService = object : ApiService {
            override suspend fun sendLocation(location: UserLocation): Response<Unit> = Response.success(Unit)

            override suspend fun getCards(limit: Int?, offset: Int?, category: String?): CardsResponse {
                val card = CardDto(id = 1, name = "Test Card", issuer = "Test Bank", normalizedCategories = listOf("DINING"))
                val meta = CardsMetaDto(total = 1, limit = 1, offset = 0, lastRefreshedAt = "2024-01-01", dataSource = "test")
                return CardsResponse(data = listOf(card), meta = meta)
            }

            override suspend fun getCardBenefits(cardId: Int): CardBenefitsResponse {
                val benefit = CardBenefitDto(
                    id = 10,
                    cardId = cardId,
                    description = "10% off",
                    keyword = "coffee",
                    sourceCategory = "Dining",
                    normalizedCategory = "DINING"
                )
                return CardBenefitsResponse(data = listOf(benefit))
            }
        }

        return CardsViewModel(CardsRepository(fakeApiService))
    }

    private val fakeMapRenderer: MapSurfaceRenderer =
        { modifier, _, _, contentDescription, _, _ ->
            Box(
                modifier = modifier.semantics {
                    this.contentDescription = contentDescription
                }
            )
        }
}
