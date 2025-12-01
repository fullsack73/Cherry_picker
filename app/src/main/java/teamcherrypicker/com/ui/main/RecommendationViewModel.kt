package teamcherrypicker.com.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import teamcherrypicker.com.api.RecommendationRequestDto
import teamcherrypicker.com.api.StreamEvent
import teamcherrypicker.com.api.StreamingRecommendationClient
import teamcherrypicker.com.data.OwnedCardsStore
import teamcherrypicker.com.data.RecommendationCard
import teamcherrypicker.com.data.RecommendationMeta
import teamcherrypicker.com.data.RecommendationRepository
import teamcherrypicker.com.data.RecommendationResult
import teamcherrypicker.com.data.RecommendationScoreBreakdown
import teamcherrypicker.com.data.RecommendationScoreSource
import teamcherrypicker.com.data.Store

data class RecommendationUiState(
    val selectedStore: Store? = null,
    val cards: List<RecommendationCard> = emptyList(),
    val meta: RecommendationMeta? = null,
    val ownedCardIds: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val isStreaming: Boolean = false,
    val streamProgress: Int = 0,
    val errorMessage: String? = null,
    val discoverMode: Boolean = false,
    val discoverInFlight: Boolean = false,
    val discoverEnabled: Boolean = true,
    val fallbackBannerMessage: String? = null
)

private data class RecommendationRequestContext(
    val store: Store,
    val discover: Boolean,
    val locationKeywords: List<String>
)

