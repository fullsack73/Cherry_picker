package teamcherrypicker.com.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Job
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
import teamcherrypicker.com.data.Store
import teamcherrypicker.com.data.StoreRepository

data class CardsUiState(
    val isLoading: Boolean = true,
    val isAppending: Boolean = false,
    val cards: List<CardSummary> = emptyList(),
    val meta: CardsMeta? = null,
    val errorMessage: String? = null,
    val hasMore: Boolean = true
)

data class BenefitsUiState(
    val cardId: Int? = null,
    val isLoading: Boolean = false,
    val benefits: List<CardBenefit> = emptyList(),
    val errorMessage: String? = null
)

data class StoresUiState(
    val isLoading: Boolean = false,
    val stores: List<Store> = emptyList(),
    val errorMessage: String? = null
)

data class StoreSearchState(
    val isSearching: Boolean = false,
    val results: List<Store> = emptyList(),
    val errorMessage: String? = null
)

class CardsViewModel(
    private val repository: CardsRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardsUiState())
    val uiState: StateFlow<CardsUiState> = _uiState.asStateFlow()

    private val _benefitsState = MutableStateFlow(BenefitsUiState())
    val benefitsState: StateFlow<BenefitsUiState> = _benefitsState.asStateFlow()

    private val _storesUiState = MutableStateFlow(StoresUiState())
    val storesUiState: StateFlow<StoresUiState> = _storesUiState.asStateFlow()

    private val _storeSearchState = MutableStateFlow(StoreSearchState())
    val storeSearchState: StateFlow<StoreSearchState> = _storeSearchState.asStateFlow()

    private var storeSearchJob: Job? = null

    private val benefitsCache = ConcurrentHashMap<Int, List<CardBenefit>>()

    private var nextOffset = 0
    private var currentCategory: String? = null
    private var currentLimit: Int = DEFAULT_PAGE_LIMIT

    init {
        refreshCards()
    }

    fun refreshCards(category: String? = null, limit: Int = DEFAULT_PAGE_LIMIT) {
        currentCategory = category
        currentLimit = limit
        nextOffset = 0
        fetchCards(offset = 0, append = false)
    }

    fun loadMoreCards() {
        val state = _uiState.value
        if (state.isLoading || state.isAppending || !state.hasMore) return
        fetchCards(offset = nextOffset, append = true)
    }

    fun loadStores(latitude: Double, longitude: Double, radius: Int = 500, categories: List<String>? = null) {
        viewModelScope.launch {
            _storesUiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val stores = storeRepository.fetchNearbyStores(latitude, longitude, radius, categories)
                _storesUiState.update { it.copy(isLoading = false, stores = stores) }
            } catch (throwable: Throwable) {
                _storesUiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unable to load stores"
                    )
                }
            }
        }
    }

    fun loadCards(category: String? = null, limit: Int = DEFAULT_PAGE_LIMIT, offset: Int = 0) {
        currentCategory = category
        currentLimit = limit
        nextOffset = if (offset > 0) offset else 0
        fetchCards(offset = offset, append = offset > 0)
    }

    private fun fetchCards(offset: Int, append: Boolean) {
        viewModelScope.launch {
            if (append) {
                _uiState.update { it.copy(isAppending = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            try {
                val page: CardsPage = repository.fetchCards(
                    limit = currentLimit,
                    offset = offset,
                    category = currentCategory
                )
                val newOffset = offset + page.cards.size
                nextOffset = newOffset
                _uiState.update { current ->
                    val mergedCards = if (append) current.cards + page.cards else page.cards
                    current.copy(
                        isLoading = false,
                        isAppending = false,
                        cards = mergedCards,
                        meta = page.meta,
                        errorMessage = null,
                        hasMore = newOffset < page.meta.total
                    )
                }
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = if (append) current.isLoading else false,
                        isAppending = false,
                        errorMessage = throwable.message ?: if (append) {
                            "Unable to load more cards"
                        } else {
                            "Unable to load cards"
                        }
                    )
                }
            }
        }
    }

    fun searchStores(query: String, limit: Int = 10) {
        val sanitized = query.trim()
        if (sanitized.isEmpty()) {
            clearStoreSearch()
            return
        }

        storeSearchJob?.cancel()
        storeSearchJob = viewModelScope.launch {
            _storeSearchState.update { it.copy(isSearching = true, errorMessage = null) }
            try {
                val stores = storeRepository.searchStores(sanitized, limit)
                _storeSearchState.value = StoreSearchState(
                    isSearching = false,
                    results = stores,
                    errorMessage = if (stores.isEmpty()) "No stores matched '$sanitized'" else null
                )
            } catch (throwable: Throwable) {
                _storeSearchState.value = StoreSearchState(
                    isSearching = false,
                    errorMessage = throwable.message ?: "Unable to search stores"
                )
            }
        }
    }

    fun clearStoreSearch() {
        storeSearchJob?.cancel()
        _storeSearchState.value = StoreSearchState()
    }

    fun clearSearchMessage() {
        _storeSearchState.update { it.copy(errorMessage = null) }
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
        private const val DEFAULT_PAGE_LIMIT = 50
        fun provideFactory(
            repository: CardsRepository = CardsRepository(),
            storeRepository: StoreRepository = StoreRepository()
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(CardsViewModel::class.java)) {
                        return CardsViewModel(repository, storeRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${'$'}modelClass")
                }
            }
        }
    }
}
