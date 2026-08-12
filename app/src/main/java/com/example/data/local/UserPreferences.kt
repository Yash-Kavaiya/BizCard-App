package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.PersonalCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_NAME = stringPreferencesKey("card_name")
        private val KEY_TITLE = stringPreferencesKey("card_title")
        private val KEY_COMPANY = stringPreferencesKey("card_company")
        private val KEY_EMAIL = stringPreferencesKey("card_email")
        private val KEY_PHONE = stringPreferencesKey("card_phone")
        private val KEY_WEBSITE = stringPreferencesKey("card_website")
        private val KEY_LINKEDIN = stringPreferencesKey("card_linkedin")
        private val KEY_ADDRESS = stringPreferencesKey("card_address")
        private val KEY_BIO = stringPreferencesKey("card_bio")
        private val KEY_GUJARATI_UI = booleanPreferencesKey("gujarati_ui_enabled")
        private val KEY_OFFLINE_ONLY = booleanPreferencesKey("offline_mode_only")
    }

    val personalCardFlow: Flow<PersonalCard> = context.dataStore.data.map { prefs ->
        PersonalCard(
            name = prefs[KEY_NAME] ?: "Alex Morgan",
            title = prefs[KEY_TITLE] ?: "VP of Business Development",
            company = prefs[KEY_COMPANY] ?: "Apex Innovations Inc.",
            email = prefs[KEY_EMAIL] ?: "alex.morgan@apexinnovations.com",
            phone = prefs[KEY_PHONE] ?: "+1 (555) 234-5678",
            website = prefs[KEY_WEBSITE] ?: "https://apexinnovations.com",
            linkedIn = prefs[KEY_LINKEDIN] ?: "linkedin.com/in/alexmorgan-biz",
            address = prefs[KEY_ADDRESS] ?: "742 Evergreen Terrace, San Francisco, CA",
            bio = prefs[KEY_BIO] ?: "Building strategic enterprise partnerships & AI integrations."
        )
    }

    val gujaratiUiFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_GUJARATI_UI] ?: false
    }

    val offlineModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_OFFLINE_ONLY] ?: false
    }

    suspend fun savePersonalCard(card: PersonalCard) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NAME] = card.name
            prefs[KEY_TITLE] = card.title
            prefs[KEY_COMPANY] = card.company
            prefs[KEY_EMAIL] = card.email
            prefs[KEY_PHONE] = card.phone
            prefs[KEY_WEBSITE] = card.website
            prefs[KEY_LINKEDIN] = card.linkedIn
            prefs[KEY_ADDRESS] = card.address
            prefs[KEY_BIO] = card.bio
        }
    }

    suspend fun setGujaratiUi(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GUJARATI_UI] = enabled
        }
    }

    suspend fun setOfflineMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OFFLINE_ONLY] = enabled
        }
    }
}
