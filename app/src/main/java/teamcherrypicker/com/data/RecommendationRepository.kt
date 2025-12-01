package teamcherrypicker.com.data

import teamcherrypicker.com.api.ApiClient
import teamcherrypicker.com.api.ApiService
import teamcherrypicker.com.api.RecommendationCardDto
import teamcherrypicker.com.api.RecommendationRequestDto
import teamcherrypicker.com.api.RecommendationMetaDto
import teamcherrypicker.com.api.RecommendationsResponse
import teamcherrypicker.com.api.ScoreSourcesDto

class RecommendationRepository(
    private val apiService: ApiService = ApiClient.apiService
) {

    suspend fun fetchRecommendations(request: RecommendationRequestDto): RecommendationResult {
        val response = apiService.getRecommendations(request)
        return mapResponse(response)
    }

    suspend fun fetchRecommendations(
        store: Store,
        ownedCardIds: Set<Int>,
        discover: Boolean,
        locationKeywords: List<String> = emptyList(),
        limit: Int = DEFAULT_LIMIT
    ): RecommendationResult {
        val storeCategory = store.normalizedCategory.ifBlank { store.sourceCategory }
        val request = RecommendationRequestDto(
            storeId = store.id,
            storeName = store.name,
            storeCategory = storeCategory,
            ownedCardIds = ownedCardIds.toList(),
            discover = discover,
            locationKeywords = locationKeywords,
            limit = limit
        )
        return fetchRecommendations(request)
    }

    private fun mapResponse(response: RecommendationsResponse): RecommendationResult {
        val cards = response.data.map(::mapCard)
        val meta = mapMeta(response.meta)
        return RecommendationResult(cards = cards, meta = meta)
    }

    private fun mapCard(dto: RecommendationCardDto): RecommendationCard {
        return RecommendationCard(
            cardId = dto.cardId,
            cardName = dto.cardName,
            issuer = dto.issuer,
            normalizedCategories = dto.normalizedCategories,
            score = dto.score,
            scoreSource = RecommendationScoreSource.fromRaw(dto.scoreSource),
            rationale = dto.rationale
        )
    }

    private fun mapMeta(dto: RecommendationMetaDto): RecommendationMeta {
        return RecommendationMeta(
            total = dto.total,
            limit = dto.limit,
            discover = dto.discover,
            storeId = dto.storeId,
            latencyMs = dto.latencyMs,
            cached = dto.cached,
            scoreSources = mapBreakdown(dto.scoreSources)
        )
    }

    private fun mapBreakdown(dto: ScoreSourcesDto?): RecommendationScoreBreakdown {
        return RecommendationScoreBreakdown(
            location = dto?.location ?: 0,
            llm = dto?.llm ?: 0,
            fallback = dto?.fallback ?: 0
        )
    }

    companion object {
        private const val DEFAULT_LIMIT = 10
    }
}