class RecommendationViewModel(
    private val repository: RecommendationRepository,
    private val ownedCardsStore: OwnedCardsStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationUiState())
    val uiState: StateFlow<RecommendationUiState> = _uiState

    private var latestOwnedCardIds: Set<Int> = emptySet()
    private var lastContext: RecommendationRequestContext? = null
    private var fetchJob: Job? = null
    private var pendingOwnedCardsRefresh: Boolean = false

    init {
        viewModelScope.launch {
            ownedCardsStore.ownedCardIds.collectLatest { ids ->
                if (ids == latestOwnedCardIds) {
                    _uiState.update { it.copy(ownedCardIds = ids) }
                    return@collectLatest
                }
                latestOwnedCardIds = ids
                _uiState.update { it.copy(ownedCardIds = ids) }
                lastContext?.let { context ->
                    if (fetchJob?.isActive == true) {
                        pendingOwnedCardsRefresh = true
                    } else {
                        fetchRecommendationsStreaming(context)
                    }
                }
            }
        }
    }

    fun onStoreSelected(store: Store) {
        val keywords = buildLocationKeywords(store)
        val context = RecommendationRequestContext(store = store, discover = false, locationKeywords = keywords)
        lastContext = context
        _uiState.value = RecommendationUiState(
            selectedStore = store,
            ownedCardIds = latestOwnedCardIds,
            isLoading = true,
            isStreaming = true,
            discoverEnabled = false
        )
        fetchRecommendationsStreaming(context)
    }

    fun clearSelection() {
        lastContext = null
        fetchJob?.cancel()
        _uiState.value = RecommendationUiState(ownedCardIds = latestOwnedCardIds)
    }

    fun retry() {
        val context = lastContext ?: return
        fetchRecommendationsStreaming(context)
    }

    fun showDiscoverRecommendations() {
        val currentState = _uiState.value
        if (!currentState.discoverEnabled || currentState.isStreaming) return
        
        val context = lastContext ?: return
        if (context.discover) return
        val discoverContext = context.copy(discover = true)
        lastContext = discoverContext
        fetchRecommendationsBatch(discoverContext)
    }

    fun showOwnedRecommendations() {
        val context = lastContext ?: return
        if (!context.discover) return
        val ownedContext = context.copy(discover = false)
        lastContext = ownedContext
        fetchRecommendationsStreaming(ownedContext)
    }

    private fun fetchRecommendationsStreaming(context: RecommendationRequestContext) {
        pendingOwnedCardsRefresh = false
        fetchJob?.cancel()
        
        val job = viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    selectedStore = context.store,
                    cards = emptyList(),
                    isLoading = true,
                    isStreaming = true,
                    streamProgress = 0,
                    errorMessage = null,
                    discoverMode = false,
                    discoverEnabled = false,
                    fallbackBannerMessage = null
                )
            }

            val storeCategory = context.store.normalizedCategory.ifBlank { context.store.sourceCategory }
            val request = RecommendationRequestDto(
                storeId = context.store.id,
                storeName = context.store.name,
                storeCategory = storeCategory,
                ownedCardIds = latestOwnedCardIds.toList(),
                discover = false,
                locationKeywords = context.locationKeywords,
                limit = 10
            )

            var cardCount = 0
            val collectedCards = mutableListOf<RecommendationCard>()

            try {
                StreamingRecommendationClient.streamRecommendations(request).collect { event ->
                    when (event) {
                        is StreamEvent.Card -> {
                            cardCount++
                            val card = RecommendationCard(
                                cardId = event.cardId,
                                cardName = event.cardName,
                                issuer = event.issuer,
                                normalizedCategories = event.normalizedCategories,
                                score = event.score,
                                scoreSource = RecommendationScoreSource.fromRaw(event.scoreSource),
                                rationale = event.rationale
                            )
                            
                            // Insert card in sorted order (highest score first)
                            val insertIndex = collectedCards.indexOfFirst { it.score < card.score }
                            if (insertIndex == -1) {
                                collectedCards.add(card)
                            } else {
                                collectedCards.add(insertIndex, card)
                            }
                            
                            _uiState.update { current ->
                                current.copy(
                                    cards = collectedCards.toList(),
                                    isLoading = false,
                                    streamProgress = cardCount
                                )
                            }
                        }
                        is StreamEvent.Done -> {
                            val meta = RecommendationMeta(
                                total = event.total,
                                limit = event.limit,
                                discover = event.discover,
                                storeId = event.storeId,
                                latencyMs = 0,
                                cached = false,
                                scoreSources = RecommendationScoreBreakdown(
                                    location = event.locationCount,
                                    llm = event.llmCount,
                                    fallback = event.fallbackCount
                                )
                            )
                            _uiState.update { current ->
                                current.copy(
                                    meta = meta,
                                    isLoading = false,
                                    isStreaming = false,
                                    discoverEnabled = true,
                                    fallbackBannerMessage = buildFallbackMessage(meta)
                                )
                            }
                        }
                        is StreamEvent.Error -> {
                            _uiState.update { current ->
                                current.copy(
                                    isLoading = false,
                                    isStreaming = false,
                                    discoverEnabled = true,
                                    errorMessage = event.message
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            isStreaming = false,
                            discoverEnabled = true,
                            errorMessage = e.message ?: "Stream failed"
                        )
                    }
                }
            }
        }

        job.invokeOnCompletion { cause ->
            if (pendingOwnedCardsRefresh && cause !is CancellationException) {
                pendingOwnedCardsRefresh = false
                lastContext?.let { pendingContext ->
                    fetchRecommendationsStreaming(pendingContext)
                }
            }
        }
        fetchJob = job
    }

    // Non-streaming fallback for discover mode
    private fun fetchRecommendationsBatch(context: RecommendationRequestContext) {
        pendingOwnedCardsRefresh = false
        fetchJob?.cancel()
        val job = viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    selectedStore = context.store,
                    isLoading = true,
                    errorMessage = null,
                    discoverMode = context.discover,
                    discoverInFlight = context.discover
                )
            }
            runCatching {
                repository.fetchRecommendations(
                    store = context.store,
                    ownedCardIds = latestOwnedCardIds,
                    discover = context.discover,
                    locationKeywords = context.locationKeywords
                )
            }.onSuccess { result ->
                handleSuccess(context, result)
            }.onFailure { throwable ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unable to load recommendations",
                        discoverInFlight = false
                    )
                }
            }
        }
        job.invokeOnCompletion { cause ->
            if (pendingOwnedCardsRefresh && cause !is CancellationException) {
                pendingOwnedCardsRefresh = false
                lastContext?.let { pendingContext ->
                    fetchRecommendationsStreaming(pendingContext)
                }
            }
        }
        fetchJob = job
    }

    private fun handleSuccess(context: RecommendationRequestContext, result: RecommendationResult) {
        _uiState.update {
            it.copy(
                selectedStore = context.store,
                cards = result.cards,
                meta = result.meta,
                isLoading = false,
                isStreaming = false,
                errorMessage = null,
                discoverMode = result.meta.discover,
                discoverInFlight = false,
                discoverEnabled = true,
                fallbackBannerMessage = buildFallbackMessage(result.meta)
            )
        }
    }

    private fun buildFallbackMessage(meta: RecommendationMeta?): String? {
        meta ?: return null
        val breakdown = meta.scoreSources
        return when {
            meta.discover -> "Discover mode shows cards you don't own yet."
            breakdown.llm == 0 && breakdown.location == 0 -> "Showing heuristic matches while smart scoring is unavailable."
            else -> null
        }
    }

    private fun buildLocationKeywords(store: Store): List<String> {
        val seen = mutableSetOf<String>()
        val keywords = mutableListOf<String>()
        fun addKeyword(raw: String?) {
            val value = raw?.trim().orEmpty()
            if (value.isEmpty()) return
            val normalized = value.lowercase(Locale.US)
            if (seen.add(normalized)) {
                keywords.add(value)
            }
        }
        addKeyword(store.branch)
        addKeyword(store.address?.substringBefore(','))
        return keywords
    }

    companion object {
        fun provideFactory(
            ownedCardsStore: OwnedCardsStore,
            repository: RecommendationRepository = RecommendationRepository()
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(RecommendationViewModel::class.java)) {
                        return RecommendationViewModel(repository, ownedCardsStore) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${'$'}modelClass")
                }
            }
        }
    }
}
