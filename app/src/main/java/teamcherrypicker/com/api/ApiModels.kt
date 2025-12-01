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
    @SerializedName("name") val name: String?,
    @SerializedName("branch") val branch: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("source_category") val sourceCategory: String?,
    @SerializedName("normalized_category") val normalizedCategory: String?,
    @SerializedName("distance") val distance: Double?
)

data class RecommendationRequestDto(
    @SerializedName("storeId") val storeId: Int,
    @SerializedName("storeName") val storeName: String,
    @SerializedName("storeCategory") val storeCategory: String,
    @SerializedName("ownedCardIds") val ownedCardIds: List<Int>,
    @SerializedName("discover") val discover: Boolean,
    @SerializedName("locationKeywords") val locationKeywords: List<String> = emptyList(),
    @SerializedName("limit") val limit: Int = 10
)

data class RecommendationsResponse(
    @SerializedName("data") val data: List<RecommendationCardDto>,
    @SerializedName("meta") val meta: RecommendationMetaDto
)

data class RecommendationCardDto(
    @SerializedName("cardId") val cardId: Int,
    @SerializedName("cardName") val cardName: String,
    @SerializedName("issuer") val issuer: String,
    @SerializedName("normalizedCategories") val normalizedCategories: List<String>,
    @SerializedName("score") val score: Int,
    @SerializedName("scoreSource") val scoreSource: String,
    @SerializedName("rationale") val rationale: String?
)

data class RecommendationMetaDto(
    @SerializedName("total") val total: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("storeId") val storeId: Int?,
    @SerializedName("discover") val discover: Boolean,
    @SerializedName("latencyMs") val latencyMs: Long?,
    @SerializedName("cached") val cached: Boolean,
    @SerializedName("scoreSources") val scoreSources: ScoreSourcesDto?
)

data class ScoreSourcesDto(
    @SerializedName("location") val location: Int = 0,
    @SerializedName("llm") val llm: Int = 0,
    @SerializedName("fallback") val fallback: Int = 0
)
