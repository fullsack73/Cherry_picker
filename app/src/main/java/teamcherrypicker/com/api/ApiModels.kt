package teamcherrypicker.com.api

import com.google.gson.annotations.SerializedName

data class CardsResponse(
    @SerializedName("data") val data: List<CardDto>,
    @SerializedName("meta") val meta: CardsMetaDto
)

data class CardDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("issuer") val issuer: String,
    @SerializedName("normalizedCategories") val normalizedCategories: List<String>
)

data class CardsMetaDto(
    @SerializedName("total") val total: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("offset") val offset: Int,
    @SerializedName("lastRefreshedAt") val lastRefreshedAt: String?,
    @SerializedName("dataSource") val dataSource: String?
)

data class CardBenefitsResponse(
    @SerializedName("data") val data: List<CardBenefitDto>
)

data class CardBenefitDto(
    @SerializedName("id") val id: Int,
    @SerializedName("card_id") val cardId: Int,
    @SerializedName("description") val description: String,
    @SerializedName("keyword") val keyword: String?,
    @SerializedName("source_category") val sourceCategory: String,
    @SerializedName("normalized_category") val normalizedCategory: String
)

data class StoresResponse(
    @SerializedName("data") val data: List<StoreDto>
)

data class StoreDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("branch") val branch: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("source_category") val sourceCategory: String,
    @SerializedName("normalized_category") val normalizedCategory: String,
    @SerializedName("distance") val distance: Double
)
