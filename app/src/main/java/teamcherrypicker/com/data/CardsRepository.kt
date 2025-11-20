package teamcherrypicker.com.data

import java.util.Locale
import teamcherrypicker.com.api.ApiClient
import teamcherrypicker.com.api.ApiService

class CardsRepository(private val apiService: ApiService = ApiClient.apiService) {

    suspend fun fetchCards(limit: Int? = null, offset: Int? = null, category: String? = null): CardsPage {
        val normalizedCategory = category?.takeIf { it.isNotBlank() }?.uppercase(Locale.US)
        val response = apiService.getCards(limit = limit, offset = offset, category = normalizedCategory)
        val cards = response.data.map { dto ->
            CardSummary(
                id = dto.id,
                name = dto.name,
                issuer = dto.issuer,
                normalizedCategories = dto.normalizedCategories
            )
        }
        val meta = CardsMeta(
            total = response.meta.total,
            limit = response.meta.limit,
            offset = response.meta.offset,
            lastRefreshedAt = response.meta.lastRefreshedAt,
            dataSource = response.meta.dataSource
        )
        return CardsPage(cards = cards, meta = meta)
    }

    suspend fun fetchCardBenefits(cardId: Int): List<CardBenefit> {
        val benefitsResponse = apiService.getCardBenefits(cardId)
        return benefitsResponse.data.map { dto ->
            CardBenefit(
                id = dto.id,
                description = dto.description,
                normalizedCategory = dto.normalizedCategory,
                keyword = dto.keyword
            )
        }
    }
}
