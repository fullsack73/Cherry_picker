package teamcherrypicker.com.data

import android.util.Log
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

        val stores = response.data.mapNotNull { dto ->
            if (dto.latitude == null || dto.longitude == null || dto.latitude.isNaN() || dto.longitude.isNaN()) return@mapNotNull null
            
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
        }.distinctBy { it.id }

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
}
