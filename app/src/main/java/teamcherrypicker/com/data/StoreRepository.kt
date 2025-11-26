package teamcherrypicker.com.data

import teamcherrypicker.com.api.ApiClient
import teamcherrypicker.com.api.ApiService

class StoreRepository(private val apiService: ApiService = ApiClient.apiService) {

    suspend fun fetchNearbyStores(
        latitude: Double,
        longitude: Double,
        radius: Int? = null,
        categories: List<String>? = null
    ): List<Store> {
        val categoriesParam = categories?.joinToString(",")
        
        val response = apiService.getNearbyStores(
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            categories = categoriesParam
        )

        return response.data.mapNotNull { dto ->
            if (dto.latitude == null || dto.longitude == null) return@mapNotNull null
            
            Store(
                id = dto.id,
                name = dto.name ?: "Unknown Store",
                branch = dto.branch,
                address = dto.address,
                latitude = dto.latitude,
                longitude = dto.longitude,
                sourceCategory = dto.sourceCategory ?: "UNKNOWN",
                normalizedCategory = dto.normalizedCategory ?: "UNKNOWN",
                distance = dto.distance
            )
        }
    }
}
