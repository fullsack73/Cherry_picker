package teamcherrypicker.com.data

data class Store(
    val id: Int,
    val name: String,
    val branch: String?,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val sourceCategory: String,
    val normalizedCategory: String,
    val distance: Double
)
