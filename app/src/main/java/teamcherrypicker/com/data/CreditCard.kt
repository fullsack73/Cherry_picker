package teamcherrypicker.com.data

data class CreditCard(
    val id: String,
    val name: String,
    val issuer: String,
    val category: CardCategory,
    val logoUrl: String // URL to the card's logo
)

enum class CardCategory(val displayName: String) {
    TRAVEL("Travel"),
    CASHBACK("Cashback"),
    DINING("Dining"),
    GROCERIES("Groceries"),
    SHOPPING("Shopping"),
    GAS("Gas")
}
