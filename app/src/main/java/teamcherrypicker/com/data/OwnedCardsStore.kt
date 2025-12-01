package teamcherrypicker.com.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.ownedCardsDataStore: DataStore<Preferences> by preferencesDataStore(name = "owned_cards")

class OwnedCardsStore private constructor(
    private val dataStore: DataStore<Preferences>
) {

    val ownedCardIds: Flow<Set<Int>> = dataStore.data.map { preferences ->
        preferences[OWNED_IDS_KEY]
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    suspend fun setCardOwned(cardId: Int, owned: Boolean) {
        dataStore.edit { preferences ->
            val current = preferences[OWNED_IDS_KEY]?.toMutableSet() ?: mutableSetOf()
            if (owned) {
                current.add(cardId.toString())
            } else {
                current.remove(cardId.toString())
            }
            preferences[OWNED_IDS_KEY] = current
        }
    }

    suspend fun replaceAll(cardIds: Set<Int>) {
        dataStore.edit { preferences ->
            preferences[OWNED_IDS_KEY] = cardIds.map { it.toString() }.toSet()
        }
    }

    companion object {
        private val OWNED_IDS_KEY = stringSetPreferencesKey("owned_card_ids")

        fun from(context: Context): OwnedCardsStore {
            return OwnedCardsStore(context.applicationContext.ownedCardsDataStore)
        }
    }
}
