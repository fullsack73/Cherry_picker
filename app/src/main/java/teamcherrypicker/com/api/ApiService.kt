package teamcherrypicker.com.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import teamcherrypicker.com.data.UserLocation

interface ApiService {
    @POST("/api/location")
    suspend fun sendLocation(@Body location: UserLocation): Response<Unit>

    @GET("/api/cards")
    suspend fun getCards(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("category") category: String? = null
    ): CardsResponse

    @GET("/api/cards/{cardId}/benefits")
    suspend fun getCardBenefits(
        @Path("cardId") cardId: Int
    ): CardBenefitsResponse

    @GET("/api/stores/nearby")
    suspend fun getNearbyStores(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int? = null,
        @Query("categories") categories: String? = null
    ): StoresResponse

    @GET("/api/stores/search")
    suspend fun searchStores(
        @Query("query") query: String,
        @Query("limit") limit: Int? = null
    ): StoresResponse
}
