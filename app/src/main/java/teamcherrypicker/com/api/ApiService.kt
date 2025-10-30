package teamcherrypicker.com.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import teamcherrypicker.com.data.UserLocation

interface ApiService {
    @POST("/api/location")
    suspend fun sendLocation(@Body location: UserLocation): Response<Unit>
}
