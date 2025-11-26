package teamcherrypicker.com.api

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import teamcherrypicker.com.data.UserLocation

class ApiClientTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        apiService = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `sendLocation should make a POST request to the correct endpoint`() = runBlocking {
        // Given
        val location = UserLocation(latitude = 40.7128, longitude = -74.0060)
        server.enqueue(MockResponse().setResponseCode(200))

        // When
        apiService.sendLocation(location)

        // Then
        val request = server.takeRequest()
        assertEquals("/api/location", request.path)
        assertEquals("POST", request.method)
        val expectedBody = Gson().toJson(location)
        assertEquals(expectedBody, request.body.readUtf8())
    }
}
