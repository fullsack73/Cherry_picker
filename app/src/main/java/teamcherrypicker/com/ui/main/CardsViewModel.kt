package teamcherrypicker.com.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import teamcherrypicker.com.data.CardBenefit
import teamcherrypicker.com.data.CardSummary
import teamcherrypicker.com.data.CardsMeta
import teamcherrypicker.com.data.CardsPage
import teamcherrypicker.com.data.CardsRepository

data class CardsUiState(
    val isLoading: Boolean = true,
    val cards: List<CardSummary> = emptyList(),
    val meta: CardsMeta? = null,
    val errorMessage: String? = null
)

data class BenefitsUiState(
    val cardId: Int? = null,
    val isLoading: Boolean = false,
    val benefits: List<CardBenefit> = emptyList(),
    val errorMessage: String? = null
)

class CardsViewModel(private val repository: CardsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CardsUiState())
    val uiState: StateFlow<CardsUiState> = _uiState.asStateFlow()

    private val _benefitsState = MutableStateFlow(BenefitsUiState())
    val benefitsState: StateFlow<BenefitsUiState> = _benefitsState.asStateFlow()

    private val benefitsCache = ConcurrentHashMap<Int, List<CardBenefit>>()

    init {
        loadCards()
    }

    fun loadCards(category: String? = null, limit: Int = 25, offset: Int = 0) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val page: CardsPage = repository.fetchCards(limit = limit, offset = offset, category = category)
                _uiState.value = CardsUiState(
                    isLoading = false,
                    cards = page.cards,
                    meta = page.meta,
                    errorMessage = null
                )
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unable to load cards"
                    )
                }
            }
        }
    }

    fun selectCard(cardId: Int) {
        val cached = benefitsCache[cardId]
        if (cached != null) {
            _benefitsState.value = BenefitsUiState(cardId = cardId, benefits = cached, isLoading = false)
            return
        }

        viewModelScope.launch {
            _benefitsState.value = BenefitsUiState(cardId = cardId, isLoading = true)
            try {
                val benefits = repository.fetchCardBenefits(cardId)
                benefitsCache[cardId] = benefits
                _benefitsState.value = BenefitsUiState(cardId = cardId, benefits = benefits, isLoading = false)
            } catch (throwable: Throwable) {
                _benefitsState.value = BenefitsUiState(
                    cardId = cardId,
                    isLoading = false,
                    benefits = emptyList(),
                    errorMessage = throwable.message ?: "Unable to load benefits"
                )
            }
        }
    }

    companion object {
        fun provideFactory(repository: CardsRepository = CardsRepository()): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(CardsViewModel::class.java)) {
                        return CardsViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${'$'}modelClass")
                }
            }
        }
    }
}
