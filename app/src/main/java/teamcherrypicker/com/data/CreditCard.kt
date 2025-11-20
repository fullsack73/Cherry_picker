package teamcherrypicker.com.data

data class CardSummary(
    val id: Int,
    val name: String,
    val issuer: String,
    val normalizedCategories: List<String>
)

data class CardsMeta(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val lastRefreshedAt: String?,
    val dataSource: String?
)

data class CardsPage(
    val cards: List<CardSummary>,
    val meta: CardsMeta
)

data class CardBenefit(
    val id: Int,
    val description: String,
    val normalizedCategory: String,
    val keyword: String?
)
