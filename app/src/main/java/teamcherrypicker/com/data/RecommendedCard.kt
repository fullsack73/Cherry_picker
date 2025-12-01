package teamcherrypicker.com.data

import java.util.Locale

enum class RecommendationScoreSource {
    LOCATION,
    LLM,
    FALLBACK;

    companion object {
        fun fromRaw(value: String?): RecommendationScoreSource {
            return when (value?.lowercase(Locale.US)) {
                "location" -> LOCATION
                "llm" -> LLM
                else -> FALLBACK
            }
        }
    }
}

data class RecommendationCard(
    val cardId: Int,
    val cardName: String,
    val issuer: String,
    val normalizedCategories: List<String>,
    val score: Int,
    val scoreSource: RecommendationScoreSource,
    val rationale: String?
)

data class RecommendationScoreBreakdown(
    val location: Int = 0,
    val llm: Int = 0,
    val fallback: Int = 0
)

data class RecommendationMeta(
    val total: Int,
    val limit: Int,
    val discover: Boolean,
    val storeId: Int?,
    val latencyMs: Long?,
    val cached: Boolean,
    val scoreSources: RecommendationScoreBreakdown
)

data class RecommendationResult(
    val cards: List<RecommendationCard>,
    val meta: RecommendationMeta
)
