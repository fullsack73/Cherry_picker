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

        return response.data.map { dto ->
            Store(
                id = dto.id,
                name = dto.name,
                branch = dto.branch,
                address = dto.address,
                latitude = dto.latitude,
                longitude = dto.longitude,
                sourceCategory = dto.sourceCategory,
                normalizedCategory = dto.normalizedCategory,
                distance = dto.distance
            )
        }
    }
}
