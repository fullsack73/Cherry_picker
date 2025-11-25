package teamcherrypicker.com.ui.main

import java.util.Locale

private val CATEGORY_LABELS = mapOf(
    "ALL_MERCHANTS" to "모든가맹점",
    "BENEFIT_VOUCHER" to "바우처",
    "BILLS" to "공과금",
    "CAFE_BEVERAGE" to "카페/음료",
    "CASHBACK" to "캐시백",
    "CONVENIENCE" to "편의점",
    "DIGITAL_PAYMENT" to "간편결제",
    "DIGITAL_SUBSCRIPTION" to "디지털 구독",
    "DISCOUNT" to "할인",
    "DINING" to "외식",
    "EDUCATION" to "교육",
    "ENTERTAINMENT" to "문화/엔터",
    "ENTERTAINMENT_TRAVEL" to "여가/숙박",
    "FINANCIAL_SERVICES" to "금융/보험",
    "FOOD_DELIVERY" to "배달앱",
    "FUEL_AUTOMOTIVE" to "주유/차량",
    "HEALTHCARE" to "의료/병원",
    "INSTALLMENT" to "무이자할부",
    "LIFESTYLE" to "생활/뷰티",
    "ONLINE_SHOPPING" to "온라인쇼핑",
    "OTHER" to "기타",
    "OVERSEAS" to "해외이용",
    "PREMIUM_SERVICE" to "프리미엄 서비스",
    "PUBLIC_TRANSPORT" to "대중교통",
    "RETAIL" to "리테일/마트",
    "REWARDS" to "포인트/적립",
    "SHOPPING" to "쇼핑",
    "TAXI" to "택시",
    "TELECOM" to "통신",
    "TRAVEL_AIR" to "항공/공항"
)

fun String.toCategoryLabel(): String {
    val key = uppercase(Locale.ROOT)
    return CATEGORY_LABELS[key] ?: this
}
