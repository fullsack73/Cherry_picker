package teamcherrypicker.com.data

import java.net.HttpURLConnection
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import teamcherrypicker.com.api.ApiService

class CardsRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: ApiService
    private lateinit var repository: CardsRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        apiService = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        repository = CardsRepository(apiService)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `fetchCards parses page data and metadata`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(
                    """
                    {
                      "data": [
                        {
                          "id": 1,
                          "name": "Sample Card",
                          "issuer": "Sample Issuer",
                          "normalizedCategories": ["DINING", "TRAVEL"]
                        }
                      ],
                      "meta": {
                        "total": 1,
                        "limit": 10,
                        "offset": 5,
                        "lastRefreshedAt": "2024-01-01T00:00:00.000Z",
                        "dataSource": "abc123"
                      }
                    }
                    """.trimIndent()
                )
        )

        val page = repository.fetchCards(limit = 10, offset = 5)
        val request = server.takeRequest()

        assertEquals("/api/cards?limit=10&offset=5", request.path)
        assertEquals(1, page.cards.size)
        val card = page.cards.first()
        assertEquals(1, card.id)
        assertEquals("Sample Card", card.name)
        assertEquals(listOf("DINING", "TRAVEL"), card.normalizedCategories)
        assertNotNull(page.meta)
        assertEquals(1, page.meta.total)
        assertEquals("abc123", page.meta.dataSource)
    }

    @Test
    fun `fetchCards uppercases category parameter`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(
                    """
                    {
                      "data": [],
                      "meta": {
                        "total": 0,
                        "limit": 25,
                        "offset": 0,
                        "lastRefreshedAt": null,
                        "dataSource": null
                      }
                    }
                    """.trimIndent()
                )
        )

        repository.fetchCards(category = "dining")
        val request = server.takeRequest()
        val category = request.requestUrl?.queryParameter("category")

        assertEquals("DINING", category)
    }

    @Test
    fun `fetchCardBenefits returns ordered benefits`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(
                    """
                    {
                      "data": [
                        {
                          "id": 10,
                          "card_id": 1,
                          "description": "Dining discount",
                          "keyword": "food",
                          "source_category": "외식",
                          "normalized_category": "DINING"
                        },
                        {
                          "id": 11,
                          "card_id": 1,
                          "description": "Cafe cashback",
                          "keyword": "coffee",
                          "source_category": "카페",
                          "normalized_category": "CAFE"
                        }
                      ]
                    }
                    """.trimIndent()
                )
        )

        val benefits = repository.fetchCardBenefits(cardId = 1)
        val request = server.takeRequest()

        assertEquals("/api/cards/1/benefits", request.path)
        assertEquals(2, benefits.size)
        assertTrue(benefits.any { it.description == "Dining discount" && it.normalizedCategory == "DINING" })
        assertTrue(benefits.any { it.description == "Cafe cashback" && it.keyword == "coffee" })
    }
}
