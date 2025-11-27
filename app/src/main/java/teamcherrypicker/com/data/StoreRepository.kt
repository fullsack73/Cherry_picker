package teamcherrypicker.com.data

import android.util.Log
import teamcherrypicker.com.api.ApiClient
import teamcherrypicker.com.api.ApiService
import teamcherrypicker.com.api.StoreDto

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

        val stores = response.data.mapNotNull(::mapStoreDto).distinctBy { it.id }

        Log.d(
            "StoreRepository",
            "Fetched ${stores.size} stores for lat=$latitude lon=$longitude radius=${radius ?: "default"} categories=${categoriesParam ?: "all"}"
        )

        stores.take(5).forEachIndexed { index, store ->
            Log.d(
                "StoreRepository",
                "Store[$index]=${store.id}:${store.name}@${store.latitude},${store.longitude} category=${store.normalizedCategory}"
            )
        }

        return stores
    }

    suspend fun searchStores(query: String, limit: Int? = null): List<Store> {
        val sanitized = query.trim()
        if (sanitized.isEmpty()) {
            return emptyList()
        }

        val response = apiService.searchStores(query = sanitized, limit = limit)
        val stores = response.data.mapNotNull(::mapStoreDto).distinctBy { it.id }

        Log.d("StoreRepository", "Search query='$sanitized' returned ${stores.size} stores")
        return stores
    }

    private fun mapStoreDto(dto: StoreDto): Store? {
        val lat = dto.latitude ?: return null
        val lon = dto.longitude ?: return null
        if (lat.isNaN() || lon.isNaN()) return null

        return Store(
            id = dto.id,
            name = dto.name ?: "Unknown Store",
            branch = dto.branch,
            address = dto.address,
            latitude = lat,
            longitude = lon,
            sourceCategory = dto.sourceCategory ?: "UNKNOWN",
            normalizedCategory = dto.normalizedCategory ?: "UNKNOWN",
            distance = dto.distance ?: 0.0
        )
    }
}
